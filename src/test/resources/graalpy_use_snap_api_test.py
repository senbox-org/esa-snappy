import argparse
import os
import glob

import java

import numpy as np
import matplotlib.pyplot as plt

EXCLUDED_NB_CLUSTERS = {'platform', 'ide', 'bin', 'etc'}

EXCLUDED_DIR_NAMES = {'org.esa.snap.snap-worldwind', 'org.esa.snap.snap-rcp', 'org.esa.snap.snap-product-library',
                      'org.esa.snap.snap-sta-ui'}

EXCLUDED_JAR_NAMES = {'org-esa-snap-netbeans-docwin.jar', 'org-esa-snap-netbeans-tile.jar',
                      'org-esa-snap-snap-worldwind.jar', 'org-esa-snap-snap-tango.jar', 'org-esa-snap-snap-rcp.jar',
                      'org-esa-snap-snap-ui.jar', 'org-esa-snap-snap-graph-builder.jar',
                      'org-esa-snap-snap-branding.jar'}


def _get_snap_jvm_env(snap_home):
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
    nb_user_modules_dir = _get_nb_user_modules_dir()
    if nb_user_modules_dir and os.path.isdir(nb_user_modules_dir):
        java_module_dirs.append(nb_user_modules_dir)

        print(module_dir + ': java_module_dirs = ')

    env = (dict(), [])
    for path in java_module_dirs:
        _collect_snap_jvm_env(path, env)

    print(module_dir + ': env =')

    return env

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


def _main():
    """
    Tests access of SNAP API from graalpy. We no longer need jpy!
    :return:
    """
    parser = argparse.ArgumentParser(description='Configures snappy, the SNAP-Python interface.')
    parser.add_argument('--input_product_path', default=None, help='SNAP input test product path')
    parser.add_argument('--snap_home', default=None, help='SNAP installation dir')
    args = parser.parse_args()

    product_path = args.input_product_path
    snap_home = args.snap_home

    # Test: Get the Java classpath and print it
    System = java.type("java.lang.System")
    classpath = System.getProperty("java.class.path")
    print(f"Java Classpath: {classpath}")

    # Collect jars to put on classpath (taken from old __init.py__)
    env = _get_snap_jvm_env(snap_home)

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

    EngineConfig.instance().load()
    SystemUtils.init3rdPartyLibs(None)
    Engine.start()

    ### FINISHED preparations - now test something...

    # Test: access Java BigInteger
    BigInteger = java.type("java.math.BigInteger")
    myBigInt = BigInteger.valueOf(42)
    print(str(myBigInt))

    # Test: access Java RsMathUtils from SNAP API, access public class constant and use static method:
    print('deg2rad: ' + str(RsMathUtils.DEG_PER_RAD))
    print('elevationToZenith: ' + str(RsMathUtils.elevationToZenith(60.0)))

    # Test: access Java GeoPos from SNAP API, use non-static method:
    gp = GeoPos(30.0, 45.0)
    print('geopos: ' + str(gp.getLat()) + ', ' + str(gp.getLon()))

    # Test: set up a Java Product from SNAP API
    p = Product("bla", "blubb", 3, 3)
    print('product name = ' + p.getName())
    print('product type = ' + p.getProductType())
    w = p.getSceneRasterWidth()
    h = p.getSceneRasterHeight()
    print('scene raster width = ' + str(w))
    print('scene raster height = ' + str(h))

    # Test: access Product via ProductIO from SNAP API
    p = ProductIO.readProduct(product_path)
    name = p.getName()
    print('product name = ' + name)
    rad13 = p.getBand('radiance_13')
    w = rad13.getRasterWidth()
    h = rad13.getRasterHeight()
    print('radiance_13 raster width = ' + str(w))
    print('radiance_13 raster height = ' + str(h))
    w = p.getSceneRasterWidth()
    h = p.getSceneRasterHeight()
    print('scene raster width = ' + str(w))
    print('scene raster height = ' + str(h))

    rad13_data = np.zeros(w * h, np.float32)
    rad13.readPixels(0, 0, w, h, rad13_data)
    p.dispose()
    rad13_data.shape = h, w
    imgplot = plt.imshow(rad13_data)
    imgplot.write_png('radiance_13.png')

if __name__ == '__main__':
    _main()