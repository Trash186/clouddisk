package com.dgut.clouddisk.controller;


import com.dgut.clouddisk.config.ObsConfig;
import com.dgut.clouddisk.entity.User;
import com.dgut.clouddisk.entity.vo.UserTokenVO;
import com.dgut.clouddisk.service.UserService;
import com.dgut.clouddisk.service.impl.ObsServiceImpl;
import com.dgut.clouddisk.util.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obs.services.ObsClient;
import com.obs.services.model.ObsBucket;
import com.obs.services.model.ObsObject;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/clouddisk/obs")
public class ObsController {
    private ObsClient obsClient;
    @Autowired
    ObsConfig obsConfig;
    @Resource
    private JedisPool jedisPool;

    @Resource
    private UserService userService;
    @Resource
    private ObsServiceImpl obsService;
    //文件夹路径
    final String basepath = "/test";
    public ObsController() {
    }
    @RequestMapping("list_bucket")
    public JsonData getAllObsBucket() {
        try {
            List<ObsBucket> list = obsService.getAllObsBucket();
            if(list!=null && list.size()>0) {
                return JsonData.buildSuccess(list);
            }else {
                return JsonData.buildError("没有数据");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return JsonData.buildError("obs存储访问异常");
        }
    }

    @RequestMapping("list_object")
    public JsonData getAllObject() {
        try {
            List<ObsObject> list = obsService.getAllObsObject();
            if (list != null && list.size() > 0) {
                return JsonData.buildSuccess(list);
            } else {
                return JsonData.buildError("没有对象存储");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return JsonData.buildError("obs存储访问异常");
        }
    }






}


