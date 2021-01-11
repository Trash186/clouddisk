package com.dgut.clouddisk.controller;


import com.dgut.clouddisk.entity.File;
import com.dgut.clouddisk.entity.Share;
import com.dgut.clouddisk.entity.vo.ShareVO;
import com.dgut.clouddisk.mapper.ShareMapper;
import com.dgut.clouddisk.service.impl.FileServiceImpl;
import com.dgut.clouddisk.service.impl.ShareServiceImpl;
import com.dgut.clouddisk.util.JsonData;
import com.github.pagehelper.PageInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.UUID;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
@RestController
@RequestMapping("/clouddisk/share")
public class ShareController {
    @Resource
    private ShareServiceImpl shareService;
    @Resource
    private FileServiceImpl fileService;
    @Autowired(required = false)
    RedisTemplate redisTemplate;

    @Value(value = "${serverHost}") //到application.properties改
    private String serverHost;



    //简陋的分享链接 （可用，已废弃）
    @RequestMapping("getUrl")
    public JsonData getFileShareUrlNoIn(@RequestBody File file, int shareTime) {
        // 获取分享链接
        String url = shareService.getFileShareLink(file.getFilePath(), shareTime);
        // 返回分享链接
        return JsonData.buildSuccess(url);
    }

    /**
     * 获取分享链接
     * @param fileId  文件Id
     * @param shareTime   有效时间
     * @return 返回虚拟的分享链接
     */
    @RequestMapping("getShareUrl")
    public JsonData getFileShareUrl(Long fileId, Integer shareTime) {
        File file = fileService.getById(fileId);
        // 获取分享链接（存入文件路径和要保存的时间，返回共享链接）
        String shareUrl = shareService.getFileShareLink(file.getFilePath(), shareTime);
        // 创建uuid
        String shareId = UUID.randomUUID().toString().replace("-", "".toLowerCase());
        // 插入分享数据到分享表
        shareService.insertShareLink(shareId,file.getFileId(),file.getUserId(),shareTime,shareUrl);
        String virtualUrl = "http://" + serverHost + "/clouddisk/share/jumpRealUrl?share_id=" +shareId;
        return JsonData.buildSuccess(virtualUrl);
    }

    /**
     * 跳转到真实的分享地址
     * @param share_id  分享Id
     * @param response
     * @return   获取下载内容
     */
    @RequestMapping("jumpRealUrl")
    public JsonData jumpRealUrl(String share_id, HttpServletResponse response) {
        //过期链接的页面
        String shareExpired;
        // 重定向
        try {
            Share share = shareService.getByshareId(share_id);
            if(share==null){
                shareExpired = "http://" + serverHost + "/shareDelete.html";
                response.sendRedirect(shareExpired);
            }
            // 获取链接有效时间
            long shareTime = share.getShareTime();
            //创建时间
            long createdTime = share.getShareCtime().getTime();
            //当前时间
            long nowTime = new Date().getTime();
            // 判断
            if ((nowTime - createdTime)/1000 < shareTime) {
                // 调用函数，获取真实地址
                response.sendRedirect(share.getShareUrl());
            } else {
                shareExpired = "http://" + serverHost + "/shareExpired.html";
                response.sendRedirect(shareExpired);
                //return JsonData.buildError("链接已过期！");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return JsonData.buildError("链接已经过期了");
    }

    /**
     * 获取用户自己的全部分享的链接列表
     * @param userId   用户Id
     * @param pageCount   当前页
     * @param pageSize    每页条数
     * @return  所有的分享链接（分页显示）
     */
   @RequestMapping("getShareRecord")
   public JsonData getShareRecord(Long userId,int pageCount,int pageSize){
       PageInfo<ShareVO> shares =shareService.getByUserId(userId,pageCount,pageSize);

       if(shares!=null){
            return JsonData.buildSuccess(shares);
        }else {
            return JsonData.buildError("暂没有分享链接！");
        }
   }

   //取消分享操作
   @RequestMapping("deleteById")
   public JsonData deleteById(String shareIds){
       String[] list = shareIds.split(",");
       for (String key : list) {
           shareService.deleteByShareId(key);
       }
       return JsonData.buildSuccess("删除成功！");
   }

   //修改分享链接操作
    @RequestMapping("updateShare")
    public JsonData updateShare(String shareId,Integer shareTime){
        ShareVO shareVO = shareService.updateShare(shareId,shareTime);
        return JsonData.buildSuccess(shareVO);
    }

}



