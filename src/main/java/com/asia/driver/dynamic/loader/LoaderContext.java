package com.asia.driver.dynamic.loader;

import com.asia.driver.dynamic.bean.DriverInfo;
import com.asia.driver.dynamic.bean.DriverMapping;
import com.asia.driver.dynamic.config.Parser;
import com.asia.driver.dynamic.config.PropertiesFileParser;
import com.asia.driver.dynamic.exception.ParseError;
import com.asia.driver.dynamic.utils.ProxyInfo;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author wangwei
 * @date 2024/02/22
 * 上下文对象，用于保存驱动配置的解析结果
 */
public class LoaderContext {
    static {
        driverMappings = new CopyOnWriteArrayList<>();
        driverInfoMap = new ConcurrentHashMap<>();
        getParse().parse();
    }

    // 封装解析出来的驱动配置
    private final static List<DriverMapping> driverMappings;

    // 一个驱动包对应一个DriverInfo
    private final static ConcurrentHashMap<String, DriverInfo> driverInfoMap;

    private static Parser parse;

    public static Parser getParse() {
        if (parse == null) {
            synchronized (LoaderContext.class) {
                if (parse == null) {
                    parse = new PropertiesFileParser();
                }
            }
        }
        return parse;
    }

    public static List<DriverMapping> getDriverMappings() {
        return driverMappings;
    }

    // 当前线程所匹配到的driver对象
    private static ThreadLocal<Driver> driverContext = ThreadLocal.withInitial(() -> null);

    public static URLClassLoader getClassLoad(String url) {
        DriverInfo driverInfo = driverInfoMap.get(url);
        return driverInfo == null ? null : driverInfo.getClassLoader();
    }

    public static Driver getDriverContext() {
        return driverContext.get();
    }

    public static void setDriverContext(Driver driver) {

        driverContext.set(driver);
    }

    public static void addOneDriverMapping(DriverMapping driverMapping) {
        driverMappings.add(driverMapping);
    }


    public static void putDriverInfo(HashMap<String, DriverInfo> map) {
        driverInfoMap.putAll(map);
    }

    public static void addDriverMappings(List<DriverMapping> mappings) {
        driverMappings.addAll(mappings);
    }

    public static void initClassLoader(DriverInfo driverInfo) {
        if (driverInfo.getClassLoader() == null) {
            try {
                List<URL> allUrl = new ArrayList<>();
//                URL url = new URL("jar:file:" + driverInfo.getJarAbsolutePath() + "!/");
//                allUrl.add(url);

                // 扩展包的处理
                List<String> otherJars = findJars(driverInfo.getJarAbsolutePath());
                for (int i = 0; i < otherJars.size(); i++) {
                    URL otherUrl = new URL("jar:file:" + otherJars.get(i) + "!/");
                    allUrl.add(otherUrl);
                }
                URLClassLoader classLoader = new DriverClassLoader((URL[]) allUrl.toArray(new URL[0]));
                driverInfo.setClassLoader(classLoader);
            } catch (Exception e) {
                throw new ParseError(e);
            }
        }
    }

    private static List<String> findJars(String jar) {

        ArrayList<String> otherJars = new ArrayList<>();
        String parentDir = jar.substring(0, jar.lastIndexOf(ProxyInfo.SPERATE_CHAR));
        File parentDirFile = new File(parentDir);
        File[] files = parentDirFile.listFiles();
        for (File file : files) {
            if (file.getPath().endsWith(".jar") ) {
                otherJars.add(file.getPath());
            }
        }
        return otherJars;
    }

    public static void initDriver(DriverInfo driverInfo) {
        if (driverInfo.getDriver() == null) {
            try {
                initClassLoader(driverInfo);
                Class<?> driverCls = driverInfo.getClassLoader().loadClass(driverInfo.getClassName());
                Driver driver = (Driver) driverCls.newInstance();
                driverInfo.setDriver(driver);
            } catch (Exception e) {
                throw new ParseError(e);
            }

        }
    }

    public static void clearDriver() {
        driverContext.remove();
    }


}
