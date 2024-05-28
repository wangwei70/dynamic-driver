/*
 * Copyright (c) 2003, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package com.asia.driver.dynamic;

import com.asia.driver.dynamic.enums.ProxyProperty;
import com.asia.driver.dynamic.exception.ProxyDriverError;
import com.asia.driver.dynamic.adapt.BasicDriverMatcherAdapter;
import com.asia.driver.dynamic.utils.ProxyInfo;


import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.*;
import java.util.*;
import java.util.logging.*;



/**
 * <p>The Java SQL framework allows for multiple database drivers. Each driver should supply a class
 * that implements the Driver interface</p>
 *
 * <p>The DriverManager will try to load as many drivers as it can find and then for any given
 * connection request, it will ask each driver in turn to try to connect to the target URL.</p>
 *
 * <p>It is strongly recommended that each Driver class should be small and standalone so that the
 * Driver class can be loaded and queried without bringing in vast quantities of supporting code.</p>
 *
 * <p>When a Driver class is loaded, it should create an instance of itself and register it with the
 * DriverManager. This means that a user can load and register a driver by doing
 * Class.forName("foo.bah.Driver")</p>
 *
 * @see
 * @see java.sql.Driver
 */
public class Driver implements java.sql.Driver {

    private static Driver registeredDriver;
    private static final Logger LOGGER = Logger.getLogger("com.asia.driver.dynamic.Driver");
    private static final Logger PARENT_LOGGER = Logger.getLogger("com.asia.driver.dynamic");

    static {
        try {
            // moved the registerDriver from the constructor to here
            // because some clients call the driver themselves (I know, as
            // my early jdbc work did - and that was based on other examples).
            // Placing it here, means that the driver is registered once only.
            register();
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Helper to retrieve default properties from classloader resource
    // properties files.
    private Properties defaultProperties;

    private synchronized Properties getDefaultProperties() throws IOException {
        if (defaultProperties != null) {
            return defaultProperties;
        }

        // Make sure we load properties with the maximum possible privileges.
        try {
            defaultProperties =
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Properties>() {
                        public Properties run() throws IOException {
                            return loadDefaultProperties();
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }

        return defaultProperties;
    }

    private Properties loadDefaultProperties() throws IOException {
        // 扩展默认配置参数
        return new Properties();
    }

    /**
     * <p>Try to make a database connection to the given URL. The driver should return "null" if it
     * realizes it is the wrong kind of driver to connect to the given URL. This will be common, as
     * when the JDBC driverManager is asked to connect to a given URL, it passes the URL to each
     * loaded driver in turn.</p>
     *
     * <p>The driver should raise an SQLException if it is the right driver to connect to the given URL,
     * but has trouble connecting to the database.</p>
     *
     * <p>The java.util.Properties argument can be used to pass arbitrary string tag/value pairs as
     * connection arguments.</p>
     *
     * <ul>
     * <li>user - (required) The user to connect as</li>
     * <li>password - (optional) The password for the user</li>
     * <li>ssl -(optional) Use SSL when connecting to the server</li>
     * <li>readOnly - (optional) Set connection to read-only by default</li>
     * <li>charSet - (optional) The character set to be used for converting to/from
     * the database to unicode. If multibyte is enabled on the server then the character set of the
     * database is used as the default, otherwise the jvm character encoding is used as the default.
     * This value is only used when connecting to a 7.2 or older server.</li>
     * <li>loglevel - (optional) Enable logging of messages from the driver. The value is an integer
     * from 0 to 2 where: OFF = 0, INFO =1, DEBUG = 2 The output is sent to
     * DriverManager.getPrintWriter() if set, otherwise it is sent to System.out.</li>
     * <li>compatible - (optional) This is used to toggle between different functionality
     * as it changes across different releases of the jdbc driver code. The values here are versions
     * of the jdbc client and not server versions. For example in 7.1 get/setBytes worked on
     * LargeObject values, in 7.2 these methods were changed to work on bytea values. This change in
     * functionality could be disabled by setting the compatible level to be "7.1", in which case the
     * driver will revert to the 7.1 functionality.</li>
     * </ul>
     *
     * <p>Normally, at least "user" and "password" properties should be included in the properties. For a
     * list of supported character encoding , see
     * http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html Note that you will
     * probably want to have set up the Postgres database itself to use the same encoding, with the
     * {@code -E <encoding>} argument to createdb.</p>
     *
     * <p>Our protocol takes the forms:</p>
     *
     * <pre>
     *  jdbc:postgresql://host:port/database?param1=val1&amp;...
     * </pre>
     *
     * @param url  the URL of the database to connect to
     * @param info a list of arbitrary tag/value pairs as connection arguments
     * @return a connection to the URL or null if it isnt us
     * @throws SQLException if a database access error occurs or the url is
     *                      {@code null}
     * @see java.sql.Driver#connect
     */
    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        LOGGER.info("------------------ enter proxy driver connect ------------------ ");
        if (url == null) {
            throw new SQLException("url is null");
        }
        // get defaults
        Properties defaults;

        if (!url.startsWith("proxy:jdbc:")) {
            return null;
        }
        try {
            defaults = getDefaultProperties();
        } catch (IOException ioe) {
            throw new ProxyDriverError("Error loading default settings param");
        }

        // 获取默认参数值
        Properties props = new Properties(defaults);
        if (info != null) {
            Set<String> e = info.stringPropertyNames();
            for (String propName : e) {
                String propValue = info.getProperty(propName);
                if (propValue == null) {
                    throw new ProxyDriverError(
                            "Properties for the proxy driver contains a non-string value for the key "
                                    + propName);
                }
                props.setProperty(propName, propValue);
            }
        }
        // 解析url获取参数
        if ((props = parseURL(url, props)) == null) {
            return null;
        }

        // 适配器模式，内部适配到匹配器再进行驱动匹配
        BasicDriverMatcherAdapter matcherAdapter = new BasicDriverMatcherAdapter(props);
        java.sql.Driver driver = matcherAdapter.match();

        // 用于存储代理驱动的参数
        Properties proxyProp = new Properties();
        Set<Object> keys = new HashSet<>(props.keySet());
        for (Object key : keys) {
            if (ProxyProperty.forName((String) key) != null) {
                // 真实驱动的参数
                proxyProp.put(key, props.remove(key));
            }
        }
        LOGGER.info("real jdbc url : "+ProxyProperty.REAL_URL.get(proxyProp));
        LOGGER.info("find driver version : "+driver.getMajorVersion()+"."+driver.getMinorVersion());
        Connection connection = driver.connect(ProxyProperty.REAL_URL.get(proxyProp), props);
        LOGGER.info("------------------ finish proxy driver connect ------------------ ");
        return connection;
    }



    /**
     * Returns true if the driver thinks it can open a connection to the given URL. Typically, drivers
     * will return true if they understand the subprotocol specified in the URL and false if they
     * don't. Our protocols start with jdbc:postgresql:
     *
     * @param url the URL of the driver
     * @return true if this driver accepts the given URL
     * @see java.sql.Driver#acceptsURL
     */
    @Override
    public boolean acceptsURL(String url) {
        return parseURL(url, null) != null;
    }

    /**
     * <p>The getPropertyInfo method is intended to allow a generic GUI tool to discover what properties
     * it should prompt a human for in order to get enough information to connect to a database.</p>
     *
     * <p>Note that depending on the values the human has supplied so far, additional values may become
     * necessary, so it may be necessary to iterate through several calls to getPropertyInfo</p>
     *
     * @param url  the Url of the database to connect to
     * @param info a proposed list of tag/value pairs that will be sent on connect open.
     * @return An array of DriverPropertyInfo objects describing possible properties. This array may
     * be an empty array if no properties are required
     * @see java.sql.Driver#getPropertyInfo
     */
    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        Properties copy = new Properties(info);
        Properties parse = parseURL(url, copy);
        if (parse != null) {
            copy = parse;
        }

        ProxyProperty[] knownProperties = ProxyProperty.values();
        DriverPropertyInfo[] props = new DriverPropertyInfo[knownProperties.length];
        for (int i = 0; i < props.length; ++i) {
            props[i] = knownProperties[i].toDriverPropertyInfo(copy);
        }

        return props;
    }

    @Override
    public int getMajorVersion() {
        return ProxyInfo.MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return ProxyInfo.MINOR_VERSION;
    }

    /**
     * Returns the server version series of this driver and the specific build number.
     *
     * @return JDBC driver version
     * @deprecated use {@link #getMajorVersion()} and {@link #getMinorVersion()} instead
     */
    @Deprecated
    public static String getVersion() {
        return ProxyInfo.DRIVER_FULL_NAME;
    }

    /**
     * <p>Report whether the driver is a genuine JDBC compliant driver. A driver may only report "true"
     * here if it passes the JDBC compliance tests, otherwise it is required to return false. JDBC
     * compliance requires full support for the JDBC API and full support for SQL 92 Entry Level.</p>
     *
     * <p>For PostgreSQL, this is not yet possible, as we are not SQL92 compliant (yet).</p>
     */
    @Override
    public boolean jdbcCompliant() {
        return false;
    }



    public static void main(String[] args) {
//        parseURL("jdbc:postgresql://10.1.206.136:8402/postgres?dbversion=1.1.1", new Properties());
        parseURL("proxy:jdbc:postgresql://10.1.206.136:8402/postgres?dbversion=1.1.1&aaa&bb=true", new Properties());
    }

    /**
     * Constructs a new DriverURL, splitting the specified URL into its component parts.
     *
     * @param url      JDBC URL to parse
     * @param defaults Default properties
     * @return Properties with elements added from the url
     */
    public static Properties parseURL(String url, Properties defaults) {
        Properties prop = new Properties();

        if (!url.startsWith("proxy:jdbc:")) {
            LOGGER.log(Level.FINE, "JDBC URL must start with \"proxy:jdbc:\" but was: {0}", url);
            return null;
        }
        //  url="proxy:jdbc:postgresql://${host}:${port}/postgres?dbversion=1.1.1&&aaa&&bb=true"
        //  urlServer="jdbc:postgresql://${host}:${port}/postgres"
        //  urlArgs="dbversion=1.1.1&&aaa&&bb=true"
        String urlServer = url.substring("proxy:".length());
        String urlArgs = "";
        int qPos = urlServer.indexOf('?');
        if (qPos != -1) {
            urlArgs = urlServer.substring(qPos + 1);
            urlServer = urlServer.substring(0, qPos);
        }

        //  dbType="postgresql"
        String dbTypeServer = urlServer.substring("jdbc:".length());
        int hostPos = dbTypeServer.indexOf(":");
        String dbType = "";
        if (hostPos != -1) {
            dbType = dbTypeServer.substring(0, hostPos);
        //  urlServer = urlServer.substring(hostPos + 1);
        }
        ProxyProperty.DB_TYPE.set(prop, dbType);


        // 重组参数，屏蔽代理驱动的参数，获取真实驱动的url
        String[] args = urlArgs.split("&");
        StringBuilder urlSb = new StringBuilder(urlServer).append("?");
        Properties argsProp = new Properties();
        for (String token : args) {
            if (token.isEmpty()) {
                continue;
            }
            int pos = token.indexOf('=');
            String paramName;
            if (pos == -1) {
                paramName = token;
                argsProp.setProperty(token, "");
            } else {
                String pName = token.substring(0, pos);
                paramName = pName;
                String pValue = token.substring(pos + 1);
                argsProp.setProperty(pName, pValue);
            }
            if (ProxyProperty.forName(paramName) != null) {
                // 真实url需要屏蔽掉代理驱动的参数
                prop.put(paramName, argsProp.get(paramName));
                continue;
            }
            urlSb.append(token).append("&");
        }
        urlServer = urlSb.toString();
        urlServer = (urlServer.endsWith("&") || urlServer.endsWith("?")) ? urlServer.substring(0, urlServer.length() - 1) : urlServer;

        ProxyProperty.REAL_URL.set(prop, urlServer);
        prop.putAll(defaults);
        return prop;
    }

    @Override
    public Logger getParentLogger() {
        return PARENT_LOGGER;
    }


    /**
     * Register the driver against {@link DriverManager}. This is done automatically when the class is
     * loaded. Dropping the driver from DriverManager's list is possible using
     * method.
     *
     * @throws IllegalStateException if the driver is already registered
     * @throws SQLException          if registering the driver fails
     */
    public static void register() throws SQLException {
        if (isRegistered()) {
            throw new IllegalStateException(
                    "Driver is already registered. It can only be registered once.");
        }
        Driver registeredDriver = new Driver();
        DriverManager.registerDriver(registeredDriver);
        Driver.registeredDriver = registeredDriver;
    }



    /**
     * @return {@code true} if the driver is registered against {@link DriverManager}
     */
    public static boolean isRegistered() {
        return registeredDriver != null;
    }
}
