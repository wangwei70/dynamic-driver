package com.asia.driver.dynamic.bean;


import com.asia.driver.dynamic.enums.DriverEnum;
import lombok.Data;

@Data
public class DbMappingDriver {
    private DriverEnum jarEnum;
    private String dbType;
    private String dbVersion;
}
