package com.dgut.clouddisk.mapper;

import com.dgut.clouddisk.entity.File;
import com.dgut.clouddisk.entity.vo.FileVO;
import com.dgut.clouddisk.util.JsonData;
import com.obs.services.model.*;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.RequestParam;
import com.dgut.clouddisk.util.JsonData;

import java.io.IOException;
import java.util.List;

public interface ObsMapper {


    //下载文件
     byte[] downloadFile(String objectName) throws IOException;

     //根据文件id下载文件
     byte[] downloadFileById(String ids) throws IOException;

    //删除单个文件
    DeleteObjectResult deleteFile(String objectName);

    //删除多个文件
    DeleteObjectResult deleteFiles(String ids);


    //创建用户时创建文件夹
    PutObjectResult createDir(String objectName) throws IOException;

    //在当前路径下创建文件夹
    PutObjectResult createNewDir(String path);

    //断点续传
    PutObjectResult upBreakPoint() throws IOException;

    //查看桶
    List<ObsBucket> getAllObsBucket() throws IOException;

    //查看所有对象
    List<ObsObject> getAllObsObject() throws IOException;

    //重命名对象
    JsonData rename(String name, String newname) throws IOException;

    //删除单个对象
    DeleteObjectResult deleteObject(String objectName);

    // 查看指定用户的所有对象  已作废 请使用新的！！！！！！
    List<ObsObject> getUserObsObject(Long userID) throws IOException;
    // 查看指定用户的所有对象 已作废
    List<ObsObject> getUserObsDeleteObject(Long userID) throws IOException;
    // 演示上传文件
    PutObjectResult testForUploadFile(String path,byte[] bytes) throws IOException;


    //创建回收站
     PutObjectResult createRecycleBin(String userId) throws IOException;
    //删除到回收站
    Boolean deleteFileToRecycle(String sourcePath,String objectName,Long userId) throws IOException;
    //回收站内容恢复
    Boolean RecycleFile(String objectName,Long userId,String newPath) throws IOException;
    public List<FileVO> getDeleteFileList(String path, boolean onlyDir,Long userId) throws IOException;
    //路径下的obs
    List<ObsObject> getObjectByPath(String str) throws IOException;
    //回收站彻底删除
    Boolean thoughDelete(String objectName,Long userId) throws IOException;
    //删除指定路径对象
    DeleteObjectResult deleteByPath(String objectName) throws IOException;


    // 复制文件
    Boolean copyFile(String sourceObjName, String destObjName) throws IOException;

    // 获取文件列表
    public List<FileVO> getFileList(String path, boolean onlyDir,Long userId);

    // 获取群组文件列表
    public List<FileVO> getGroupFileList(String path,boolean onlyDir);
}
