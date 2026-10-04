package org.apache.commons.csv;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CATG_Csv_16Test {

    @Test(timeout = 120000)
    public void testCatg1() throws Exception {
        assertEquals("null", invoke(new String[] {}));
    }

    private static String invoke(String[] values) throws Exception {
        Class<?> target = Class.forName("org.apache.commons.csv.CSVParser");
        Class<?>[] types = new Class<?>[0];
        Method method = target.getDeclaredMethod("iterator", types);
        method.setAccessible(true);
        Object receiver = null;
        if (!Modifier.isStatic(method.getModifiers())) {
            receiver = newInstance(target);
        }
        Object[] arguments = new Object[types.length];
        for (int index = 0; index < types.length; index++) {
            arguments[index] = convert(values[index], types[index]);
        }
        try {
            return outcome(method.invoke(receiver, arguments));
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
        if (value.getClass().isArray()) return Arrays.deepToString(new Object[] { value });
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
