package com.dgut.clouddisk.util;
/**
 *
 * @ClassName ErrorCode
 * @Description 错误代码和信息
 **/
public enum StatusCode {

    DEFAULT_ERROR(1000, "默认错误代码"),

    SUCCESS(2000, "成功"),

    PARAM_IS_EMPTY(3001, "参数不能为空"),

    FILE_IS_NOT_EXISTED(3002, "文件/文件夹不存在"),

    FILE_IS_EXISTED(3003, "文件/文件夹已经存在！"),

    NOT_ACCESS(3004, "没有执行该操作的权限"),

    IS_OVER_DIR_MAX_LEVEL(3005, "已经超过最大层数，不能再新建文件夹！"),

    AUTHORITY_IS_NOT_EXISTED(3006, "权限不存在！"),

    PARAM_VALIDATE_FAILED(4000, "参数校验不通过"),

    DATABASE_ERROR(5000, "数据库错误！"),

    OBS_ERROR(6000, "obs错误代码"),

    REDIS_ERROR(7000, "redis错误"),

    SERVER_ERROR(8001, "服务端发生错误"),

    CLIENT_ERROR(8002, "客户端请求发生错误"),

    MAIL_OR_PHONE_IS_EXISTED(9000,"邮箱或电话号码已存在"),

    FILE_FORMAT_ERROR(9001,"文件格式错误"),

    FILE_READ_ERROR(9002,"文件读取失败"),

    FILE_IS_EMPTY(9003,"文件内容为空"),

    USER_IS_EMPTY(9004,"员工为空"),

    USER_IS_EXISTED(9005,"用戶已存在"),

    PHONE_IS_EXISTED(9006,""),

    PHONE_FORMAT_ERROR(9007,""),

    CONTENT_HAVE_EMPTY(9008,"");


    private Integer code;

    private String message;

    StatusCode(Integer code, String message){
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return code;
    }

    public String message() {
        return message;
    }
}
