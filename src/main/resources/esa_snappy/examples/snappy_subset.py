import sys

# This relative path is for the esa_snappy test setup only, as the esa_snappy module is located two folder levels above.
# Adapt this path if script shall be run from a different location!
sys.path.append('../../')
import esa_snappy
from esa_snappy import ProductIO, WKTReader

SubsetOp = esa_snappy.jpy.get_type('org.esa.snap.core.gpf.common.SubsetOp')

if len(sys.argv) != 3:
    print("usage: %s <file> <geometry-wkt>" % sys.argv[0])
    print("       %s ./TEST.N1 \"POLYGON((15.786082 45.30223, 11.798364 46.118263, 10.878688 43.61961, 14.722727"
          "42.85818, 15.786082 45.30223))\"" % sys.argv[0])
    sys.exit(1)

file = sys.argv[1]
wkt = sys.argv[2]

geom = WKTReader().read(wkt)

print("Reading...")
product = ProductIO.readProduct(file)

op = SubsetOp()
op.setSourceProduct(product)
op.setGeoRegion(geom)
op.setCopyMetadata(True)

sub_product = op.getTargetProduct()

print("Writing...")
ProductIO.writeProduct(sub_product, "snappy_subset_output.dim", "BEAM-DIMAP")

print("Done.")
