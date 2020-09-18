package com.imengli.appletServer.dto;

import java.io.Serializable;

public class ResultDTO<T> implements Serializable {

    private static final long serialVersionUID = 7434727470152168503L;

    private int code;

    private String msg;

    private T data;

    public ResultDTO() {
    }

    /**
     * @param resultStatus 通用状态
     */
    public ResultDTO(ResultStatus resultStatus) {
        this(resultStatus.code(), resultStatus.msg(), null);
    }

    /**
     * @param code 提示状态码
     * @param msg  提示内容
     */
    public ResultDTO(int code, String msg) {
        this(code, msg, null);
    }

    /**
     * @param resultStatus 通用状态
     * @param data         数据内容
     */
    public ResultDTO(ResultStatus resultStatus, T data) {
        this(resultStatus.code(), resultStatus.msg(), data);
    }

    public ResultDTO(int code, String msg, T data) {
        super();
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public ResultDTO<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public ResultDTO<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }


    public T getData() {
        return data;
    }

    public ResultDTO<T> setData(T data) {
        this.data = data;
        return this;
    }

}