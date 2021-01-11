package com.dgut.clouddisk.service;

import com.dgut.clouddisk.entity.File;
import com.dgut.clouddisk.entity.vo.FileVO;
import com.dgut.clouddisk.util.JsonData;
import com.obs.services.model.DeleteObjectResult;
import com.obs.services.model.ObsBucket;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import com.obs.services.model.*;

import java.io.IOException;
import java.util.List;

public interface ObsService {


    //下载文件
    byte[] downloadFile(String objectName) throws IOException;


    //根据文件id下载单个或多个文件
    byte[] downloadFileById(String ids) throws IOException;


    //创建用户时新建文件夹
    PutObjectResult createDir(String objectName) throws IOException;

    //在当前路径创建文件夹
    PutObjectResult createNewDir(String path);

    //断点续传
    PutObjectResult upBreakPoint() throws IOException;

    //查看桶
    List<ObsBucket> getAllObsBucket() throws IOException;

    //查看所有对象
    List<ObsObject> getAllObsObject() throws IOException;

    //删除单个对象
    DeleteObjectResult deleteObject(String objectName);


    //删除单个文件
    DeleteObjectResult deleteFile(String objectName);

    //删除多个文件
    DeleteObjectResult deleteFiles(String ids);



    JsonData rename(String name, String newname) throws IOException;


    // 查看用户的所有文件
    List<ObsObject> getUserObsObject(Long userID) throws IOException;
    // 查看指定用户的删除对象
    List<ObsObject> getUserObsDeleteObject(Long userID) throws IOException;
    // 演示上传文件
    PutObjectResult testForUploadFile(String path,byte[] bytes) throws IOException;

    //创建回收站文件夹
    PutObjectResult createRecycleBin(String objectName) throws IOException;
    //删除到回收站
    Boolean deleteFileToRecycle(String sourcePath,String objectName,Long userId) throws IOException;
    //恢复回收站
    Boolean RecycleFile(String objectName,Long userId,String newPath) throws IOException;
    //路径下的obs
    List<ObsObject> getObjectByPath(String str) throws IOException;
    List<FileVO> getDeleteFileList(String path, boolean onlyDir,Long userId);
    //彻底删除
    Boolean thoughDelete(String objectName,Long userId) throws IOException;
    //删除指定路径对象
    DeleteObjectResult deleteByPath(String objectName) throws IOException;

    // 根据目的位置ID复制文件
    public Boolean copyFile(Long userId, Long fileId, Long destId) throws IOException;

    // 根据目的路径复制文件
    public Boolean copyFileByPath(Long userId, Long fileId,String destPath) throws IOException;

    // 获取文件列表
    public List<FileVO> getFileList(String path, boolean onlyDir,Long userId);

    // 获取群组文件列表
    public List<FileVO> getGroupFileList(String path,boolean onlyDir);


}
