package com.dgut.clouddisk.service;

import com.dgut.clouddisk.entity.File;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
public interface FileService extends IService<File> {

    /**
     * 在当前路径下创建新的文件夹
     * @param userId  用户ID
     * @param path 当前文件夹路径
     * @param dirName 文件夹的名字
     * @return
     */
    boolean  createNewDir(Long userId,String path,String dirName);

    /**
     * 创建用户时创建文件夹
     * @param userId
     * @return
     */
    boolean createDir(Long userId);


    /**
     * 创建群组文件夹
     * @param userId
     * @param fileName
     * @return
     */
    boolean createGroupDir(Long userId,String fileName) throws IOException;


     boolean deleteGroupFile(Long fileId);

    /**
     * 删除个人文件
     * @param userId
     * @return
     */
    boolean deletePerFile(Long userId);

    /**
     * 判断是否重名
     * @param userId 用户ID
     * @param destPath 模板地址
     * @return 判断结果
     */
    Boolean isSame(Long userId,String destPath);




}
