package com.asia.driver.dynamic.match;

import com.asia.driver.dynamic.bean.DbInfo;
import com.asia.driver.dynamic.bean.DriverMapping;
import com.asia.driver.dynamic.enums.ProxyProperty;
import com.asia.driver.dynamic.exception.CanNotFindError;
import com.asia.driver.dynamic.loader.LoaderContext;
import lombok.extern.slf4j.Slf4j;

import java.sql.Driver;
import java.util.Properties;

/**
 * @author wangwei
 * @date 2024/02/22
 * 基础驱动匹配器，使用数据库的版本和数据库类型进行驱动匹配
 */
@Slf4j
public class BasicDriverMatcher implements DriverMatcher {


    @Override
    public Driver match(Properties properties) {
        try {

            // 获取真实驱动
            String dbVersion = ProxyProperty.DB_VERSION.get(properties);
            if (dbVersion == null) {
                throw new RuntimeException("The URL must include parameters for the 'dbversion'");
            }

            String dbType = ProxyProperty.DB_TYPE.get(properties);
            if (dbType == null) {
                throw new RuntimeException("unknown database type ");
            }

            DbInfo dbInfo = DbInfo.builder().dbType(dbType).dbVersion(dbVersion).build();
            log.info("begin match driver for :" + dbInfo);
            DriverMapping targetDriverMapping = null;
            for (DriverMapping driverMapping : LoaderContext.getDriverMappings()) {
                if (dbInfo.equals(driverMapping.getDbInfo())) {
                    if (driverMapping.getDriverInfo().getClassLoader() == null) {
                        // 初始化类加载器和驱动
                        synchronized (driverMapping) {
                            if (driverMapping.getDriverInfo().getClassLoader() == null) {
                                LoaderContext.initClassLoader(driverMapping.getDriverInfo());
                                LoaderContext.initDriver(driverMapping.getDriverInfo());
                            }
                        }
                    }
                    targetDriverMapping = driverMapping;
                    break;
                }
            }
            if (targetDriverMapping == null) {
                throw new CanNotFindError(dbInfo.toString());
            }
            log.info("choose driver mapping:" + targetDriverMapping.getDriverInfo());
            return targetDriverMapping.getDriverInfo().getDriver();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("not suitble driver be found :" + e);
        }

    }
}
