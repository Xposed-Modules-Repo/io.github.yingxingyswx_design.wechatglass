package com.wechatglass;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Obfuscated WeChat class names, per WeChat build.
 *
 * <p>WeChat renames its obfuscated packages on every release (for example the
 * messenger storage accessor was {@code l51.i9} in 8.0.79 and is
 * {@code w41.i9} in 8.0.80). Each lookup tries the known names newest first
 * and checks the shape of the class before it is used, so a wrong guess is
 * skipped instead of breaking the page. Add a new WeChat build by putting its
 * names at the front of each list.
 */
final class WxNames {

    /** Static {@code b()} returns the storage container with r()/s(). */
    private static final String[] STORAGE_ACCESSOR = {
            "w41.i9", // 8.0.80
            "l51.i9", // 8.0.79
    };

    /** Home conversation adapter (a BaseAdapter) and its ListView setter. */
    private static final String[][] HOME_ADAPTER = {
            {"cp5.a1", "X"}, // 8.0.80
            {"iq5.a1", "Z"}, // 8.0.79
    };

    private static volatile String homeAdapterClass;

    private WxNames() {
    }

    /** The messenger storage accessor class, or null when none matches. */
    static Class<?> storageAccessor(ClassLoader cl) {
        for (String name : STORAGE_ACCESSOR) {
            Class<?> cls = find(name, cl);
            if (cls == null) {
                continue;
            }
            try {
                Method b = cls.getDeclaredMethod("b");
                if (!Modifier.isStatic(b.getModifiers())) {
                    continue;
                }
                Class<?> container = b.getReturnType();
                container.getMethod("r");
                container.getMethod("s");
                return cls;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    /**
     * The home conversation adapter class and the name of its
     * {@code (ListView) -> void} setter, or null when none matches.
     */
    static String[] homeAdapter(ClassLoader cl) {
        for (String[] pair : HOME_ADAPTER) {
            Class<?> cls = find(pair[0], cl);
            if (cls == null || !android.widget.BaseAdapter.class.isAssignableFrom(cls)) {
                continue;
            }
            try {
                Method m = cls.getDeclaredMethod(pair[1], android.widget.ListView.class);
                if (m.getReturnType() != void.class) {
                    continue;
                }
                homeAdapterClass = pair[0];
                return pair;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    /** Whether {@code name} is the home conversation adapter of this build. */
    static boolean isHomeAdapter(String name) {
        String resolved = homeAdapterClass;
        if (resolved != null) {
            return resolved.equals(name);
        }
        for (String[] pair : HOME_ADAPTER) {
            if (pair[0].equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static Class<?> find(String name, ClassLoader cl) {
        try {
            return Class.forName(name, false, cl);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
