package com.dgut.clouddisk.controller;

import com.alibaba.fastjson.JSONObject;
import com.dgut.clouddisk.service.impl.EmailServiceImpl;
import com.dgut.clouddisk.util.Email;
import com.dgut.clouddisk.util.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.UUID;

/**
 * @Author Lai Jiantian
 * @Date 2020/9/22 19:55
 * @Version 1.0
 */
@RestController
@RequestMapping("/clouddisk/email")
public class EmailController {
    // Jdeis连接池
    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private EmailServiceImpl emailService;

    // 邮箱验证码Redis上默认有效时间
    @Value("${mail.default_code_valid_time}")
    private Integer valid_time;

    @Value("${mail.default_send_interval}")
    private Integer send_interval;

    @RequestMapping(value = "/send")
    public JsonData send(@RequestBody JSONObject jsonObject) throws MessagingException, JsonProcessingException {
        // 获取Redis连接池对象
        Jedis jedis = jedisPool.getResource();
        String email= jsonObject.get("Email").toString();
        if (email==null){
            return JsonData.buildError("请正确输入邮箱地址");
        }
        String redisValue = jedis.get(email);
        Long ttl = jedis.ttl(email);
        if (redisValue!=null&&ttl>(valid_time-send_interval)){
            // 验证码不为空且发送间隔大于 60s
            jedis.close();
            return JsonData.buildError(-2,"已发送邮件");
        }else{
            // 生成验证码
            String code=UUID.randomUUID().toString().substring(0,6);
            Boolean sendMail = emailService.sendMail(new Email(email.toString(),
                    "账号安全中心-验证码",
                    code));
            if (sendMail){
                // 将需要保存到redis服务器的对象，转换成json字符串
                ObjectMapper mapper = new ObjectMapper();
                String jsonRst = mapper.writeValueAsString(code);
                // 写入token和对应的用户对象
                jedis.set(email,jsonRst);
                // 设置有效时间
                jedis.expire(email,valid_time);
                jedis.close();
                return JsonData.buildSuccess(email);
            }else {
                return JsonData.buildError("邮件发送失败");
            }
        }
    }
}
