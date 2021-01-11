package com.dgut.clouddisk.service;

import com.alibaba.fastjson.JSONObject;
import com.dgut.clouddisk.util.Email;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.mail.MessagingException;
import java.util.Map;

/**
 * @Author Lai Jiantian
 * @Date 2020/9/22 19:31
 * @Version 1.0
 */
public interface EmailService {
    /**
     * 发送邮箱验证码
     * @param email 发送内容
     * @throws MessagingException
     */
    Boolean sendMail(Email email) throws MessagingException;

    /**
     * 发送修改成功提示
     * @param email 发送内容
     * @throws MessagingException 邮箱异常
     */
    Boolean sendSuccess(Email email) throws MessagingException;

    Boolean verifyEmail(JSONObject map) throws JsonProcessingException;

}
