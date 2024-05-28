package com.asia.driver.dynamic.adapt;

import com.asia.driver.dynamic.match.DriverMatcher;

import java.sql.Driver;
import java.util.Properties;

/**
 * @author wangwei
 * @date 2024/02/22
 * 抽象的驱动匹配器适配器，用于适配到合适的匹配器，具体适配逻辑由子类实现
 *
 */
public abstract class AbstractDriverMatcherAdapter implements Adapter<Properties, DriverMatcher> {

    private DriverMatcher matcher;

    private Properties properties;

    public AbstractDriverMatcherAdapter(Properties properties) {
        this.properties = properties;
        this.matcher = adapter(properties);
    }

    public Driver match() {
        return matcher.match(properties);
    }

}
