package com.dgut.clouddisk.controller;


import com.alibaba.fastjson.JSONObject;
import com.dgut.clouddisk.config.ObsConfig;

import com.dgut.clouddisk.service.impl.FileServiceImpl;

import com.dgut.clouddisk.util.JsonData;
import com.obs.services.ObsClient;
import com.obs.services.model.AccessControlList;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/clouddisk/toHtml")
public class TohtmlController {
    //private static String url = "https://obs-laixingzhen-file01.obs.cn-north-4.myhuaweicloud.com/";
    @Resource
    private ObsConfig obsConfig;

    public ObsClient getObsClient() {
        return new ObsClient(obsConfig.getAccessKeyId(), obsConfig.getSecretAccessKey(), obsConfig.getEndpoint());
    }
    //通过拼接域名 --在线预览
    @PostMapping("/toShow")
    public JsonData toShow(@RequestBody JSONObject jsonObject)  {

        String userId = jsonObject.get("userId").toString();
        String objectname = jsonObject.get("objectname").toString();

        String url = "https://"+obsConfig.getBucketName()+"."+obsConfig.getEndpoint()+"/"+ "测试1/"+ userId + objectname ;
        System.out.println(url);
        //中文正常显示
        Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(url);
        String tmp = "";
        while (matcher.find()) {
            tmp = matcher.group();
            try {
                url = url.replaceAll(tmp, URLEncoder.encode(tmp, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //需要设置对象读权限 -- 匿名用户
        // 创建ObsClient实例
        ObsClient obsClient = getObsClient();
        // 设置对象访问权限为公共读
        obsClient.setObjectAcl(obsConfig.getBucketName(), "测试1/"+ userId + objectname, AccessControlList.REST_CANNED_PUBLIC_READ);

        return JsonData.buildSuccess(url);
//        response.setHeader("content-type","text/html;charset=UTF-8");
//
//        //告知浏览器以附件的方式提供下载功能attachment, 而不是解析inline
//        response.setHeader("Content-Disposition","inline;filename="+url);
//
//        try {
//            response.sendRedirect(url);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//

    }


}
