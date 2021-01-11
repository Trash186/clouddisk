package com.dgut.clouddisk.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Lai Jiantian
 * @Date 2020/9/21 13:16
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonData {
    private Integer code;
    private String message;
    private Object data;

    /**
     * 返回成功JSON
     *
     * @param object 成功返回的对象
     * @return 状态码 1，提示语 ， 对象
     */
    public static JsonData buildSuccess(Object object) {
        return new JsonData(1, "Suceesee", object);
    }



    /**
     * 返回失败JSON
     * @param str 失败提示语
     * @return 状态码 -1 提示语 对象
     */
    public static JsonData buildError(String str){
        return new JsonData(-1,str,null);
    }

    public static JsonData buildError() {
        return buildError(StatusCode.DEFAULT_ERROR.code(), "失败");
    }

    public static JsonData buildError(Integer code, String message) {
        JsonData jsonData = new JsonData();
        jsonData.setCode(code);
        jsonData.setMessage(message);
        return jsonData;
    }

    /**
     * 返回自定义JSON
     * @param code 自定义状态码
     * @param str 自定义提示语
     * @param obj 返回的对象
     * @return 返回自定义设置
     */
    public static JsonData buildCustom(Integer code, String str, Object obj){
        return new JsonData(code,str,obj);
    }

}

