package com.asia.driver.dynamic.loader;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;


@Slf4j
public class DriverClassLoader extends URLClassLoader {

    private static final String CLASS_FILE_SUFFIX = ".class";

    private ClassLoader javaseClassLoader;

    /**
     * 存放数据库版本的对象
     **/
    @Setter
    private HashMap<String, ClassLoader> configLoaders = new HashMap();

    public ClassLoader getConfigLoader(String mapKey) {
        return configLoaders.get(mapKey);
    }

    /**
     * 定义需要加载的数据库版本
     **/
    @Setter
    private String dmsDriverVersion;

    public DriverClassLoader(URL[] urls) {

        super(urls,null);
        ClassLoader javese = String.class.getClassLoader();
        if (javese == null) {
            javese = getSystemClassLoader();
            while (javese.getParent() != null) {
                javese = javese.getParent();
            }
        }
        // extClassloader
        this.javaseClassLoader = javese;

    }



    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    /**
     * 跳过从classpath下加载类，先通过extClassloader进行加载，加载不到再使用当前类加载器进行加载
     *
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return (clazz);
            }
            String resourceName = binaryNameToPath(name, false);
            boolean tryLoadingFromJavaseLoader;
            try {
                URL url = javaseClassLoader.getResource(resourceName);
                tryLoadingFromJavaseLoader = (url != null);
            } catch (Throwable t) {
                tryLoadingFromJavaseLoader = true;
            }
            if (tryLoadingFromJavaseLoader) {
                try {
                    clazz = javaseClassLoader.loadClass(name);
                    if (clazz != null) {
                        if (resolve) {
                            resolveClass(clazz);
                        }
                        return (clazz);
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore
                }
            }

            //
            try {
                clazz = findClass(name);
                if (clazz != null) {
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }

        }
        throw new ClassNotFoundException(name);
    }



    private String binaryNameToPath(String binaryName, boolean withLeadingSlash) {
        // 1 for leading '/', 6 for ".class"
        StringBuilder path = new StringBuilder(7 + binaryName.length());
        if (withLeadingSlash) {
            path.append('/');
        }
        path.append(binaryName.replace('.', '/'));
        path.append(CLASS_FILE_SUFFIX);
        return path.toString();
    }
}