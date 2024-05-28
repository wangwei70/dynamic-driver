package com.asia.driver.dynamic.enums;

public enum DBTypeEnum {
    POSTGRES("postgres"),
    MYSQL("mysql");

    private String type;

    DBTypeEnum(String type) {
        this.type = type;
    }
}
