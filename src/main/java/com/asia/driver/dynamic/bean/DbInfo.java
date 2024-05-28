package com.asia.driver.dynamic.bean;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class DbInfo {
    private String dbType;
    private String dbVersion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbInfo)) return false;
        DbInfo dbInfo = (DbInfo) o;
        return Objects.equals(getDbType(), dbInfo.getDbType()) &&
                Objects.equals(getDbVersion(), dbInfo.getDbVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDbType(), getDbVersion());
    }
}
