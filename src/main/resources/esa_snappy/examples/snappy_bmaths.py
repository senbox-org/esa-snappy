import sys

# This example requires a MERIS L1b product as input

# This relative path is for the esa_snappy test setup only, as the esa_snappy module is located two folder levels above.
# Adapt this path if script shall be run from a different location!
sys.path.append('../../')

from esa_snappy import GPF
from esa_snappy import HashMap
from esa_snappy import ProductIO
from esa_snappy import jpy

if len(sys.argv) != 2:
    print("usage: %s <file>" % sys.argv[0])
    sys.exit(1)

file = sys.argv[1]

print("Reading...")
product = ProductIO.readProduct(file)
width = product.getSceneRasterWidth()
height = product.getSceneRasterHeight()
name = product.getName()
description = product.getDescription()
band_names = product.getBandNames()

print("Product: %s, %d x %d pixels, %s" % (name, width, height, description))
print("Bands:   %s" % (list(band_names)))

GPF.getDefaultInstance().getOperatorSpiRegistry().loadOperatorSpis()

BandDescriptor = jpy.get_type('org.esa.snap.core.gpf.common.BandMathsOp$BandDescriptor')

targetBand1 = BandDescriptor()
targetBand1.name = 'band_1'
targetBand1.type = 'float32'
targetBand1.expression = '(radiance_10 - radiance_7) / (radiance_10 + radiance_7)'

targetBand2 = BandDescriptor()
targetBand2.name = 'band_2'
targetBand2.type = 'float32'
targetBand2.expression = '(radiance_9 - radiance_6) / (radiance_9 + radiance_6)'

if product.getBand('radiance_10') is None:
    # probably 4RP
    targetBand1.expression = '(M10_radiance - M07_radiance) / (M10_radiance + M07_radiance)'
    targetBand2.expression = '(M09_radiance - M06_radiance) / (M09_radiance + M06_radiance)'

targetBands = jpy.array('org.esa.snap.core.gpf.common.BandMathsOp$BandDescriptor', 2)
targetBands[0] = targetBand1
targetBands[1] = targetBand2

parameters = HashMap()
parameters.put('targetBands', targetBands)

result = GPF.createProduct('BandMaths', parameters, product)

print("Writing...")

ProductIO.writeProduct(result, 'snappy_bmaths_output.dim', 'BEAM-DIMAP')

print("Done.")
