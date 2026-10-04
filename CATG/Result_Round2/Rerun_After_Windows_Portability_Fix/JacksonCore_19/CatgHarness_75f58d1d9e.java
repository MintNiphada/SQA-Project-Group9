package catg.generated;

import catg.CATG;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class CatgHarness_75f58d1d9e {
    public static void main(String[] args) {
        try {
            Class<?> target = Class.forName("com.fasterxml.jackson.core.json.ReaderBasedJsonParser");
            Class<?>[] types = {boolean.class, int.class};
            Method method = target.getDeclaredMethod("_parseNumber2", types);
            method.setAccessible(true);
            Object receiver = Modifier.isStatic(method.getModifiers()) ? null : newInstance(target);
            Object[] values = new Object[types.length];
values[0] = CATG.readBool(false);
values[1] = CATG.readInt(0);
            try {
                method.invoke(receiver, values);
            } catch (InvocationTargetException error) {
                Throwable cause = error.getCause();
                if (cause != null) {
                    System.err.println(cause);
                }
            }
        } catch (Throwable error) {
            error.printStackTrace();
            System.exit(1);
        }
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
