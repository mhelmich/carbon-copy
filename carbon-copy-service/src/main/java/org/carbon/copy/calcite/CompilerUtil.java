package org.carbon.copy.calcite;

import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.UnitCompiler;
import org.codehaus.janino.util.ClassFile;

import java.io.ByteArrayInputStream;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.concurrent.atomic.AtomicLong;

class CompilerUtil {

    private final static AtomicLong COMPILED_CLASS_INDEX = new AtomicLong();

    private CompilerUtil() {}

    private final static class JaninoRestrictedClassLoader extends SecureClassLoader {
        Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length, new ProtectionDomain(null, new Permissions(), this, null));
        }
    }

//    static Predicate compile(String expression) throws Exception {
//        if (!java.util.regex.Pattern.matches(
//                "^[a-zA-Z0-9+\\-()/\\* \t^%\\.\\?]+$", expression)) {
//            throw new SecurityException();
//        }
//
//        String classPackage = CompilerUtil.class.getPackage().getName() + ".compiled";
//        String className = "JaninoCompiledPredicate" + COMPILED_CLASS_INDEX.incrementAndGet();
//        String source = "package " + classPackage + ";\n"
//                + "import static java.lang.Math.*;\n" + "public final class "
//                + className + " implements "
//                + Predicate.class.getCanonicalName() + " {\n"
//                + "public double evaluate(double x) {\n"
//                + "return (" + expression + ");\n" + "}\n" + "}";
//        Scanner scanner = new Scanner(null, new ByteArrayInputStream(source.getBytes("UTF-8")), "UTF-8");
//
//        JaninoRestrictedClassLoader cl = new JaninoRestrictedClassLoader();
//        UnitCompiler unitCompiler = new UnitCompiler(
//                new Parser(scanner).parseCompilationUnit(),
//                new ClassLoaderIClassLoader(cl)
//        );
//        ClassFile[] classFiles = unitCompiler.compileUnit(true, true, true);
//        Class<?> clazz = cl.defineClass(classPackage + "." + className, classFiles[0].toByteArray());
//
//        return (Predicate) clazz.newInstance();
//    }

    /**
     * This compiler util compiles a boolean java code expression into a executable java function.
     */
    static CarbonCopyPredicate compileBooleanExpression(String expression) {
        if (expression == null || expression.isEmpty()) throw new IllegalArgumentException("No valid source\n" + expression);
        String classPackage = CompilerUtil.class.getPackage().getName() + ".compiled";
        String className = "CompiledPredicate" + COMPILED_CLASS_INDEX.incrementAndGet();

        try {
            String source = String.format(SOURCE_TEMPLATE, classPackage, className, expression);
            Scanner scanner = new Scanner(null, new ByteArrayInputStream(source.getBytes("UTF-8")), "UTF-8");
            JaninoRestrictedClassLoader cl = new JaninoRestrictedClassLoader();
            UnitCompiler unitCompiler = new UnitCompiler(
                    new Parser(scanner).parseCompilationUnit(),
                    new ClassLoaderIClassLoader(cl)
            );
            ClassFile[] classFiles = unitCompiler.compileUnit(false, false, false);
            Class<?> clazz = cl.defineClass(classPackage + "." + className, classFiles[0].toByteArray());

            return (CarbonCopyPredicate) clazz.newInstance();
        } catch (Exception xcp) {
            // just bubble up a RuntimeException
            throw new RuntimeException(xcp);
        }
    }

    // This template is supposed to generate code that looks like this
    // private class CompiledPredicate13 implements CarbonCopyPredicate {
    //     @Override
    //     public Boolean apply(Map<Integer, Comparable> rowIndexValueMap) {
    //         return java.util.Objects.compare("", rowIndexValueMap.get(1), java.lang.Comparable::compareTo) == 0;
    //     }
    // }
    private static final String SOURCE_TEMPLATE =
            "package %s;\n" +
            "public final class %s implements org.carbon.copy.calcite.CarbonCopyPredicate {\n" +
                    "@Override\n" +
                    "public boolean test(Object o) {\n" +
                        "org.carbon.copy.data.structures.Tuple tuple = (org.carbon.copy.data.structures.Tuple)o;\n" +
                        "return %s ;\n" +
                    "}\n" +
            "}\n";
}
