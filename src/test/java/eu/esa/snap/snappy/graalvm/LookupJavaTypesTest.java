//package eu.esa.snap.snappy.graalvm;
//
//import org.junit.Test;
//
//import org.graalvm.polyglot.*;
//
//import java.math.BigDecimal;
//
//import static org.junit.Assert.assertEquals;
//
//public class LookupJavaTypesTest {
//
//    @Test
//    public void testBigDecimal() {
//        try (Context context = Context.newBuilder()
//                .allowAllAccess(true)
//                .build()) {
//            BigDecimal v = context.eval("python",
//                            "import java\n" +
//                                    "BigDecimal = java.type('java.math.BigDecimal')\n" +
//                                    "BigDecimal.valueOf(10).pow(20)")
//                    .asHostObject();
//            assert v.toString().equals("100000000000000000000");
//        }
//    }
//
//    @Test
//    public void testSnapCoreClass() {
//        try (Context context = Context.newBuilder()
//                .allowAllAccess(true)
//                .build()) {
//            double v = context.eval("python",
//                            "import java\n" +
//                                    "rsmu = java.type('org.esa.snap.core.util.math.RsMathUtils')\n" +
//                                    "rsmu.DEG_PER_RAD")
//                    .asDouble();
//            assertEquals(57.2958, v, 1.E-4);
//        }
//    }
//}
