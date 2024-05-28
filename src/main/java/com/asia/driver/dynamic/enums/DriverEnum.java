package com.asia.driver.dynamic.enums;



import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DriverEnum {

    POSTGRES_42_1_1("postgresql", "42.1.1", "org.postgresql.Driver", "lib/driver/postgresql/42_1_1/postgresql-42.1.1.jar"),
    POSTGRES_42_2_26("postgresql", "42.2.26", "org.postgresql.Driver", "lib/driver/postgresql/42_1_1/postgresql-42.1.1.jar"),
    MYSQL_5_1_6("mysql", "5.1.6", "com.mysql.jdbc.Driver", "lib/driver/mysql/5_1_6/mysql-connector-java-5.1.6.jar"),
    MYSQL_5_1_47("mysql", "5.1.47", "com.mysql.jdbc.Driver", "lib/driver/mysql/5_1_6/mysql-connector-java-5.1.47.jar"),
    MYSQL_8_0_19("mysql", "8.0.19", "com.mysql.cj.jdbc.Driver", "lib/driver/mysql/5_1_6/mysql-connector-java-8.0.19.jar");



    private String dbType;
    private String jarVersion;
    private String className;
    private String defaultJarPath;

    public static DriverMatcher getMatcher(){
         return new DriverMatcher();
    }

    public String getJarVersion() {
        return jarVersion;
    }

    public void setJarVersion(String jarVersion) {
        this.jarVersion = jarVersion;
    }

    public String getDefaultJarPath() {
        return defaultJarPath;
    }

    public void setDefaultJarPath(String defaultJarPath) {
        this.defaultJarPath = defaultJarPath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }


    DriverEnum(String dbType, String jarVersion, String className, String defaultJarPath) {
        this.dbType = dbType;
        this.jarVersion = jarVersion;
        this.className = className;
        this.defaultJarPath = defaultJarPath;
    }

  public static class DriverMatcher {
        private List<DriverEnum> drivers = Arrays.asList(DriverEnum.values());

        public DriverMatcher() {
        }

        public DriverMatcher findByDbType(String dbType) {
            drivers = drivers.stream().filter(driverEnum -> driverEnum.getDbType().equals(dbType)).collect(Collectors.toList());
            return this;
        }

        public DriverMatcher findByJarVersion(String jarVersion) {
            drivers = drivers.stream().filter(driverEnum -> driverEnum.getJarVersion().equals(jarVersion)).collect(Collectors.toList());
            return this;
        }

        public DriverMatcher findByClassName(String className) {
            drivers = drivers.stream().filter(driverEnum -> driverEnum.getClassName().equals(className)).collect(Collectors.toList());
            return this;
        }

        public DriverMatcher findByDefaultJarPath(String defaultJarPath) {
            drivers = drivers.stream().filter(driverEnum -> driverEnum.getDefaultJarPath().equals(defaultJarPath)).collect(Collectors.toList());
            return this;
        }

        public List<DriverEnum> get(){
            return drivers;
        }
    }
}
