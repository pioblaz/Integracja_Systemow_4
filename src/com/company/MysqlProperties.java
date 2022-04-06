package com.company;

public enum MysqlProperties {
    DB_CONNECTION("jdbc:mysql://localhost:3306/integracja?serverTimezone=UTC&useUnicode=yes&characterEncoding=UTF-8"),
    DB_USER("root"),
    DB_PASSWORD("");

    private final String value;

    private MysqlProperties(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
