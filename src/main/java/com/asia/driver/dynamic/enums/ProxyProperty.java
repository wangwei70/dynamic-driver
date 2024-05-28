/*
 * Copyright (c) 2004, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package com.asia.driver.dynamic.enums;

import com.asia.driver.dynamic.exception.ProxyDriverError;
import java.sql.DriverPropertyInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * All connection parameters that can be either set in JDBC URL, in Driver properties or in
 * datasource setters.
 */
public enum ProxyProperty {
    // 来源于URL server部分解析
    REAL_URL(
            "proxyRealUrl",
            "",
            "The url of real driver"),

    // 来源于URL server部分解析
    DB_TYPE(
            "proxyDbType",
            "",
            "The type of target database"),
    // 来源于URL param部分解析
    DB_VERSION(
            "proxyDbVersion",
            "",
            "The version of target database");


    private final String name;
    private final String defaultValue;
    private final boolean required;
    private final String description;
    private final String[] choices;
    private final boolean deprecated;

    ProxyProperty(String name, String defaultValue, String description) {
        this(name, defaultValue, description, false);
    }

    ProxyProperty(String name, String defaultValue, String description, boolean required) {
        this(name, defaultValue, description, required, (String[]) null);
    }

    ProxyProperty(String name, String defaultValue, String description, boolean required,
                  String[] choices) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.required = required;
        this.description = description;
        this.choices = choices;
        try {
            this.deprecated = ProxyProperty.class.getField(name()).getAnnotation(Deprecated.class) != null;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<String, ProxyProperty> PROPS_BY_NAME = new HashMap<String, ProxyProperty>();

    static {

        for (ProxyProperty prop : ProxyProperty.values()) {
            if (PROPS_BY_NAME.put(prop.getName(), prop) != null) {
                throw new IllegalStateException("Duplicate ProxyProperty name: " + prop.getName());
            }
        }
    }


    /**
     * Returns the name of the connection parameter. The name is the key that must be used in JDBC URL
     * or in Driver properties
     *
     * @return the name of the connection parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the default value for this connection parameter.
     *
     * @return the default value for this connection parameter or null
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns whether this parameter is required.
     *
     * @return whether this parameter is required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Returns the description for this connection parameter.
     *
     * @return the description for this connection parameter
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the available values for this connection parameter.
     *
     * @return the available values for this connection parameter or null
     */
    public String[] getChoices() {
        return choices;
    }

    /**
     * Returns whether this connection parameter is deprecated.
     *
     * @return whether this connection parameter is deprecated
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * Returns the value of the connection parameters according to the given {@code Properties} or the
     * default value.
     *
     * @param properties properties to take actual value from
     * @return evaluated value for this connection parameter
     */
    public String get(Properties properties) {
        return properties.getProperty(name, defaultValue);
    }

    /**
     * Set the value for this connection parameter in the given {@code Properties}.
     *
     * @param properties properties in which the value should be set
     * @param value      value for this connection parameter
     */
    public void set(Properties properties, String value) {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.setProperty(name, value);
        }
    }

    /**
     * Return the boolean value for this connection parameter in the given {@code Properties}.
     *
     * @param properties properties to take actual value from
     * @return evaluated value for this connection parameter converted to boolean
     */
    public boolean getBoolean(Properties properties) {
        return Boolean.parseBoolean(get(properties));
    }

    /**
     * Return the int value for this connection parameter in the given {@code Properties}. Prefer the
     * use of {@link #getInt(Properties)} anywhere you can throw an {@link java.sql.SQLException}.
     *
     * @param properties properties to take actual value from
     * @return evaluated value for this connection parameter converted to int
     * @throws NumberFormatException if it cannot be converted to int.
     */
    @SuppressWarnings("nullness:argument.type.incompatible")
    public int getIntNoCheck(Properties properties) {
        String value = get(properties);
        //noinspection ConstantConditions
        return Integer.parseInt(value);
    }

    /**
     * Return the int value for this connection parameter in the given {@code Properties}.
     *
     * @param properties properties to take actual value from
     * @return evaluated value for this connection parameter converted to int
     */
    @SuppressWarnings("nullness:argument.type.incompatible")
    public int getInt(Properties properties)   {
        String value = get(properties);
        try {
            //noinspection ConstantConditions
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw new ProxyDriverError();
        }
    }

    /**
     * Return the {@code Integer} value for this connection parameter in the given {@code Properties}.
     *
     * @param properties properties to take actual value from
     * @return evaluated value for this connection parameter converted to Integer or null
     */
    public Integer getInteger(Properties properties)   {
        String value = get(properties);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw new ProxyDriverError();
        }
    }

    /**
     * Set the boolean value for this connection parameter in the given {@code Properties}.
     *
     * @param properties properties in which the value should be set
     * @param value      boolean value for this connection parameter
     */
    public void set(Properties properties, boolean value) {
        properties.setProperty(name, Boolean.toString(value));
    }

    /**
     * Set the int value for this connection parameter in the given {@code Properties}.
     *
     * @param properties properties in which the value should be set
     * @param value      int value for this connection parameter
     */
    public void set(Properties properties, int value) {
        properties.setProperty(name, Integer.toString(value));
    }

    /**
     * Test whether this property is present in the given {@code Properties}.
     *
     * @param properties set of properties to check current in
     * @return true if the parameter is specified in the given properties
     */
    public boolean isPresent(Properties properties) {
        return getSetString(properties) != null;
    }

    /**
     * Convert this connection parameter and the value read from the given {@code Properties} into a
     * {@code DriverPropertyInfo}.
     *
     * @param properties properties to take actual value from
     * @return a DriverPropertyInfo representing this connection parameter
     */
    public DriverPropertyInfo toDriverPropertyInfo(Properties properties) {
        DriverPropertyInfo propertyInfo = new DriverPropertyInfo(name, get(properties));
        propertyInfo.required = required;
        propertyInfo.description = description;
        propertyInfo.choices = choices;
        return propertyInfo;
    }

    public static ProxyProperty forName(String name) {
        return PROPS_BY_NAME.get(name);
    }

    /**
     * Return the property if exists but avoiding the default. Allowing the caller to detect the lack
     * of a property.
     *
     * @param properties properties bundle
     * @return the value of a set property
     */
    public String getSetString(Properties properties) {
        Object o = properties.get(name);
        if (o instanceof String) {
            return (String) o;
        }
        return null;
    }
}
