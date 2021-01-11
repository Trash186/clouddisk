package com.dgut.clouddisk.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.dgut.clouddisk.service.EmailService;
import com.dgut.clouddisk.util.Email;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @Author Lai Jiantian
 * @Date 2020/9/22 19:35
 * @Version 1.0
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Resource
    TemplateEngine templateEngine;


    //邮件发件人
    @Value("${mail.fromMail.addr}")
    private String from;

    // Jdeis连接池
    @Autowired
    private JedisPool jedisPool;

    @Override
    public Boolean sendMail(Email email) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        // 邮件正文
        Context context = new Context();
        context.setVariable("verifyCode", email.getMsg());
        // 将模版引擎解析成html字符串
        String emailTemplate = templateEngine.process("codeTemplate", context);
        return send(email, mimeMessage, helper, emailTemplate);
    }

    @Override
    public Boolean sendSuccess(Email email) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String time = df.format(new Date());
        // 邮件正文
        Context context = new Context();
        context.setVariable("time", time);
        context.setVariable("msg", email.getMsg());
        // 将模版引擎解析成html字符串
        String emailTemplate = templateEngine.process("successTemplate", context);
        return send(email, mimeMessage, helper, emailTemplate);
    }

    @Override
    public Boolean verifyEmail(JSONObject map) throws JsonProcessingException {
        String email =  map.get("Email").toString();
        String code =  map.get("Code").toString();
        if (email == null || code == null) {
            return false;
        }
        Jedis jedis = jedisPool.getResource();
        ObjectMapper mapper = new ObjectMapper();
        if ((jedis.ttl(email) < -1) || (jedis.get(email) == null)) {
            // 如果邮箱在Redis上已过期或不存在
            return false;
        }
        String readValue = mapper.readValue(jedis.get(email), String.class);
        if (readValue == null || !readValue.equals(code)) {
            // Redis上value为空 或 value不等于验证码 或 有效时间过期 返回false
            jedis.close();
            return false;
        } else {
            jedis.expire(email,10);
            jedis.close();
            return true;
        }
    }

    /**
     * 发送邮件
     *
     * @param email         接收邮箱
     * @param mimeMessage   mimeMessage
     * @param helper        helper
     * @param emailTemplate 邮箱模板
     * @return 是否发送成功
     * @throws MessagingException 邮箱发送异常
     */
    private Boolean send(Email email, MimeMessage mimeMessage, MimeMessageHelper helper, String emailTemplate) throws MessagingException {
        helper.setFrom(from);
        helper.setTo(email.getTo());
        helper.setSubject(email.getSubject());
        helper.setText(emailTemplate, true);
        try {
            mailSender.send(mimeMessage);
        } catch (MailException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
