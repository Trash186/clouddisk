package com.dgut.clouddisk.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dgut.clouddisk.config.ObsConfig;
import com.dgut.clouddisk.entity.Share;
import com.dgut.clouddisk.entity.vo.ShareVO;
import com.dgut.clouddisk.mapper.FileMapper;
import com.dgut.clouddisk.mapper.ShareMapper;
import com.dgut.clouddisk.service.ShareService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.obs.services.ObsClient;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
//import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share> implements ShareService {

    @Autowired
    private ObsConfig obsConfig;
    @Resource
    private ShareMapper shareMapper;
    @Resource
    private FileMapper fileMapper;
    @Value(value = "${serverHost}")
    private String serverHost;


    public ObsClient getObsClient() {
        return new ObsClient(obsConfig.getAccessKeyId(), obsConfig.getSecretAccessKey(), obsConfig.getEndpoint());
    }


    /**
     * 创建函数，用来实现传入路径，获得文件的共享链接
     *
     * @param filePath      传入的路径
     * @param expireSeconds 有效时间
     * @return 返回的共享链接
     */
    @Override
    public String getFileShareLink(String filePath, long expireSeconds) {
        // 创建obs客户端
        ObsClient obsClient = this.getObsClient();
        // 创建请求类
        TemporarySignatureRequest request = new TemporarySignatureRequest(HttpMethodEnum.GET, expireSeconds);
        // 设置桶名
        request.setBucketName(obsConfig.getBucketName());
        // 设置对象名
        request.setObjectKey(filePath);
        // 创建回复类
        TemporarySignatureResponse response = obsClient.createTemporarySignature(request);
        // 返回地址
        return response.getSignedUrl();
    }


    /**
     * 创建函数，用来插入分享链接
     *
     * @param fileId 传入的用户id
     * @param userID 分享链接的id
     * @param
     */
    @Override
    public void insertShareLink(String shareId, Long fileId, Long userID, Integer shareTime, String shareUrl) {
        // 创建时间
        Date date = new Date();
        Timestamp share_Ctime = new Timestamp(date.getTime());

        //System.out.println("share_Ctime:"+share_Ctime);
        // 调用函数
        shareMapper.insertShareLink(shareId, fileId, userID, shareTime, share_Ctime, shareUrl);

    }

    @Override
    public Share getByshareId(String shareId) {
        return shareMapper.getByshareId(shareId);
    }

    @Override
    public PageInfo<ShareVO> getByUserId(Long userId, Integer pageCount, Integer pageSize) {
        PageHelper.startPage(pageCount, pageSize);
        List<ShareVO> shareVOS = shareMapper.getByUserId(userId);
        for(ShareVO key:shareVOS){
            String virtualUrl = "http://" + serverHost + "/clouddisk/share/jumpRealUrl?share_id="+key.getShareId();
            key.setVirtualUrl(virtualUrl);

            long share_Ctime = key.getShareCtime().getTime();
            long now = new Date().getTime();
            long shareTime = 1L;
            if(key.getShareTime() > (now-share_Ctime)/1000){
                shareTime = key.getShareTime()-(now-share_Ctime)/1000;
            }else {
                shareTime=0L;
            }
            String deadTime="5分钟内过期";
            if(shareTime>86400){
                deadTime = Math.round(shareTime/86400.0)+"天后";
            }else if(shareTime>3600){
                deadTime = Math.round(shareTime/3600.0)+"小时后";
            }else if(shareTime>60){
                deadTime = Math.round(shareTime/60.0)+"分钟后";
            }else if(shareTime>0){
                deadTime =(int)(shareTime)+"秒后";
            }else if(shareTime==0L){
                deadTime = "已过期";
            }
            key.setDeadTime(deadTime);
        }
        return new PageInfo(shareVOS);
    }

    @Override
    public void deleteByShareId(String shareId) {
        shareMapper.deleteByShareId(shareId);
    }



    public ShareVO updateShare(String shareId,Integer deadTimes) {
        UpdateWrapper<Share> shareUpdateWrapper = new UpdateWrapper<>();
        //根据share_id更新信息
        shareUpdateWrapper.eq("share_id", shareId);
        ShareVO shareVO = shareMapper.getByShareId(shareId);
        long share_Ctime = shareVO.getShareCtime().getTime();
        long now = new Date().getTime();
        //这个用来存数据库
        int shareTime = (int)((now-share_Ctime)/1000)+deadTimes;
        shareVO.setShareTime(shareTime);

        String deadTime="5分钟内过期";
        if(deadTimes>86400){
            deadTime =Math.round(deadTimes/86400.0)+"天后";
        }else if(deadTimes>3600){
            deadTime =Math.round(deadTimes/3600.0)+"小时后";
        }else if(deadTimes>60){
            deadTime =Math.round(deadTimes/60.0)+"分钟后";
        }else if(deadTimes>0){
            deadTime =(int)(deadTimes)+"秒后";
        }else if(deadTimes==0L){
            deadTime = "已过期";
        }

        shareVO.setDeadTime(deadTime);
        String virtualUrl = "http://" + serverHost + "/clouddisk/share/jumpRealUrl?share_id="+shareId;
        shareVO.setVirtualUrl(virtualUrl);

        ObsClient obsClient = this.getObsClient();
        Share share = shareMapper.getByshareId(shareId);
        String filePath = fileMapper.getFilePathById(shareVO.getFileId());
        TemporarySignatureRequest request = new TemporarySignatureRequest(HttpMethodEnum.GET,
                obsConfig.getBucketName(), filePath, null, deadTimes, shareVO.getShareCtime()
        );
        // 创建回复类
        TemporarySignatureResponse response = obsClient.createTemporarySignature(request);

        share.setShareTime(shareTime);
        share.setShareUrl(response.getSignedUrl());
        shareMapper.update(share, shareUpdateWrapper);
        return shareVO;
    }
}