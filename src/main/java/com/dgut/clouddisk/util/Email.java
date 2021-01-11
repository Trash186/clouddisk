package com.dgut.clouddisk.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Lai Jiantian
 * @Date 2020/9/22 19:27
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {
    private String to;        // 接收邮箱地址
    private String subject;   // 邮箱主题
    private String msg;       // 邮箱内容（验证码，修改类型）

}
