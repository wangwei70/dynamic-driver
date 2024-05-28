package com.asia.driver.dynamic.config;

import com.asia.driver.dynamic.bean.DbInfo;
import com.asia.driver.dynamic.exception.ParseError;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;


@Slf4j
/**
 * @author wangwei
 * @date 2024/02/22
 * 用于解析配置的键
 */
public class KeyParser implements Parser {

    private static final String SPERATE_CHAR = "\\.";

    private Properties properties;

    private HashMap<String,DbInfo> dbInfos;


    public KeyParser(Properties properties) {
        this.properties = properties;
    }

    /**
     * 解析properties配置文件
     *
     * @return
     */
    @Override
    public HashMap<String,DbInfo> parse() {

        try {
            if (dbInfos==null && properties!=null) {
                dbInfos=new HashMap<>();
                Set<Object> keys = properties.keySet();
                for (Object key : keys) {
                    String[] split = ((String)key).split(SPERATE_CHAR);
                    DbInfo dbInfo= DbInfo.builder().dbType(split[0]).dbVersion(split[1].replace("_",".")).build();
                    dbInfos.put((String)key,dbInfo);
                }
            }
            return dbInfos;
        }catch (Exception e){
            log.error(e.getMessage());
            throw new ParseError(e);
        }
    }




}
