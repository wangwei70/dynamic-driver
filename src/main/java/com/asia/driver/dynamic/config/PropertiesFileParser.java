package com.asia.driver.dynamic.config;


import com.asia.driver.dynamic.bean.DbInfo;
import com.asia.driver.dynamic.bean.DriverInfo;
import com.asia.driver.dynamic.bean.DriverMapping;
import com.asia.driver.dynamic.exception.ConfigParseError;
import com.asia.driver.dynamic.loader.LoaderContext;
import com.asia.driver.dynamic.utils.Ordered;
import com.asia.driver.dynamic.utils.ProxyInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

@Slf4j
public class PropertiesFileParser implements Parser<List<DriverMapping>> {

    private ConfigFiles configFiles;

    private Properties properties;

    private List<DriverMapping> driverMappings;

    private HashMap<String, DriverInfo> jarMap;


    public PropertiesFileParser() {
        this.configFiles = new ConfigFiles();
        for (int i = 1; i < ProxyInfo.DRIVER_PATHES.size(); i++) {
            configFiles.addFile(i, ProxyInfo.DRIVER_PATHES.get(i));
        }
    }



    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public List<DriverMapping> getDriverMappings() {
        return driverMappings;
    }

    public void setDriverMappings(List<DriverMapping> driverMappings) {
        this.driverMappings = driverMappings;
    }

    /**
     * 解析properties配置文件
     *
     * @return
     */
    @Override
    public List<DriverMapping> parse() {
        if (this.properties == null) {
            log.info("-------------------------- start parse properties file --------------------------");
            try {
                this.properties = configFiles.loadAll();
                LoaderContext.addDriverMappings(parseDriverMapping());
                LoaderContext.putDriverInfo(this.jarMap);
                for (DriverMapping driverMapping : driverMappings) {
                    log.debug("driver config mapping:"+driverMapping.toString());
                }
                log.info("-------------------------- finish parse properties file --------------------------");
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new ConfigParseError(e.getCause());
            }
        }
        return driverMappings;
    }


    /**
     * 解析数据库和驱动包的映射关系
     *
     * @return
     */
    private List<DriverMapping> parseDriverMapping() {
        this.driverMappings = new ArrayList<>();
        this.jarMap = new HashMap<>();
        Enumeration<Object> keys = properties.keys();

        KeyParser keyParser = new KeyParser(properties);
        ValueParser valueParser = new ValueParser(properties);

        HashMap<String, DbInfo> dbInfos = keyParser.parse();
        HashMap<String, DriverInfo> DriverInfos = valueParser.parse();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            DbInfo dbInfo = dbInfos.get(key);
            DriverInfo driverInfo = DriverInfos.get(key);

            this.jarMap.put(driverInfo.getJarConfigPath(), driverInfo);
            DriverMapping driverMapping = new DriverMapping(dbInfo, driverInfo);
            this.driverMappings.add(driverMapping);
        }
        return this.driverMappings;
    }


    static class ConfigFile implements Ordered {

        private int order;

        private String path;

        public String getPath() {
            return path;
        }

        public ConfigFile(int order, String path) {
            this.order = order;
            this.path = path;
        }

        boolean exists() {
            try {
                if (path == null) {
                    return false;
                }

                File file = new File(path);
                if (!file.exists()) {
                    return false;
                }

                if (file.isDirectory()) {
                    path = path + File.separator + ProxyInfo.DEFAULT_FILE_NAME;
                    return exists();
                }

                return true;
            } catch (Exception e) {
                throw new ConfigParseError(e);
            }
        }

        @SneakyThrows
        Properties load() {
            if (exists()) {
                InputStream fileStream = new FileInputStream(path);
                Properties properties = new Properties();
                properties.load(fileStream);
                return properties;
            } else {
                throw new FileNotFoundException(path);
            }

        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    static class ConfigFiles {
        private ArrayList<ConfigFile> files = new ArrayList<>();

        ConfigFiles addFile(int order, String path) {
            ConfigFile configFile = new ConfigFile(order, path);
            this.files.add(configFile);
            return this;
        }

        /**
         * 根据排序加载一个配置文件
         *
         * @return
         */
        public Properties loadOne() {
            // 升序
            files.sort((file1, file2) -> {
                return file1.getOrder() - file2.getOrder();
            });
            for (int i = 0; i < files.size(); i++) {
                ConfigFile configFile = files.get(i);
                if (configFile.exists()) {
                    log.info("Load config from : " + configFile.getPath());
                    return configFile.load();
                }
            }
            throw new ConfigParseError("No available configuration files found");
        }


        /**
         * 根据排序加载多个配置文件
         *
         * @return
         */
        public Properties loadAll() {
            // 降序，优先级高的配置会覆盖低的配置
            files.sort((file1, file2) -> {
                return file2.getOrder() - file1.getOrder();
            });
            Properties properties = new Properties();
            for (int i = 0; i < files.size(); i++) {
                ConfigFile configFile = files.get(i);
                if (configFile.exists()) {
                    log.info("Load config from : " + configFile.getPath());
                    properties.putAll(configFile.load());

                }
            }
            return properties;
        }
    }

}
