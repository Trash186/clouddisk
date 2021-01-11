package com.dgut.clouddisk.exception;

/**
 * @ClassName CloudException
 * @Description 自定义错误类
 */
public class CloudException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;

    public CloudException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
