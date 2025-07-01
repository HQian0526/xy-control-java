package com.example.springboottemplate.dto;

public class Response {
    private Integer code; //返回状态码
    private Object data; //返回的数据内容
    private String msg; //返回的中文

    public Response() {
    }

    public Response(Integer code, Object data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Response{" +
                "code=" + code +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }

    /**
     * 成功响应（无数据）
     */
    public static Response success() {
        return new Response(200, null, "操作成功");
    }

    /**
     * 成功响应（带数据）
     */
    public static Response success(Object data) {
        return new Response(200, data, "操作成功");
    }

    /**
     * 失败响应（默认500错误码）
     */
    public static Response fail(String msg) {
        return new Response(500, null, msg);
    }

    /**
     * 自定义失败响应
     */
    public static Response fail(Integer code, String msg) {
        return new Response(code, null, msg);
    }
}
