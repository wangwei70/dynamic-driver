package com.asia.driver.dynamic.bean;

import lombok.Builder;
import lombok.Data;

import java.net.URLClassLoader;
import java.sql.Driver;

/**
 * @author wangwei
 * @date 2024/02/21
 *  用于封装外部驱动包信息
 *
 */
@Data
@Builder
public class DriverInfo {
    private String jarConfigPath;
    private String jarAbsolutePath;
    private String className;
    private String jarVersion;
    private String dbType;
    private URLClassLoader classLoader;
    private Driver driver;

}
