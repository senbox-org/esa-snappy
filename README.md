esa-snappy
==========

[![pipeline status](https://gitlab.com/senbox-org/esa-snappy/badges/master/pipeline.svg)](https://gitlab.com/senbox-org/esa-snappy/-/commits/master)
[![coverage report](https://gitlab.com/senbox-org/esa-snappy/badges/master/coverage.svg)](https://gitlab.com/senbox-org/esa-snappy/-/commits/master)

The plugin `esa-snappy` enables Python developers to

1. use the SNAP Java API from Python, and to
2. extend SNAP by *operator plug-ins* for EO data processing written in the Python programming language.

It is worth mentioning that the `esa-snappy` plugin works with the standard *CPython*, so
that any native Python extension modules such as `numpy` and `scipy` can be used. Before you
read further you may have a look at the example code in `examples/*.py` for using the SNAP
Java API from Python.

The esa_snappy plugin
depends on a *bi-directional* Java-Python bridge *jpy* that enables calls from Python into a Java virtual machine
and, at the same time, the other way round. This bridge is implemented by the
[jpy Project](https://github.com/jpy-consortium/jpy) and is independent of the snappy module.

Until SNAP version 9, the functionality was realized by an internal module 'snap-python' in snap-engine,
finally providing a Python package named *snappy*, but this lead to conflicts as other common Python
packages exist with this name. E.g., various state-of-the-art Python packages (such as xarray, dask) could not be used.
Moreover, the *snappy* package could only be used with Python versions <= 3.6. The new *esa_snappy* plugin now
provides a Python package with the same name *esa_snappy* which, along with the current jpy version, 
supports the more recent Python versions 3.6 to 3.10.

Before you can start using the SNAP API or developing SNAP operator plugins with Python you
need to install *esa_snappy* in SNAP and configure SNAP for the desired Python version. This procedure
is described in the following.

Installation
------------

With the new *esa_snappy* plugin, a manual installation of jpy is no longer necessary,
as jpy wheels with the required shared libraries for all supported
Python versions and platforms are now included in the plugin. The jpy installation is done
automatically as part of the *esa_snappy* installation and Python configuration, which
in return can be done either

* from the command line using a configuration script which is provided within the SNAP installation 
(as for *snappy* in earlier SNAP versions)
* from a GUI as an optional step of the SNAP installation itself (as for *snappy* in earlier SNAP versions)
* from a GUI which can be invoked from SNAP Desktop if SNAP is already installed (in preparation, not yet functional)

### Installation from the command line

On Darwin / Linux type:

    cd <path to your SNAP installation>/bin
    ./snappy-conf <path/to/your/Python/installation>/bin/python [esa_snappy installation dir]

On Windows type:

    cd <path to your SNAP installation>\bin
    .\snappy-conf.bat <path\to\your\Python\installation>\python.exe [esa_snappy installation dir]

If no *esa_snappy* installation directory is specified, the installation will be done into
<'user home directory'>/.snap/snap-python. This is likely the most common case.

If you encounter any problems during the *esa_snappy* installation, please do not hesitate to contact the
[SNAP user forum](https://forum.step.esa.int/).

### Installation from GUI during the SNAP installation

During the SNAP installation, a dedicated screen for the Python configuration will appear. 
Follow the instructions given there.

### Installation from GUI within SNAP Desktop

#### in preparation, not yet functional

Testing
-------

When *esa_snappy* is imported into your Python script or module, it will scan a SNAP installation
for the available SNAP API components. To test if the import works, make sure that your Python
executable is on your path, and then type

On Darwin / Linux:

    python
    >>> import sys
    >>> sys.path.append('<esa_snappy installation dir>')
    >>> import esa_snappy

On Windows:

    python.exe
    >>> import sys
    >>> sys.path.append('<esa_snappy installation dir>')
    >>> import esa_snappy

If the import is successful (no errors are raised) you can exit the Python interpreter and
perform the test cases in the '<esa_snappy installation dir>/esa_snappy/tests' directory:


    python test_snappy_mem.py
    python test_snappy_perf.py
    python test_snappy_product.py

These tests expect [numpy](http://www.numpy.org/) to be installed in your Python.
They all require an EO data test product file as input named `MER_FRS_L1B_SUBSET.dim`,
which is an Envisat MERIS L1b product. This product is located in the
'<esa_snappy installation dir>/esa_snappy/testdata' directory.

Please note that the SNAP API is actually independent of specific data formats, the MERIS file in this case
is only used as an example and for testing.

Known issues
------------

#### Configuration failed with exit code 30

This error has been occasionally observed during the *esa_snappy* installation and Python
configuration on Linux. Here, the environment variable LD_LIBRARY_PATH is likely not set
correctly, and thus the shared library for the JVM cannot be found. This can be solved by
performing the following steps:

* `locate libjvm.so`
* Output is, say, `/path/to/libjvm.so`
* `export LD_LIBRARY_PATH=/path/to/:${LD_LIBRARY_PATH}`
* Re-try the installation

#### Cannot open shared object

After a successful *esa_snappy* installation and Python configuration, you might get an error
similar to
`ImportError: libjvm.so: cannot open shared object file: No such file or directory`
when doing `import jpy` within your Python script.
Again, the shared library for the JVM cannot be found. This might happen if the
LD_LIBRARY_PATH has previously been set correctly, but was changed or not set
permanently. In this case, set the LD_LIBRARY_PATH as described above and restart your Python script.


Configuration
-------------

*esa_snappy* can be configured by an *INI file* `snappy.ini`. This file is read from the current working directory
or from the system-dependent location from which the installed Python *esa_snappy* module is loaded from.

Given here is an example of its content (Windows):

    [DEFAULT]
    snap_home: C:\Program Files\snap-10.0
    java_class_path: ./target/classes
    java_max_mem: 8G
    debug: True


Examples
--------

### SNAP Java API Usage

The examples for the API usage are simple tools that compute an output product from an input product.
These examples expect MERIS L1 or L2 products as stated in each of the example scripts.
You can download Envisat MERIS L1 or L2 files used as input from various archives, such as
[ESA Envisat MERIS Online Dissemination Service](https://meris-ds.eo.esa.int/oads/access/)
at ESA, or
[OceanColor Web](https://oceancolor.gsfc.nasa.gov/data/meris/) at NASA.

Again, all examples expect [numpy](http://www.numpy.org/) to be installed. Also note that all
the scripts append the relative path

    sys.path.append('../../')

as in this example setup the *esa_snappy* module is located two folder levels above the 'examples' subfolder.
Adapt this path (or replace by the absolute path of your *esa_snappy* module) if the scripts shall
be run from a different location.

Let's assume you have in the 'examples' subfolder a MERIS L1 and a MERIS L2 test product saved in Dimap format
named `MER_RR__1P.dim` and `MER_RR__2P.dim` in order to run the example code.

Computing a Fluorescence Line Height (FLH) product from water-leaving reflectances:

    python snappy_flh.py MER_RR__2P.dim

Computing a Normalized Difference Vegetation Index (NDVI) product from top-of-atmosphere radiances:

    python snappy_ndvi.py MER_RR__1P.dim
    python snappy_ndvi_with_masks.py MER_RR__1P.dim

Performing arbitrary band maths:

    python snappy_bmaths.py MER_RR__1P.dim

Tailoring any input product to a spatial subset:

    python snappy_subset.py MER_RR__2P.dim "POLYGON((15.786082 45.30223, 11.798364 46.118263, 10.878688 43.61961, 14.722727 42.85818, 15.786082 45.30223))"


There are many more possibilities to use the SNAP API. In principle, all Java classes of the SNAP API can be used.
As the SNAP API can be used from Python in a similar way as from Java, all of the Java API documentation applies as well.
Please check:

* [SNAP Engine API Documentation](http://step.esa.int/docs/v9.0/apidoc/engine/)
* [SNAP Desktop API Documentation](http://step.esa.int/docs/v9.0/apidoc/desktop/)

### Import of Java API classes

The *esa_snappy* module imports the most frequently used Java API classes by default:

##### Frequently used classes & interfaces from JRE
*    `String = jpy.get_type('java.lang.String')`
*    `File = jpy.get_type('java.io.File')`
*    `Point = jpy.get_type('java.awt.Point')`
*    `Rectangle = jpy.get_type('java.awt.Rectangle')`
*    `Arrays = jpy.get_type('java.util.Arrays')`
*    `Collections = jpy.get_type('java.util.Collections')`
*    `List = jpy.get_type('java.util.List')`
*    `Map = jpy.get_type('java.util.Map')`
*    `Set = jpy.get_type('java.util.Set')`
*    `ArrayList = jpy.get_type('java.util.ArrayList')`
*    `HashMap = jpy.get_type('java.util.HashMap')`
*    `HashSet = jpy.get_type('java.util.HashSet')`

##### Frequently used classes & interfaces from SNAP Engine

*Product tree & associates:*

*    `Product = jpy.get_type('org.esa.snap.core.datamodel.Product')`
*    `VectorDataNode = jpy.get_type('org.esa.snap.core.datamodel.VectorDataNode')`
*    `RasterDataNode = jpy.get_type('org.esa.snap.core.datamodel.RasterDataNode')`
*    `TiePointGrid = jpy.get_type('org.esa.snap.core.datamodel.TiePointGrid')`
*    `AbstractBand = jpy.get_type('org.esa.snap.core.datamodel.AbstractBand')`
*    `Band = jpy.get_type('org.esa.snap.core.datamodel.Band')`
*    `VirtualBand = jpy.get_type('org.esa.snap.core.datamodel.VirtualBand')`
*    `Mask = jpy.get_type('org.esa.snap.core.datamodel.Mask')`
*    `GeneralFilterBand = jpy.get_type('org.esa.snap.core.datamodel.GeneralFilterBand')`
*    `ConvolutionFilterBand = jpy.get_type('org.esa.snap.core.datamodel.ConvolutionFilterBand')`

*Product tree associates:*

*    `ProductData = jpy.get_type('org.esa.snap.core.datamodel.ProductData')`
*    `GeoCoding = jpy.get_type('org.esa.snap.core.datamodel.GeoCoding')`
*    `TiePointGeoCoding = jpy.get_type('org.esa.snap.core.datamodel.TiePointGeoCoding')`
*    `PixelGeoCoding = jpy.get_type('org.esa.snap.core.datamodel.PixelGeoCoding')`
*    `PixelGeoCoding2 = jpy.get_type('org.esa.snap.core.datamodel.PixelGeoCoding2')`
*    `CrsGeoCoding = jpy.get_type('org.esa.snap.core.datamodel.CrsGeoCoding')`
*    `GeoPos = jpy.get_type('org.esa.snap.core.datamodel.GeoPos')`
*    `PixelPos = jpy.get_type('org.esa.snap.core.datamodel.PixelPos')`
*    `FlagCoding = jpy.get_type('org.esa.snap.core.datamodel.FlagCoding')`
*    `ProductNodeGroup = jpy.get_type('org.esa.snap.core.datamodel.ProductNodeGroup')`

*Graph Processing Framework:*

*    `GPF = jpy.get_type('org.esa.snap.core.gpf.GPF')`
*    `Operator = jpy.get_type('org.esa.snap.core.gpf.Operator')`
*    `Tile = jpy.get_type('org.esa.snap.core.gpf.Tile')`

*Utilities:*

*    `EngineConfig = jpy.get_type('org.esa.snap.runtime.EngineConfig')`
*    `Engine = jpy.get_type('org.esa.snap.runtime.Engine')`
*    `SystemUtils = jpy.get_type('org.esa.snap.core.util.SystemUtils')`
*    `ProductIO = jpy.get_type('org.esa.snap.core.dataio.ProductIO')`
*    `ProductUtils = jpy.get_type('org.esa.snap.core.util.ProductUtils')`
*    `GeoUtils = jpy.get_type('org.esa.snap.core.util.GeoUtils')`
*    `ProgressMonitor = jpy.get_type('com.bc.ceres.core.ProgressMonitor')`
*    `PlainFeatureFactory = jpy.get_type('org.esa.snap.core.datamodel.PlainFeatureFactory')`
*    `FeatureUtils = jpy.get_type('org.esa.snap.core.util.FeatureUtils')`

*GeoTools:*

*    `DefaultGeographicCRS = jpy.get_type('org.geotools.referencing.crs.DefaultGeographicCRS')`
*    `ListFeatureCollection = jpy.get_type('org.geotools.data.collection.ListFeatureCollection')`
*    `SimpleFeatureBuilder = jpy.get_type('org.geotools.feature.simple.SimpleFeatureBuilder')`

*JTS:*

*    `Geometry = jpy.get_type('org.locationtech.jts.geom.Geometry')`
*    `WKTReader = jpy.get_type('org.locationtech.jts.io.WKTReader')`


To import other Java API classes, get the fully qualified type name from the API reference and import it using jpy.
For example:

    jpy = esa_snappy.jpy
    Color = jpy.get_type('java.awt.Color')
    ColorPoint = jpy.get_type('org.esa.snap.core.datamodel.ColorPaletteDef$Point')
    ColorPaletteDef = jpy.get_type('org.esa.snap.core.datamodel.ColorPaletteDef')
    ImageInfo = jpy.get_type('org.esa.snap.core.datamodel.ImageInfo')
    ImageManager = jpy.get_type('org.esa.snap.core.image.ImageManager')
    JAI = jpy.get_type('javax.media.jai.JAI')


### SNAP Operator Plugin

The *esa-snappy* module also enables Python developers to write operators for SNAP.
See the comprehensive documentation:

* [How to integrate an operator](https://senbox.atlassian.net/wiki/spaces/SNAP/pages/24051787/How+to+integrate+an+operator)
* [What to consider when writing an Operator in Python](https://senbox.atlassian.net/wiki/spaces/SNAP/pages/42041346/What+to+consider+when+writing+an+Operator+in+Python)


#### Finally, please don't hesitate to contact the [SNAP User Forum](http://forum.step.esa.int).

*Have fun!*
