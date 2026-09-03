package com.example.springboottemplate.annotation;

/**
 * 操作类型：与 oper_log.oper_type 一致
 */
public final class OperTypes {
    public static final int ADD = 1;
    public static final int DELETE = 2;
    public static final int UPDATE = 3;
    public static final int QUERY = 4;

    private OperTypes() {
    }
}
