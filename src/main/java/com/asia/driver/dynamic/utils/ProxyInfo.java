/*
 * Copyright (c) 2017, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package com.asia.driver.dynamic.utils;

import com.asia.driver.dynamic.enums.ProxyProperty;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Utility class with constants of Driver information.
 */
@Slf4j
public final class ProxyInfo {
    public static List<String> DRIVER_PATHES;

    /**
     * 驱动配置文件（包括驱动包）的加载顺序：
     * 1.系统属性proxy.driver.config.path对应的目录（用户配置）
     * 2.外部config目录下
     * 3.classpath默认目录
     */
    static {
        DRIVER_PATHES = new ArrayList<>();
        // 用户配置的目录
        String userConfig = System.getProperty("proxy.driver.config.path");
        log.debug("userConfig :" + userConfig);

        // 外部默认目录
        String externalConfig = System.getProperty("user.dir");
        if (externalConfig != null) {
            externalConfig = externalConfig + File.separator + ProxyInfo.DEFAULT_EXTERNAL_CONFIG_PATH;
        }
        log.debug("externalConfig :" + externalConfig);

        // classpath默认目录
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        String cpConfigFile = systemClassLoader.getResource("").getPath() + ProxyInfo.DEFAULT_CLASSPATH_CONFIG_PATH;
        log.debug("cpConfigFile :" + cpConfigFile);

        if (userConfig != null) {
            DRIVER_PATHES.add(userConfig);
        }
        if (externalConfig != null) {
            DRIVER_PATHES.add(externalConfig);
        }
        if (cpConfigFile != null) {
            DRIVER_PATHES.add(cpConfigFile);
        }
    }

    private ProxyInfo() {
    }

    // Driver name
    public static final String DRIVER_NAME = "Proxy JDBC Driver";
    public static final String DRIVER_SHORT_NAME = "ProxyJDBC";
    public static final String DRIVER_VERSION = "1.0.0";
    public static final String DRIVER_FULL_NAME = DRIVER_NAME + " " + DRIVER_VERSION;

    // Driver version
    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 0;
    public static final int PATCH_VERSION = 0;

    // 分隔符
    public static final String SPERATE_CHAR = "/";

    // 配置路径
    // 默认的外部配置目录
    public static final String DEFAULT_EXTERNAL_CONFIG_PATH = "driver";
    // 默认的classpath配置目录
    public static final String DEFAULT_CLASSPATH_CONFIG_PATH = "driver";
    // 默认的配置文件名称
    public static final String DEFAULT_FILE_NAME = "driver.properties";

    // 默认的classpath下配置文件相对路径
    public static final String DEFAULT_CLASSPATH_CONFIG_FILE_PATH = DEFAULT_CLASSPATH_CONFIG_PATH + File.separator + DEFAULT_FILE_NAME;
    // 默认的外部配置文件相对路径
    public static final String DEFAULT_EXTERNAL_CONFIG_FILE_PATH = DEFAULT_EXTERNAL_CONFIG_PATH + File.separator + DEFAULT_FILE_NAME;

    public static void main(String[] args) {
        System.out.println(getProxyUrl("jdbc:postgresql://10.1.206.136:8402/postgres", "8.1.1"));
    }

    public static String getProxyUrl(String url, String dbVersion) {
        String proxyUrl = url;
        if (!url.startsWith("proxy")) {
            proxyUrl = "proxy:" + proxyUrl;
        }

        if (!proxyUrl.contains("?")) {
            return proxyUrl + "?" + ProxyProperty.DB_VERSION.getName() + "=" + dbVersion;
        }

        String urlArgs = url.substring(url.indexOf("?") + 1);
        // "?" 后面有参数
        if (urlArgs != null && !urlArgs.trim().equals("")) {
            // 获取所有参数
            Properties argsProp = new Properties();
            String[] args = urlArgs.split("&");
            for (String token : args) {
                if (token.isEmpty()) {
                    continue;
                }
                int pos = token.indexOf('=');
                if (pos == -1) {
                    argsProp.setProperty(token, "");
                } else {
                    String pName = token.substring(0, pos);
                    String pValue = token.substring(pos + 1);
                    argsProp.setProperty(pName, pValue);
                }
            }

            if (ProxyProperty.DB_VERSION.get(argsProp) == null) {
                proxyUrl = proxyUrl + "&" + ProxyProperty.DB_VERSION.getName() + "=" + dbVersion;
            }
        } else {
            // "?" 后面没有参数
            proxyUrl = proxyUrl + ProxyProperty.DB_VERSION.getName() + "=" + dbVersion;
        }


        return proxyUrl;
    }
}
