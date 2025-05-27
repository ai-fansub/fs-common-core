package com.fs.common.core.exception;

public enum ErrorType {

    ERROR(0, "FS_ER_"), // 기본 Basic 에러
    ERROR_SYSTEM(1, "FS_SY_"), // 시스템 에러
    ERROR_SQL(2, "FS_SQ_"), // SQL 에러
    ERROR_COMMON(3, "FS_CM_"), // 공통 에러
    ERROR_EXCEL(4, "FS_EX_"); // 엑셀 에러


    private int type;
    private String name;

    ErrorType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

}
