package com.dgut.clouddisk.interceptor;

import com.dgut.clouddisk.entity.vo.UserTokenVO;
import com.dgut.clouddisk.util.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Author Lai Jiantian
 * @Date 2020/9/21 17:32
 * @Version 1.0
 */
@Configuration
public class LoginInterceptor implements HandlerInterceptor{
    @Autowired
    private JedisPool jedisPool;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String token = request.getHeader("token");
        // 没有token
        if (token==null){
            this.renderJson(response, JsonData.buildCustom(-101,null,"非法访问！！！"));
            return false;
        }
        // 获取redis连接
        Jedis jedis = jedisPool.getResource();
        // 获取token值
        String tokenValue = jedis.get(token);
        // 没有token
        if (tokenValue==null){
            this.renderJson(response,JsonData.buildCustom(-102,null,"请重新登录！！！"));
            jedis.close();
            return false;
        } else {
            // redis中有token
            // 查看ttl值，查看是否过期
            Long ttl = jedis.ttl(token);
            if (ttl<-1){
                // token过期
                this.renderJson(response,JsonData.buildCustom(-103,null,"登录超时！！！"));
                jedis.close();
                return false;
            }else {
                // token未过期，需要重新设置token有效期
                ObjectMapper mapper = new ObjectMapper();
                // 反序列化已封装的token对象
                UserTokenVO userTokenVO = mapper.readValue(tokenValue, UserTokenVO.class);
                // 重新设置有效时间，值为登录时设定
                jedis.expire(token,userTokenVO.getValidTime());
                jedis.close();
                return HandlerInterceptor.super.preHandle(request,response,handler);
            }
        }
    }

    /**
     * 将返回的结果对象转换成JSON
     * @param response
     * @param jsonData
     */
    private void renderJson(HttpServletResponse response, JsonData jsonData) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        try {
            PrintWriter writer = response.getWriter();
            ObjectMapper mapper = new ObjectMapper();
            writer.print(mapper.writeValueAsString(jsonData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
