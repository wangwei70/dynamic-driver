package com.asia.driver.dynamic.config;


import com.asia.driver.dynamic.bean.DriverInfo;
import com.asia.driver.dynamic.enums.DriverEnum;
import com.asia.driver.dynamic.exception.CanNotFindError;
import com.asia.driver.dynamic.exception.ValueFormatError;
import com.asia.driver.dynamic.utils.ProxyInfo;
import lombok.extern.slf4j.Slf4j;


import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author wangwei
 * @date 2024/02/22
 * 用于解析配置的值
 */
@Slf4j
public class ValueParser implements Parser<HashMap<String, DriverInfo>> {


    // 目录的前缀（父目录）
    private static final String VALUE_PREFIX = "driver";

    private static final String VALUE_SUFFIX = ".jar";

    // 目录的层级
    private static final int DIRECTORY_HIERARCHY = 3;


    private Properties properties;

    private HashMap<String, DriverInfo> driverInfos;

    public ValueParser() {

    }

    public ValueParser(Properties properties) {
        this.properties = properties;
    }

    /**
     * 解析properties配置文件
     *
     * @return
     */
    private DriverInfo parse(String value) {
        checkValue(value);
        String[] split = value.split(ProxyInfo.SPERATE_CHAR);

        DriverInfo driverInfo = DriverInfo.builder()
                .dbType(split[0])
                .jarVersion(split[1].replaceAll("_", "."))
                .jarConfigPath(value)
                .build();

        List<String> driverPathes = ProxyInfo.DRIVER_PATHES;
        for (int i = 0; i < driverPathes.size(); i++) {
            // 搜索获取jar的全路径
            String configPath = driverPathes.get(i);
            String jarAbsolutePath = configPath + File.separator + value;
            File file = new File(jarAbsolutePath);
            if (file.exists()) {
                driverInfo.setJarAbsolutePath(jarAbsolutePath);
                break;
            }
        }
        if (driverInfo.getJarAbsolutePath() == null) {
            log.error("please check jar exists : " + value);
            throw new CanNotFindError("please check jar exists : " + value);
        }

        // 根据数据里类型和驱动版本，获取驱动枚举
        List<DriverEnum> driverEnums = DriverEnum.getMatcher().findByDbType(driverInfo.getDbType()).findByJarVersion(driverInfo.getJarVersion()).get();

        if (driverEnums == null || driverEnums.size() == 0) {
            throw new CanNotFindError(value);
        }

        driverInfo.setClassName(driverEnums.get(0).getClassName());

        return driverInfo;
    }


    private static void checkValue(String value) {
        String[] split = value.split(ProxyInfo.SPERATE_CHAR);
        if (split == null || split.length != DIRECTORY_HIERARCHY) {
            throw new ValueFormatError("number of hierarchy must be:" + DIRECTORY_HIERARCHY);
        }
//        if (!split[0].equals(VALUE_PREFIX)) {
//            throw new ValueFormatError("jar path must start with :" + VALUE_PREFIX);
//        }
        if (!value.endsWith(VALUE_SUFFIX)) {
            throw new ValueFormatError("jar path must end with :" + VALUE_SUFFIX);
        }
    }


    @Override
    public HashMap<String, DriverInfo> parse() {
        driverInfos = new HashMap<>();
        Set<Object> keys = properties.keySet();
        for (Object key : keys) {
            DriverInfo driverInfo = parse((String) properties.get(key));
            driverInfos.put((String) key, driverInfo);
        }
        return driverInfos;
    }
}
