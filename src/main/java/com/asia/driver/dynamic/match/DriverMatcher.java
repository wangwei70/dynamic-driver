package com.asia.driver.dynamic.match;


import java.sql.Driver;
import java.util.Properties;

/**
 * @author wangwei
 * @date 2024/02/22
 *  驱动匹配器，用于匹配合适的驱动
 *
 */
public interface DriverMatcher {
     // 根据属性值进行驱动的匹配
     Driver match(Properties properties);
}
