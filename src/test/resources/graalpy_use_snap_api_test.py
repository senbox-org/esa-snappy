import argparse
import os

import java

from pathlib import Path

# import numpy as np
# import matplotlib.pyplot as plt

def list_files_recursive(path='.'):
    for entry in os.listdir(path):
        full_path = os.path.join(path, entry)
        if os.path.isdir(full_path):
            list_files_recursive(full_path)
        else:
            if full_path.endswith(".jar"):
                java.add_to_classpath(full_path)
                print(full_path)

def _main():
    print('hier')
    parser = argparse.ArgumentParser(description='Configures snappy, the SNAP-Python interface.')
    parser.add_argument('--input_product_path', default=None, help='SNAP input test product path')
    parser.add_argument('--snap_home', default=None, help='SNAP installation dir')
    args = parser.parse_args()

    product_path = args.input_product_path
    snap_home = args.snap_home

    directory_path = snap_home + '//snap//modules'
    # directory_path = snap_home
    list_files_recursive(directory_path)

    ProductIO = java.type('org.esa.snap.core.dataio.ProductIO')
    print('hier 2')
    EngineConfig = java.type('org.esa.snap.runtime.EngineConfig')
    Engine = java.type('org.esa.snap.runtime.Engine')
    SystemUtils = java.type('org.esa.snap.core.util.SystemUtils')

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
    # EngineConfig = java.type('org.esa.snap.runtime.EngineConfig')
    # Engine = java.type('org.esa.snap.runtime.Engine')
    SystemUtils = java.type('org.esa.snap.core.util.SystemUtils')
    ProductIO = java.type('org.esa.snap.core.dataio.ProductIO')
    ProductUtils = java.type('org.esa.snap.core.util.ProductUtils')
    GeoUtils = java.type('org.esa.snap.core.util.GeoUtils')
    ProgressMonitor = java.type('com.bc.ceres.core.ProgressMonitor')
    PlainFeatureFactory = java.type('org.esa.snap.core.datamodel.PlainFeatureFactory')
    FeatureUtils = java.type('org.esa.snap.core.util.FeatureUtils')
    print('hier 2')

    # GeoTools
    DefaultGeographicCRS = java.type('org.geotools.referencing.crs.DefaultGeographicCRS')
    ListFeatureCollection = java.type('org.geotools.data.collection.ListFeatureCollection')
    SimpleFeatureBuilder = java.type('org.geotools.feature.simple.SimpleFeatureBuilder')

    # JTS
    Geometry = java.type('org.locationtech.jts.geom.Geometry')
    WKTReader = java.type('org.locationtech.jts.io.WKTReader')

    EngineConfig.instance().load()
    # SystemUtils.init3rdPartyLibs(None)
    Engine.start()

    # p = ProductIO.readProduct(product_path)
    p = Product("bla", "blubb", 3, 3)
    print('hier 3')
    # rad13 = p.getBand('radiance_13')
    # w = rad13.getRasterWidth()
    # w = p.getSceneRasterWidth()
    # h = rad13.getRasterHeight()
    # h = p.getSceneRasterHeight()
    # print('w = ' + str(w))
    # print('h = ' + str(h))

if __name__ == '__main__':
    _main()