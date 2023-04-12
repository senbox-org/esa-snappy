import sys

# This relative path is for the esa_snappy test setup only, as the esa_snappy module is located two folder levels above.
# Adapt this path if script shall be run from a different location!
sys.path.append('../../')
from esa_snappy import jpy

ProductIOPlugInManager = jpy.get_type('org.esa.snap.core.dataio.ProductIOPlugInManager')
ProductReaderPlugIn = jpy.get_type('org.esa.snap.core.dataio.ProductReaderPlugIn')
ProductWriterPlugIn = jpy.get_type('org.esa.snap.core.dataio.ProductWriterPlugIn')

read_plugins = ProductIOPlugInManager.getInstance().getAllReaderPlugIns()
write_plugins = ProductIOPlugInManager.getInstance().getAllWriterPlugIns()

print('Writer formats:')
while write_plugins.hasNext():
    plugin = write_plugins.next()
    print('  ', plugin.getFormatNames()[0], plugin.getDefaultFileExtensions()[0])

print(' ')

print('Reader formats:')
while read_plugins.hasNext():
    plugin = read_plugins.next()
    print('  ', plugin.getFormatNames()[0], plugin.getDefaultFileExtensions()[0])