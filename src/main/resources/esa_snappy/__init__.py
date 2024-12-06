"""
The esa_snappy module provides access to the Java SE APIs and SNAP Java APIs.

You can configure esa_snappy by using a file named snappy.ini as follows:

    [DEFAULT]
    snap_home: C:/Program Files/snap
    java_class_path: target/classes
    java_library_path: target/lib/amd64
    java_options: -Djava.awt.headless=true
    java_max_mem: 4G
    debug: False

You can place snappy.ini next to <python3>/site-packages/snappy.py or put it in your current working directory.
The 'snap_home' options and all options starting with 'java_' are only used if you use the SNAP API from Python
and a new Java Virtual Machine is created. They are ignored if the SNAP API is called from SNAP itself
(e.g. SNAP command-line or GUI).

'esa_snappy' uses a bundled 'jpy' module, see documentation at http://jpy.readthedocs.org/en/latest
and source code at https://github.com/bcdev/jpy
"""

EXCLUDED_NB_CLUSTERS = {'platform', 'ide', 'bin', 'etc'}

EXCLUDED_DIR_NAMES = {'org.esa.snap.snap-worldwind', 'org.esa.snap.snap-rcp', 'org.esa.snap.snap-product-library',
                      'org.esa.snap.snap-sta-ui'}

EXCLUDED_JAR_NAMES = {'org-esa-snap-netbeans-docwin.jar', 'org-esa-snap-netbeans-tile.jar',
                      'org-esa-snap-snap-worldwind.jar', 'org-esa-snap-snap-tango.jar', 'org-esa-snap-snap-rcp.jar',
                      'org-esa-snap-snap-ui.jar', 'org-esa-snap-snap-graph-builder.jar',
                      'org-esa-snap-snap-branding.jar'}

import glob
import os

import sys

if sys.version_info >= (3, 0, 0,):
    # noinspection PyUnresolvedReferences
    import configparser as cp
else:
    # noinspection PyUnresolvedReferences
    import ConfigParser as cp

module_dir = os.path.dirname(os.path.realpath(__file__))
module_ini = os.path.basename(module_dir) + '.ini'

# Read configuration *.ini file from either '.', '<module_dir>/..', '<module_dir>' in this order
config = cp.ConfigParser()
config.read([os.path.join(module_dir, module_ini),
             os.path.join(os.path.join(module_dir, '..'), module_ini),
             module_ini
             ])

debug = False
if config.has_option('DEFAULT', 'debug'):
    debug = config.getboolean('DEFAULT', 'debug')

# Pre-load Java VM shared library and import 'jpy', the Java-Python bridge
if module_dir not in sys.path:
    sys.path.append(module_dir)
    
import java

#
# Recursively searches for JAR files in 'dir_path' and appends them to the first member of the
# 'env' tuple (env[0] is the classpath). Also searches for 'lib/LIB_PLATFORM_NAMES[i]' and if one
# exists, appends it to the second member of the 'env' tuple (env[1] is the java.library.path)
# Note: Sub-directories named 'locale' or 'docs' are not searched and
#       JAR files ending with '-ui.jar' or '-examples.jar' are excluded.
#
# Note: This function is called only if the 'esa_snappy' module is not imported from Java.
#
def _collect_snap_jvm_env(dir_path, env):
    for name in os.listdir(dir_path):
        path = os.path.join(dir_path, name)
        if os.path.isfile(path) and name.endswith('.jar'):
            if not (name.endswith('-ui.jar') or name in EXCLUDED_JAR_NAMES):
                env[0][name] = path
        elif os.path.isdir(path) and name not in EXCLUDED_DIR_NAMES:
            if name == 'lib':
                import platform

                os_arch = platform.machine().lower()
                os_name = platform.system().lower()
                lib_os_arch_path = os.path.join(path, os_arch)
                if os.path.exists(lib_os_arch_path):
                    lib_os_name_path = os.path.join(lib_os_arch_path, os_name)
                    if os.path.exists(lib_os_name_path):
                        env[1].append(lib_os_name_path)
                    env[1].append(lib_os_arch_path)
                env[1].append(path)
            if not (name == 'locale' or name == 'docs'):
                _collect_snap_jvm_env(path, env)


#
# Get the NetBeans user directory for installed extra modules
#
def _get_nb_user_modules_dir():
    import platform
    from os.path import expanduser

    home_dir = expanduser('~')
    nb_user_dir = None
    if platform.system() == 'Windows':
        if home_dir:
            nb_user_dir = os.path.join(home_dir, 'AppData\\Roaming\\SNAP')
    else:
        if home_dir:
            nb_user_dir = os.path.join(home_dir, '.snap/system')

    if nb_user_dir:
        return os.path.join(nb_user_dir, 'modules')

    return None

#
# Searches for *.jar files in directory given by global 'snap_home' variable and returns them as a list.
#
# Note: This function is called only if the 'esa_snappy' module is not imported from Java.
#
def _get_snap_jvm_env():
    dir_names = []
    for name in os.listdir(snap_home):
        if os.path.isdir(os.path.join(snap_home, name)):
            dir_names.append(name)

    java_module_dirs = []

    if 'bin' in dir_names and 'etc' in dir_names and 'snap' in dir_names:
        # SNAP Desktop Distribution Directory
        for dir_name in dir_names:
            if dir_name not in EXCLUDED_NB_CLUSTERS:
                dir_path = os.path.join(snap_home, dir_name, 'modules')
                if os.path.isdir(dir_path):
                    java_module_dirs.append(dir_path)
    elif 'lib' in dir_names and 'modules' in dir_names:
        # SNAP Engine Distribution Directory
        java_module_dirs = [os.path.join(snap_home, 'modules'), os.path.join(snap_home, 'lib')]
    elif glob.glob(snap_home + '/*snap-python*.jar'):
        java_module_dirs = [snap_home]
    else:
        raise RuntimeError('does not seem to be a valid SNAP distribution directory: ' + snap_home)

    # NetBeans modules dir will be scaned as last. It contains the latest module updates and they shall replace
    # older modules
    nb_user_modules_dir = _get_nb_user_modules_dir()
    if nb_user_modules_dir and os.path.isdir(nb_user_modules_dir):
        java_module_dirs.append(nb_user_modules_dir)

    if debug:
        import pprint

        print(module_dir + ': java_module_dirs = ')
        pprint.pprint(java_module_dirs)

    env = (dict(), [])
    for path in java_module_dirs:
        _collect_snap_jvm_env(path, env)

    if debug:
        import pprint

        print(module_dir + ': env =')
        pprint.pprint(env)

    return env


#
# Creates a list of Java JVM options, including the Java classpath derived from the global 'snap_home' variable.
#
# Note: This function is called only if the 'esa_snappy' module is not imported from Java.
#
# def _get_snap_jvm_options():
#     global snap_home
#
#     if config.has_option('DEFAULT', 'snap_home'):
#         snap_home = config.get('DEFAULT', 'snap_home')
#     else:
#         snap_home = os.getenv('SNAP_HOME')
#
#     if snap_home is None or not os.path.isdir(snap_home):
#         raise IOError("Can't find SNAP distribution directory. Either configure variable 'snap_home' " +
#                       "in file './snappy.ini' or set environment variable 'SNAP_HOME' to an " +
#                       "existing SNAP distribution directory.")
#
#     env = _get_snap_jvm_env()
#     class_path = list(env[0].values())
#     library_path = env[1]
#
#     if config.has_option('DEFAULT', 'java_class_path'):
#         extra_class_path = config.get('DEFAULT', 'java_class_path')
#         class_path += extra_class_path.split(os.pathsep)
#
#     if config.has_option('DEFAULT', 'java_library_path'):
#         extra_library_path = config.get('DEFAULT', 'java_library_path')
#         library_path += extra_library_path.split(os.pathsep)
#
#     max_mem = '512M'
#     if config.has_option('DEFAULT', 'java_max_mem'):
#         max_mem = config.get('DEFAULT', 'java_max_mem')
#
#     options = ['-Djava.awt.headless=true',
#                '-Djava.class.path=' + os.pathsep.join(class_path),
#                '-Djava.library.path=' + os.pathsep.join(library_path),
#                '-Dsnap.home=' + snap_home,
#                '-Xmx' + max_mem]
#
#     if config.has_option('DEFAULT', 'java_options'):
#         extra_options = config.get('DEFAULT', 'java_options')
#         options += extra_options.split('|')
#
#     return options


# Figure out if this module is called from a Java VM. If not, derive a list of Java VM options and create the Java VM.
# called_from_java = jpy.has_jvm()
# if not called_from_java:
#     jpy.create_jvm(options=_get_snap_jvm_options())


# Don't need these functions anymore
# del _get_snap_jvm_options
# del _get_snap_jvm_env
# del _collect_snap_jvm_env


# noinspection PyUnusedLocal
# def annotate_RasterDataNode_methods(type_name, method):
#     index = -1
#
#     if sys.version_info >= (3, 0, 0,):
#         arr_z_type_str = "<class '[Z'>"
#         arr_i_type_str = "<class '[I'>"
#         arr_f_type_str = "<class '[F'>"
#         arr_d_type_str = "<class '[D'>"
#     else:
#         arr_z_type_str = "<type '[Z'>"
#         arr_i_type_str = "<type '[I'>"
#         arr_f_type_str = "<type '[F'>"
#         arr_d_type_str = "<type '[D'>"
#
#     if method.name == 'readPixels' and method.param_count >= 5:
#         index = 4
#         param_type_str = str(method.get_param_type(index))
#         if param_type_str == arr_i_type_str \
#                 or param_type_str == arr_f_type_str \
#                 or param_type_str == arr_d_type_str:
#             method.set_param_mutable(index, True)
#             method.set_param_output(index, True)
#             method.set_param_return(index, True)
#
#     if method.name == 'readValidMask' and method.param_count == 5:
#         index = 4
#         param_type_str = str(method.get_param_type(index))
#         if param_type_str == arr_z_type_str:
#             method.set_param_mutable(index, True)
#             method.set_param_output(index, True)
#             method.set_param_return(index, True)
#
#     if index >= 0 and debug:
#         print('annotate_RasterDataNode_methods: Method "{0}": '
#               'modified parameter {1:d}: mutable = {2}, return = {3}'
#               .format(method.name, index, method.is_param_mutable(index), method.is_param_return(index)))
#
#     return True


# jpy.type_callbacks['org.esa.snap.core.datamodel.RasterDataNode'] = annotate_RasterDataNode_methods
# jpy.type_callbacks['org.esa.snap.core.datamodel.AbstractBand'] = annotate_RasterDataNode_methods
# jpy.type_callbacks['org.esa.snap.core.datamodel.Band'] = annotate_RasterDataNode_methods
# jpy.type_callbacks['org.esa.snap.core.datamodel.VirtualBand'] = annotate_RasterDataNode_methods

def setup_graalpy_java_classpath(path='.'):
    for entry in os.listdir(path):
        full_path = os.path.join(path, entry)
        if os.path.isdir(full_path):
            # print('setup_graalpy_java_classpath: Scanning directory ' + full_path + 'for jars...')
            setup_graalpy_java_classpath(full_path)
        else:
            if full_path.endswith(".jar"):
                java.add_to_classpath(full_path)
                # print(full_path)

#
# Preload and assign frequently used Java classes from the Java SE and SNAP Java API.
#

try:

    if config.has_option('DEFAULT', 'snap_home'):
        snap_home = config.get('DEFAULT', 'snap_home')
    else:
        snap_home = os.getenv('SNAP_HOME')

    if snap_home is None or not os.path.isdir(snap_home):
        raise IOError("Can't find SNAP distribution directory. Either configure variable 'snap_home' " +
                      "in file './snappy.ini' or set environment variable 'SNAP_HOME' to an " +
                      "existing SNAP distribution directory.")

    ### START initialization...

    # Collect jars to put on classpath (taken from old __init.py__)
    env = _get_snap_jvm_env()

    # now add classpath entries in the 'graalpy way':
    class_path_entries = list(env[0].values())
    for entry in class_path_entries:
        # print('jar: ' + entry)
        java.add_to_classpath(entry)

    #
    # Frequently used classes from Java API
    #
    String = java.type('java.lang.String')
    File = java.type('java.io.File')
    Point = java.type('java.awt.Point')
    Rectangle = java.type('java.awt.Rectangle')
    Arrays = java.type('java.util.Arrays')
    Collections = java.type('java.util.Collections')
    List = java.type('java.util.List')
    Map = java.type('java.util.Map')
    Set = java.type('java.util.Set')
    ArrayList = java.type('java.util.ArrayList')
    HashMap = java.type('java.util.HashMap')
    HashSet = java.type('java.util.HashSet')

    #
    # Frequently used classes & interfaces from SNAP Engine
    #

    # Product tree & associates
    Product = java.type('org.esa.snap.core.datamodel.Product')
    VectorDataNode = java.type('org.esa.snap.core.datamodel.VectorDataNode')
    RasterDataNode = java.type('org.esa.snap.core.datamodel.RasterDataNode')
    TiePointGrid = java.type('org.esa.snap.core.datamodel.TiePointGrid')
    AbstractBand = java.type('org.esa.snap.core.datamodel.AbstractBand')
    Band = java.type('org.esa.snap.core.datamodel.Band')
    VirtualBand = java.type('org.esa.snap.core.datamodel.VirtualBand')
    Mask = java.type('org.esa.snap.core.datamodel.Mask')
    GeneralFilterBand = java.type('org.esa.snap.core.datamodel.GeneralFilterBand')
    ConvolutionFilterBand = java.type('org.esa.snap.core.datamodel.ConvolutionFilterBand')

    # Product tree associates
    ProductData = java.type('org.esa.snap.core.datamodel.ProductData')
    GeoCoding = java.type('org.esa.snap.core.datamodel.GeoCoding')
    TiePointGeoCoding = java.type('org.esa.snap.core.datamodel.TiePointGeoCoding')
    ComponentGeoCoding = java.type('org.esa.snap.core.dataio.geocoding.ComponentGeoCoding')
    CrsGeoCoding = java.type('org.esa.snap.core.datamodel.CrsGeoCoding')
    GeoPos = java.type('org.esa.snap.core.datamodel.GeoPos')
    PixelPos = java.type('org.esa.snap.core.datamodel.PixelPos')
    FlagCoding = java.type('org.esa.snap.core.datamodel.FlagCoding')
    ProductNodeGroup = java.type('org.esa.snap.core.datamodel.ProductNodeGroup')

    # Graph Processing Framework
    GPF = java.type('org.esa.snap.core.gpf.GPF')
    Operator = java.type('org.esa.snap.core.gpf.Operator')
    Tile = java.type('org.esa.snap.core.gpf.Tile')

    # Utilities
    EngineConfig = java.type('org.esa.snap.runtime.EngineConfig')
    Engine = java.type('org.esa.snap.runtime.Engine')
    SystemUtils = java.type('org.esa.snap.core.util.SystemUtils')
    ProductIO = java.type('org.esa.snap.core.dataio.ProductIO')
    ProductUtils = java.type('org.esa.snap.core.util.ProductUtils')
    GeoUtils = java.type('org.esa.snap.core.util.GeoUtils')
    RsMathUtils = java.type('org.esa.snap.core.util.math.RsMathUtils')
    ProgressMonitor = java.type('com.bc.ceres.core.ProgressMonitor')
    PlainFeatureFactory = java.type('org.esa.snap.core.datamodel.PlainFeatureFactory')
    FeatureUtils = java.type('org.esa.snap.core.util.FeatureUtils')
    JavaTypeConverter = java.type('org.esa.snap.core.util.converters.JavaTypeConverter')
    ClassConverter = java.type('com.bc.ceres.binding.converters.ClassConverter')

    # GeoTools
    DefaultGeographicCRS = java.type('org.geotools.referencing.crs.DefaultGeographicCRS')
    ListFeatureCollection = java.type('org.geotools.data.collection.ListFeatureCollection')
    SimpleFeatureBuilder = java.type('org.geotools.feature.simple.SimpleFeatureBuilder')
    # JTS
    Geometry = java.type('org.locationtech.jts.geom.Geometry')
    WKTReader = java.type('org.locationtech.jts.io.WKTReader')
    JAI = java.type('javax.media.jai.JAI')

    # Note: use the following code to initialise SNAP's 3rd party libraries, JAI and GeoTools.
    # Only needed, if SNAP Python API is not called from Java (e.g. from SNAP gpt or SNAP desktop).
    called_from_java = False  # todo: how to determine this for Graal? Needed at all?
    if not called_from_java:
        EngineConfig.instance().load()
        SystemUtils.init3rdPartyLibs(None)

        start_snap_engine = True
        if config.has_option('DEFAULT', 'snap_start_engine'):
            start_snap_engine = config.getboolean('DEFAULT', 'snap_start_engine')

        if start_snap_engine:
            Engine.start()

    # Don't need these functions anymore
    del _get_nb_user_modules_dir
    del _get_snap_jvm_env
    del _collect_snap_jvm_env

    ### FINISHED initialization.

except Exception:
    # jpy.destroy_jvm()
    raise


