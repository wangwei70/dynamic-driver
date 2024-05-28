package com.asia.driver.dynamic.bean;

import com.asia.driver.dynamic.exception.CanNotFindError;
import com.asia.driver.dynamic.exception.JarPathError;
import com.asia.driver.dynamic.exception.ParamError;
import lombok.Data;


import java.io.File;
import java.net.MalformedURLException;

/**
 * @date 2024/2/1
 * 封装数据库和驱动包的映射关系
 *
 */
@Data
public class DriverMapping {
    private DbInfo dbInfo;
    private DriverInfo driverInfo;
//    private URLClassLoader classLoader;


    private DriverMapping() {
    }

    public DriverMapping(DbInfo dbInfo, DriverInfo driverInfo) {
        this.dbInfo = dbInfo;
        this.driverInfo = driverInfo;
    }



//    public URLClassLoader getClassLoader() {
//            URL url = new URL(jarConfigPath);
//            DriverClassLoader ucl = new DriverClassLoader(new URL[]{url});
//            driverMapping.setClassLoader(ucl);
//        return classLoader;
//    }

    class DriverMappingBuilder{
        private String dbType;
        private String dbVersion;
        private String jarPath;
        private String className;
        private String jarVersion;



        public DriverMappingBuilder dbType(String dbType){
            this.dbType=dbType;
            return this;
        }

        public DriverMappingBuilder dbVersion(String dbVersion){
            this.dbVersion=dbVersion;
            return this;
        }

        public DriverMappingBuilder jarPath(String jarPath){
            checkJarPath();
            this.jarPath=jarPath;
            return this;
        }

        public DriverMappingBuilder className(String className){
            this.className=className;
            return this;
        }

        public DriverMappingBuilder jarVersion(String jarVersion){
            this.jarVersion=jarVersion;
            return this;
        }

        public DriverMapping build() throws MalformedURLException {
            if (dbType==null||dbType.trim()==""){
                throw new ParamError("dbType must not be null");
            }
            if (dbVersion==null||dbVersion.trim()==""){
                throw new ParamError("dbVersion must not be null");
            }
            if (jarPath==null||jarPath.trim()==""){
                throw new ParamError("jarConfigPath must not be null");
            }
            if (className==null||className.trim()==""){
                throw new ParamError("className must not be null");
            }

            DriverMapping driverMapping = new DriverMapping();
            DbInfo dbInfo = DbInfo.builder()
                    .dbType(dbType)
                    .dbVersion(dbVersion)
                    .build();
            DriverInfo driverInfo = DriverInfo.builder()
                    .className(className)
                    .jarConfigPath(jarPath)
                    .jarVersion(jarVersion)
                    .build();

            driverMapping.setDbInfo(dbInfo);
            driverMapping.setDriverInfo(driverInfo);


//            // 获取类加载器
//            URL url = new URL(jarConfigPath);
//            DriverClassLoader ucl = new DriverClassLoader(new URL[]{url});
//            driverMapping.setClassLoader(ucl);
            return driverMapping;
        }

        private void checkJarPath() {
            if (!jarPath.endsWith(".jar")){
                throw new JarPathError();
            }
            File file = new File(jarPath);
            if (!file.exists()){
                throw new CanNotFindError();
            }
        }

    }
}
