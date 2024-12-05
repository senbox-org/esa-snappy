import unittest

import os
import sys
import glob

# import java

EXCLUDED_NB_CLUSTERS = {'platform', 'ide', 'bin', 'etc'}

EXCLUDED_DIR_NAMES = {'org.esa.snap.snap-worldwind', 'org.esa.snap.snap-rcp', 'org.esa.snap.snap-product-library',
                      'org.esa.snap.snap-sta-ui'}

EXCLUDED_JAR_NAMES = {'org-esa-snap-netbeans-docwin.jar', 'org-esa-snap-netbeans-tile.jar',
                      'org-esa-snap-snap-worldwind.jar', 'org-esa-snap-snap-tango.jar', 'org-esa-snap-snap-rcp.jar',
                      'org-esa-snap-snap-ui.jar', 'org-esa-snap-snap-graph-builder.jar',
                      'org-esa-snap-snap-branding.jar'}

class MyTestCase(unittest.TestCase):
    def test_something(self):
        self.assertEqual(True, True)  # add assertion here

    def test_primitive_type(self):
        self.assertEqual(True, True)  # add assertion here

        array = java.type("int[]")(4)
        array[2] = 42
        self.assertEqual(42, array[2])

    def test_java_class_from_snap_core(self):
        rsmu = java.type('org.esa.snap.core.util.math.RsMathUtils')
        self.assertAlmostEqual(57.2958, rsmu.DEG_PER_RAD, delta=1.E-4)


    def test_get_snap_jvm_env(self):
        snap_home="C:\Program Files\esa-snap"

        env = self._get_snap_jvm_env(snap_home)
        class_path_entries = list(env[0].values())
        class_path = os.pathsep.join(class_path_entries)
        print('classpath: ' + class_path)


    def _get_snap_jvm_env(self, snap_home):
        module_dir = os.path.dirname(os.path.realpath(__file__))

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
        nb_user_modules_dir = self._get_nb_user_modules_dir()
        if nb_user_modules_dir and os.path.isdir(nb_user_modules_dir):
            java_module_dirs.append(nb_user_modules_dir)
    
            print(module_dir + ': java_module_dirs = ')
    
        env = (dict(), [])
        for path in java_module_dirs:
            self._collect_snap_jvm_env(path, env)

        print(module_dir + ': env =')

        return env

    def _get_nb_user_modules_dir(self):
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

    def _collect_snap_jvm_env(self, dir_path, env):
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
                    self._collect_snap_jvm_env(path, env)

if __name__ == '__main__':
    unittest.main()
