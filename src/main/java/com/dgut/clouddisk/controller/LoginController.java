package com.dgut.clouddisk.controller;

import com.alibaba.fastjson.JSONObject;
import com.dgut.clouddisk.entity.User;
import com.dgut.clouddisk.entity.vo.UserTokenVO;
import com.dgut.clouddisk.service.impl.UserServiceImpl;
import com.dgut.clouddisk.util.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.issCollege.util.MD5;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @Author Lai Jiantian
 * @Date 2020/9/21 15:51
 * @Version 1.0
 */
@RestController
@RequestMapping("/clouddisk/")
public class LoginController {
    // Jdeis连接池
    @Autowired
    private JedisPool jedisPool;

    // redis默认超时时间
    @Value("${server.default_token_valid_time}")
    private Integer defaultTokenVaildTime;

    @Resource
    private UserServiceImpl userService;

    /**
     * 登录
     * @param jsonObject 传 username password    username为用户名或邮箱
     * @param request httpRequest
     * @param response httpResponse
     * @return 登录结果
     * @throws JsonProcessingException JSON序列化异常
     */
    @RequestMapping("/login")
    public JsonData login(@RequestBody(required = false) JSONObject jsonObject, HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {

        User user=new User();
        String username = jsonObject.get("username").toString();
        String userPwd = jsonObject.get("password").toString();
        if (username==null|| username.equals("") ||userPwd==null|| userPwd.equals("")){
            return JsonData.buildError("请输入账号密码");
        }
        // 判断传入的是用户名还是邮箱地址
        if (username.contains("@")){
            user.setUserEmail(username);
        }else {
            user.setUserName(username);
        }
        // 获取Redis连接池对象
        Jedis jedis = jedisPool.getResource();
        // 用户密码MD5加密
        String md5 = MD5.stringMD5(userPwd);
        user.setUserPwd(md5);
        // 获取header中的token
        String headerToken = request.getHeader("token");
        if (headerToken != null) {
            // header中有token，向Redis中查
            // 获取token在Redis的值
            String tokenVal = jedis.get(headerToken);
            if (tokenVal == null || (jedis.ttl(headerToken) < -1)) {
                // Redis中没有该token或token已过期，往DB层查
                return findInDB(user, response, jedis);
            } else {
                ObjectMapper mapper = new ObjectMapper();
                // 反序列化已封装的token对象
                UserTokenVO userTokenVO = mapper.readValue(tokenVal, UserTokenVO.class);
                jedis.close();
                return JsonData.buildSuccess(userTokenVO);
            }
        } else {
            // header中没有token，往DB层查
            return findInDB(user, response, jedis);
        }

    }

    /**
     * 向DB持久层查用户信息
     *
     * @param user     用户信息
     * @param response Response
     * @param jedis    Redis连接池对象
     * @return 封装成JSON的查找结果
     * @throws JsonProcessingException JSON处理异常
     */
    private JsonData findInDB(@RequestBody User user, HttpServletResponse response, Jedis jedis) throws JsonProcessingException {
        if (user == null) {
            return JsonData.buildError("请输入账号密码");
        } else {
            User login = userService.login(user);
            if (login != null) {
                // 生成独立的访问令牌
                String token = UUID.randomUUID().toString().replace("-", "".toLowerCase());
                // 将需要保存到redis服务器的对象，转换成json字符串
                ObjectMapper mapper = new ObjectMapper();
                UserTokenVO userTokenVO = new UserTokenVO(login, defaultTokenVaildTime);
                String jsonRst = mapper.writeValueAsString(userTokenVO);
                // 写入token和对应的用户对象
                jedis.set(token, jsonRst);
                // 设置有效时间
                jedis.expire(token, defaultTokenVaildTime);
                // 关闭连接
                jedis.close();
                response.setHeader("token", token);
                return JsonData.buildSuccess(login);
            } else {
                return JsonData.buildError("账号或密码错误");
            }
        }
    }

    @RequestMapping("/logout")
    public JsonData logout(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader("token");
        if (token != null) {
            // 如果Header中有token，删除该Token在Redis上的值
            // 获取Redis连接池对象
            Jedis jedis = jedisPool.getResource();
            jedis.del(token);
            jedis.close();
        }
        // 设置header中的token值为空
        response.setHeader("token", null);
        return JsonData.buildSuccess(null);
    }
}
