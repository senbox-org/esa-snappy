import java
import polyglot

import numpy as np

array = java.type("int[]")(4)
array[2] = 42
print(array[2])

polyglot.eval(string="1 + 1", language="python")

polyglot.register_interop_behavior(np.int32,
                                   is_number=True,
                                   fitsInByte=lambda v: -128 <= v < 128,
                                   fitsInShort=lambda v: -0x8000 <= v < 0x8000,
                                   fitsInInt=True,
                                   fitsInLong=True,
                                   fitsInBigInteger=True,
                                   asByte=int,
                                   asShort=int,
                                   asInt=int,
                                   asLong=int,
                                   asBigInteger=int,
                                   )

print('Finished polyglot basic tests.')
