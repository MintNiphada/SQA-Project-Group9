package catg.generated;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class CatgOutcome_8adfa635dc {
    private static final Class<?>[] TYPES = new Class<?>[0];
    public static void main(String[] args) throws Exception {
        String outcome;
        try {
            List<String> lines = Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8);
            while (lines.size() < TYPES.length) {
                lines.add("");
            }
            outcome = invoke(lines);
        } catch (Throwable error) {
            outcome = "ERROR:" + error.getClass().getName() + ":" + clean(error.getMessage());
        }
        System.out.println("CATG_OUTCOME:" + clean(outcome));
    }

    private static String invoke(List<String> lines) throws Exception {
        Class<?> target = Class.forName("org.jsoup.nodes.XmlDeclaration");
        Method method = target.getDeclaredMethod("getWholeDeclaration", TYPES);
        method.setAccessible(true);
        Object receiver = null;
        if (!Modifier.isStatic(method.getModifiers())) {
            receiver = newInstance(target);
        }
        Object[] values = new Object[TYPES.length];
        for (int index = 0; index < TYPES.length; index++) {
            values[index] = convert(lines.get(index), TYPES[index]);
        }
        try {
            return outcome(method.invoke(receiver, values));
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause();
            return "EX:" + cause.getClass().getName() + ":" + clean(cause.getMessage());
        }
    }

    private static Object convert(String value, Class<?> type) {
        if (type == int.class) return Integer.parseInt(value);
        if (type == long.class) return Long.parseLong(value);
        if (type == char.class) return value.isEmpty() ? '\u0000' : value.charAt(0);
        if (type == byte.class) return Byte.parseByte(value);
        if (type == short.class) return Short.parseShort(value);
        if (type == boolean.class) return Integer.parseInt(value) != 0;
        if (type == String.class) return value;
        throw new IllegalArgumentException(type.getName());
    }

    private static String outcome(Object value) {
        if (value == null) return "null";
        if (value.getClass().isArray()) return Arrays.deepToString(new Object[]{value});
        return String.valueOf(value);
    }

    private static String clean(String value) {
        if (value == null) return "";
        return value.replace("\r", "\\r").replace("\n", "\\n");
    }

    private static Object newInstance(Class<?> type) throws Exception {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception error) {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);
            return unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, type);
        }
    }
}
