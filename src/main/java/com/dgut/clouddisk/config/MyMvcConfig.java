package com.dgut.clouddisk.config;

import com.dgut.clouddisk.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Author Lai Jiantian
 * @Date 2020/9/21 17:28
 * @Version 1.0
 */
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {
    @Autowired
    LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //增加登录拦截器
//        registry.addInterceptor(loginInterceptor).addPathPatterns("/clouddisk/user/**")
//                .excludePathPatterns("/clouddisk/user/isEmail");
    }

    // 解决跨域
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("*")
//                .allowCredentials(true)
//                .allowedMethods("GET", "POST", "DELETE", "PUT")
//                .maxAge(3600);
//    }
}
