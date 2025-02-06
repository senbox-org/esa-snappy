import argparse
import logging
import os
import os.path
import platform
import sys
import traceback
import zipfile

_ERR_CODE_NO_MATCHING_JPY_WHEEL_FOUND = 10
_ERR_CODE_MISSING_MODULE_JPYUTIL = 20
_ERR_CODE_IMPORTING_SNAPPY_FAILED = 30
_ERR_CODE_IMPORTING_SNAPISTA_FAILED = 40
_ERR_CODE_INTERNAL_ERROR = 50


def _find_file_old(dir_path, regex):
    if os.path.isdir(dir_path):
        for filename in os.listdir(dir_path):
            if regex.match(filename):
                file = os.path.join(dir_path, filename)
                if os.path.isfile(file):
                    return file
    return None


def _find_file(dir_path, jpy_whl_pat):
    if os.path.isdir(dir_path):
        for jpy_whl_filename in os.listdir(dir_path):
            # Windows AMD64
            if "win_amd64" in jpy_whl_pat and "win_amd64" in jpy_whl_filename:
                return _get_file_in_dir(dir_path, jpy_whl_filename)
            # Linux x86_64
            if "linux" in jpy_whl_pat and 'x86_64' in jpy_whl_pat and 'linux' in jpy_whl_filename:
                if 'x86_64' in jpy_whl_filename:
                    return _get_file_in_dir(dir_path, jpy_whl_filename)
            # Linux aarch64
            if 'linux' in jpy_whl_pat and 'aarch64' in jpy_whl_pat and 'linux' in jpy_whl_filename:
                if 'aarch64' in jpy_whl_filename:
                    return _get_file_in_dir(dir_path, jpy_whl_filename)
            # Macos
            if 'macosx' in jpy_whl_pat and 'arm64' in jpy_whl_pat and 'macosx' in jpy_whl_filename:
                if 'arm64' in jpy_whl_filename:
                    return _get_file_in_dir(dir_path, jpy_whl_filename)
    return None


def _get_file_in_dir(dir_path, filename):
    file = os.path.join(dir_path, filename)
    if os.path.isfile(file):
        return file
    else:
        return None


def _configure_snappy(snap_home=None,
                      java_module=None,
                      java_home=None,
                      jvm_max_mem=None,
                      req_arch=None,
                      req_java=False,
                      req_py=False,
                      force=False):
    """
    Unzips matching jpy binary distribution from ../lib/jpy.<platform>-<python-version>.zip,
    imports unpacked 'jpyutil' and configures jpy for SNAP.

    :param snap_home: SNAP distribution directory.
    :param java_home: Java home directory. See also Java system property "java.home".
    :param jvm_max_mem: The heap size of the JVM.
    :param req_arch:  Required JVM architecture (amd64, ia86, x86, etc). See Java system property "os.arch".
    :param req_java:  Fail, if configuration of jpy's Java API fails.
    :param req_py:    Fail, if configuration of jpy's Python API fails.
    :param force:     Force overwriting of existing files.
    :return:
    """

    logging.info("Installing from Java module '" + java_module + "'")

    if req_arch:
        req_arch = req_arch.lower()
        act_arch = platform.machine().lower()

        if req_arch != act_arch:
            logging.warning("Architecture requirement possibly not met: "
                            "Python is " + act_arch + " but JVM requires " + req_arch)
        is64 = sys.maxsize > 2 ** 31 - 1
        if is64 and not req_arch in ('amd64', 'ia64', 'x64', 'x86_64'):
            logging.warning("Architecture requirement possibly not met: "
                            "Python is 64 bit but JVM requires " + req_arch)

    snappy_dir = os.path.dirname(os.path.abspath(__file__))
    snappy_ini_file = os.path.join(snappy_dir, 'esa_snappy.ini')

    jpyutil_file = os.path.join(snappy_dir, 'jpyutil.py')
    jpyconfig_java_file = os.path.join(snappy_dir, 'jpyconfig.properties')
    jpyconfig_py_file = os.path.join(snappy_dir, 'jpyconfig.py')

    must_install_jpy = force \
                       or not os.path.exists(jpyutil_file)
    if not must_install_jpy:
        try:
            import jpyutil
        except ImportError:
            must_install_jpy = True

    must_configure_jpy = force \
                         or must_install_jpy \
                         or not (os.path.exists(jpyconfig_java_file) and os.path.exists(jpyconfig_py_file))
    if not must_configure_jpy:
        try:
            import jpyutil

            jpyutil.preload_jvm_dll()
            import jpy
        except:
            must_configure_jpy = True

    must_configure_snappy = force \
                            or must_configure_jpy \
                            or not os.path.exists(snappy_ini_file)

    #
    # If jpy is not installed yet at all, try to do so by using any compatible binary wheel found
    # in ${snappy_dir} or ${java_module}.
    #
    if must_install_jpy:
        logging.info("Installing jpy...")

        # See "Platform compatibility tags"
        # https://packaging.python.org/en/latest/specifications/platform-compatibility-tags/

        # import distutils.util
        # platform_tag = distutils.util.get_platform().replace('-', '_').replace('.', '_')

        import sysconfig  # switch to sysconfig, as distutils is deprecated in Python 3.10
        platform_tag = sysconfig.get_platform().replace('-', '_').replace('.', '_')
        python_tag = 'cp%d%d' % (sys.version_info.major, sys.version_info.minor,)
        jpy_wheel_file_pat = 'jpy-{version}-%s-{abi_tag}-%s.whl' % (python_tag, platform_tag)

        import re

        jpy_wheel_file_re = jpy_wheel_file_pat.replace('{version}', '[^\-]+').replace('{abi_tag}', '[^\-]+')
        jpy_wheel_file_rec = re.compile(jpy_wheel_file_re)

        #
        # Look for jpy platform wheel in <Python install dir>/Lib/site-packages/esa-snappy/lib
        # For the given Python there should be 4 wheels:
        #     win_amd64, manylinux_x_y_x86_64, manylinux_x_y_aarch64, macos_x_y_universal2
        # in the form jpy-{version}-{python_tag}-{abi_tag}-{platform_tag}.whl
        #
        jpy_wheel_file = _find_file(snappy_dir + os.sep + "lib", jpy_wheel_file_re)

        # No, then search for it in the snap-python module
        # SHOULD NO LONGER BE NEEDED!
        # if not jpy_wheel_file:
        #
        #     # Look for
        #     # ${java_module}/lib/jpy-{version}-{python_tag}-{abi_tag}-{platform_tag}.whl
        #     # depending of whether ${java_module} it is a JAR file or directory
        #
        #     if os.path.isfile(java_module):
        #         with zipfile.ZipFile(java_module) as zf:
        #             lib_prefix = 'lib/'
        #             for name in zf.namelist():
        #                 if name.startswith(lib_prefix):
        #                     basename = name[len(lib_prefix):]
        #                     if jpy_wheel_file_rec.match(basename):
        #                         logging.info("Extracting '" + name + "' from '" + java_module + "'")
        #                         jpy_wheel_file = zf.extract(name, snappy_dir)
        #                         break
        #     else:
        #         lib_dir = os.path.join(java_module, 'lib')
        #         jpy_wheel_file = _find_file(lib_dir, jpy_wheel_file_rec)

        if jpy_wheel_file and os.path.exists(jpy_wheel_file):
            logging.info("Unzipping '" + jpy_wheel_file + "'")
            with zipfile.ZipFile(jpy_wheel_file) as zf:
                zf.extractall(snappy_dir)
        else:
            logging.error("\n".join(
                ["The Java-Python bridge 'jpy' is required to run esa_snappy, but no binary 'jpy' wheel for platform",
                 "'" + sysconfig.get_platform() + "' could be found.",
                 "For further information, please check the esa_snappy log file in: ",
                 "   " + snappy_dir + "/snappyutil.log.",
                 "Please also double-check your installation steps with the documentation in: ",
                 "   https://senbox.atlassian.net/wiki/spaces/SNAP/pages/2499051521/Configure+Python+to+use+the+new+SNAP-Python+esa_snappy+interface+SNAP+version+10",
                 "If this does not help, please feel free to report your problem to the SNAP forum: ",
                 ]))

            return _ERR_CODE_NO_MATCHING_JPY_WHEEL_FOUND
    else:
        logging.info("jpy is already installed")

    #
    # If jpy isn't configured yet, do so by executing "jpyutil.py" which will write the runtime configurations:
    # - "jpyconfig.properties" - Configuration for Java about Python (jpy extension module)
    # - "jpyconfig.py" - Configuration for Python about Java (JVM)
    #
    if must_configure_jpy:
        logging.info("Configuring jpy...")
        if os.path.exists(jpyutil_file):
            # Note 'jpyutil.py' has been unpacked by the previous step, so we can safely import it
            import jpyutil

            if not java_home:
                jre_dir = os.path.join(snap_home, 'jre')
                if not os.path.exists(jre_dir):
                    parent = os.path.dirname(jpyutil_file)
                    while parent:
                        jre_dir = os.path.join(parent, 'jre')
                        if os.path.exists(jre_dir):
                            break
                        parent = os.path.dirname(parent)
                if os.path.exists(jre_dir):
                    java_home = os.path.normpath(jre_dir)

            ret_code = jpyutil.write_config_files(out_dir=snappy_dir,
                                                  java_home_dir=java_home,
                                                  req_java_api_conf=req_java,
                                                  req_py_api_conf=req_py)
            if ret_code:
                return ret_code
        else:
            logging.error("Missing Python module '{}'\nwhich is required to complete the configuration."
                          .format(jpyutil_file))
            return _ERR_CODE_MISSING_MODULE_JPYUTIL
    else:
        logging.info("jpy is already configured")

    #
    # If snappy isn't configured yet, do so by writing a default "snappy.ini".
    # Note, this file is only used if you use SNAP from Python, i.e. importing
    # the snappy module in your Python programs.
    #
    if must_configure_snappy:
        logging.info("Configuring snappy...")
        with open(snappy_ini_file, 'w') as file:
            file.writelines(['[DEFAULT]\n',
                             'snap_home = %s\n' % snap_home,
                             'java_max_mem: %s\n' % jvm_max_mem,
                             '# snap_start_engine: False\n',
                             '# java_class_path: ./target/classes\n',
                             '# java_library_path: ./lib\n',
                             '# java_options: -Djava.awt.headless=false\n',
                             '# debug: False\n'])
            logging.info("snappy configuration written to '{}'".format(snappy_ini_file))
    else:
        logging.info("snappy is already configured")

    #
    # Finally, we test the snappy installation/configuration by importing it.
    # If this won't succeed, _main() will catch the error and report it.
    #
    logging.info("Importing esa_snappy.snapista for final test...")
    sys.path = [os.path.join(snappy_dir, '..')] + sys.path

    try:
        __import__('esa_snappy.snapista')
    except:
        return _ERR_CODE_IMPORTING_SNAPISTA_FAILED

    logging.info("Done. The SNAP-Python interface is located as package 'esa_snappy' in "
                 "your Python's 'site-packages' directory..\n")

    return 0


def _main():
    parser = argparse.ArgumentParser(description='Configures snappy, the SNAP-Python interface.')
    parser.add_argument('--snap_home', default=None,
                        help='SNAP distribution directory')
    parser.add_argument('--req_arch', default=None,
                        help='required JVM architecture, e.g. "amd64", '
                             'may be taken from Java system property "os.arch"')
    parser.add_argument('--java_module', default=None,
                        help='directory or JAR file containing the "snap-python" Java module')
    parser.add_argument('--java_home', default=None,
                        help='Java JDK or JRE installation directory, '
                             'may be taken from Java system property "java.home"')
    parser.add_argument('--jvm_max_mem', default='3G', help='size of the Java VM heap space')
    parser.add_argument("--log_file", action='store', default=None, help="file into which to write logging output")
    parser.add_argument("--log_level", action='store', default='INFO',
                        help="log level, possible values are: DEBUG, INFO, WARNING, ERROR")
    parser.add_argument("-j", "--req_java", action='store_true', default=False,
                        help="require that Java API configuration succeeds")
    parser.add_argument("-p", "--req_py", action='store_true', default=False,
                        help="require that Python API configuration succeeds")
    parser.add_argument('-f', '--force', action='store_true', default=False,
                        help='force overwriting of existing files')
    args = parser.parse_args()

    log_level = getattr(logging, args.log_level.upper(), None)
    if not isinstance(log_level, int):
        raise ValueError('Invalid log level: %s' % log_level)

    log_format = '%(levelname)s: %(message)s'
    log_file = args.log_file
    if log_file:
        logging.basicConfig(format=log_format, level=log_level, filename=log_file, filemode='w')
    else:
        logging.basicConfig(format=log_format, level=log_level)

    # noinspection PyBroadException
    try:
        ret_code = _configure_snappy(snap_home=args.snap_home,
                                     java_module=args.java_module,
                                     java_home=args.java_home,
                                     jvm_max_mem=args.jvm_max_mem,
                                     req_arch=args.req_arch,
                                     req_java=args.req_java,
                                     req_py=args.req_py,
                                     force=args.force)
    except:
        ret_code = _ERR_CODE_INTERNAL_ERROR
        logging.error(traceback.format_exc())

    if ret_code != 0:
        logging.error("Configuration failed with exit code {}".format(ret_code))

    exit(ret_code)


if __name__ == '__main__':
    _main()
