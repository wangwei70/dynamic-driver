package com.asia.driver.dynamic.adapt;

import com.asia.driver.dynamic.adapt.Adapter;
import com.asia.driver.dynamic.enums.ProxyProperty;
import com.asia.driver.dynamic.exception.MatchAdaptError;
import com.asia.driver.dynamic.match.BasicDriverMatcher;
import com.asia.driver.dynamic.match.DriverMatcher;

import java.sql.Driver;
import java.util.Properties;

public class BasicDriverMatcherAdapter extends AbstractDriverMatcherAdapter {

    public BasicDriverMatcherAdapter(Properties properties) {
        super(properties);
    }

    @Override
    public DriverMatcher adapter(Properties properties) {
        try {
            // 获取真实驱动
            String dbVersion = ProxyProperty.DB_VERSION.get(properties);
            String dbType = ProxyProperty.DB_TYPE.get(properties);
            if (dbVersion != null && dbType != null) {
                return new BasicDriverMatcher();
            }
            return null;
        }catch (Exception e){
            throw new MatchAdaptError(e);
        }
    }


}
