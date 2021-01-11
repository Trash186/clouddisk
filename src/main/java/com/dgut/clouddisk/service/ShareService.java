package com.dgut.clouddisk.service;

import com.dgut.clouddisk.entity.Share;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dgut.clouddisk.entity.User;
import com.dgut.clouddisk.entity.vo.ShareVO;
import com.github.pagehelper.PageInfo;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
public interface ShareService extends IService<Share> {

    /**
     * 实现传入路径，获得文件的共享链接
     * @param filePath          传入的路径
     * @param expireSeconds     有效时间
     * @return                  返回的共享链接
     */
    String getFileShareLink(String filePath, long expireSeconds);

    /**
     * 创建函数，用来插入分享链接
     * @param shareId    分享id
     * @param fileId     文件id
     * @param userID     用户id
     * @param shareTime  有效时间
     * @param shareUrl   分享的真实链接
     */
   void insertShareLink(String shareId,Long fileId, Long userID, Integer shareTime,String shareUrl);

   //根据shareId获得share对象
   Share getByshareId(String shareId);

   //通过userId得到该用户所有的分享链接信息（有分页功能）
   PageInfo<ShareVO> getByUserId(Long userId, Integer pageCount, Integer pageSize);

   //通过shareId邮件该条分享链接
   void deleteByShareId(String shareId);

   //修改分享链接
   ShareVO updateShare(String shareId,Integer deadTimes);
}
