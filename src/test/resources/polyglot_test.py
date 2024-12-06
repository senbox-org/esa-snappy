import java

array = java.type("int[]")(4)
array[2] = 42
print(array[2])

ArrayList = java.type('java.util.ArrayList')
my_list = ArrayList()
assert java.is_symbol(ArrayList)
assert not java.is_symbol(my_list)
assert java.is_object(ArrayList)
assert java.is_function(my_list.add)
assert java.instanceof(my_list, ArrayList)

# jstring = java.type('java.lang.String')("bla")
# jstringU = java.type('java.lang.String')(jstring.toUpperCase())
# print('string: ' + jstring)
# l = jstring.length()
# print('stringlength: ' + str(l))


snap_home = 'D://olaf//bc//snap-snapshots//12//12.0-SNAPSHOT'

System = java.type("java.lang.System")
java.add_to_classpath(snap_home + '//snap//modules//org-esa-snap-snap-core.jar')
classpath = System.getProperty("java.class.path") + ';' + snap_home + '//snap//modules//org-esa-snap-snap-core.jar'
System.setProperty("java.class.path", classpath)
java.add_to_classpath(snap_home + '//snap//modules//org-esa-snap-ceres-core.jar')
java.add_to_classpath(snap_home + '//snap//modules//org-esa-snap-snap-gpf.jar')
java.add_to_classpath(snap_home + '//snap//modules//org.esa.snap.snap-core//org-geotools//gt-referencing.jar')
java.add_to_classpath(snap_home + '//snap//modules//org.esa.snap.snap-core//org-geotools//gt-main.jar')
java.add_to_classpath(snap_home + '//snap//modules//org.esa.snap.snap-core//org-locationtech-jts//jts-core.jar')
java.add_to_classpath(snap_home + '//snap//modules//org.esa.snap.snap-core//org-geotools//gt-opengis.jar')
java.add_to_classpath(snap_home + '//snap//modules//org-esa-snap-ceres-glayer.jar')
java.add_to_classpath(snap_home + '//snap//modules//org.esa.snap.ceres-jai//javax-media-jai//jai-core-openjdk.jar')
java.add_to_classpath('C://Users//olafd//.m2//repository//javax//media//jai_core//1.1.3//jai_core-1.1.3.jar')

classpath = System.getProperty("java.class.path")
print(f"Java Classpath: {classpath}")

rsmu = java.type('org.esa.snap.core.util.math.RsMathUtils')
print('deg2rad: ' + str(rsmu.DEG_PER_RAD))
print('elevationToZenith: ' + str(rsmu.elevationToZenith(60.0)))

print('hier 1')

GeoPos = java.type('org.esa.snap.core.datamodel.GeoPos')

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
MultiLevelModel = java.type('com.bc.ceres.glevel.MultiLevelModel')

# Graph Processing Framework
GPF = java.type('org.esa.snap.core.gpf.GPF')
Operator = java.type('org.esa.snap.core.gpf.Operator')
Tile = java.type('org.esa.snap.core.gpf.Tile')

# Utilities
# EngineConfig = java.type('org.esa.snap.runtime.EngineConfig')
print('hier 2')
# Engine = java.type('org.esa.snap.runtime.Engine')
SystemUtils = java.type('org.esa.snap.core.util.SystemUtils')
ProductIO = java.type('org.esa.snap.core.dataio.ProductIO')
ProductUtils = java.type('org.esa.snap.core.util.ProductUtils')
GeoUtils = java.type('org.esa.snap.core.util.GeoUtils')
ProgressMonitor = java.type('com.bc.ceres.core.ProgressMonitor')
PlainFeatureFactory = java.type('org.esa.snap.core.datamodel.PlainFeatureFactory')
FeatureUtils = java.type('org.esa.snap.core.util.FeatureUtils')

# GeoTools
CoordinateReferenceSystem = java.type('org.opengis.referencing.crs.CoordinateReferenceSystem')
DefaultGeographicCRS = java.type('org.geotools.referencing.crs.DefaultGeographicCRS')
ListFeatureCollection = java.type('org.geotools.data.collection.ListFeatureCollection')
SimpleFeatureBuilder = java.type('org.geotools.feature.simple.SimpleFeatureBuilder')

# JTS
Geometry = java.type('org.locationtech.jts.geom.Geometry')
WKTReader = java.type('org.locationtech.jts.io.WKTReader')

print('Finished preloading Java types.')
