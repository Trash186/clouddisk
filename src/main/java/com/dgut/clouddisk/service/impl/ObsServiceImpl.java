package com.dgut.clouddisk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dgut.clouddisk.config.ObsConfig;
import com.dgut.clouddisk.entity.File;
import com.dgut.clouddisk.entity.User;
import com.dgut.clouddisk.entity.vo.FileVO;
import com.dgut.clouddisk.mapper.FileMapper;
import com.dgut.clouddisk.mapper.UserMapper;
import com.dgut.clouddisk.mapper.impl.ObsMapperImpl;
import com.dgut.clouddisk.service.ObsService;
import com.dgut.clouddisk.util.JsonData;
import com.obs.services.model.DeleteObjectResult;
import com.obs.services.model.ObsBucket;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import com.obs.services.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class ObsServiceImpl implements ObsService {

    @Autowired
    private ObsConfig obsConfig;

    @Autowired
    private ObsMapperImpl obsMapper;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private UserMapper userMapper;


    @Override
    public byte[] downloadFile(String objectName) throws IOException {
        return obsMapper.downloadFile(objectName);
    }

    @Override
    public byte[] downloadFileById(String ids) throws IOException {
        return obsMapper.downloadFileById(ids);
    }

    @Override
    public PutObjectResult createDir(String objectName) throws IOException {
        return obsMapper.createDir(objectName);
    }

    @Override
    public PutObjectResult createNewDir(String path) {
        return obsMapper.createNewDir(path);
    }

    @Override
    public PutObjectResult upBreakPoint() throws IOException {
        return obsMapper.upBreakPoint();
    }

    @Override
    public List<ObsBucket> getAllObsBucket() throws IOException {
        return obsMapper.getAllObsBucket();
    }

    @Override
    public List<ObsObject> getAllObsObject() throws IOException {
        return obsMapper.getAllObsObject();
    }

    @Override
    public DeleteObjectResult deleteObject(String objectName) {
        return obsMapper.deleteObject(objectName);
    }

    @Override
    public DeleteObjectResult deleteFile(String objectName) {
        return obsMapper.deleteFile(objectName);
    }

    @Override
    public DeleteObjectResult deleteFiles(String ids) {
        return obsMapper.deleteFiles(ids);
    }


    @Override
    public JsonData rename(String name, String newname) throws IOException {
        return obsMapper.rename(name, newname);
    }

    @Override
    public List<ObsObject> getUserObsObject(Long userID) throws IOException {
        return obsMapper.getUserObsObject(userID);
    }

    @Override
    public PutObjectResult testForUploadFile(String path, byte[] bytes) throws IOException {
        return obsMapper.testForUploadFile(path, bytes);
    }

    //回收站文件夹
    @Override
    public PutObjectResult createRecycleBin(String objectName) throws IOException {
        return obsMapper.createRecycleBin(objectName);
    }

    // 查看指定用户的删除对象
    @Override
    public List<ObsObject> getUserObsDeleteObject(Long userID) throws IOException {
        return obsMapper.getUserObsDeleteObject(userID);
    }

    //删除到回收站
    @Override
    public Boolean deleteFileToRecycle(String sourcePath, String objectName, Long userId) throws IOException {
        return obsMapper.deleteFileToRecycle(sourcePath, objectName, userId);
    }

    //路径下的obs
    @Override
    public List<ObsObject> getObjectByPath(String str) throws IOException{
        return obsMapper.getObjectByPath(str);
    }

    //删除指定路径对象
    @Override
    public DeleteObjectResult deleteByPath(String objectName) throws IOException {
        return obsMapper.deleteByPath(objectName);
    }

    //回收站恢复
    @Override
    public Boolean RecycleFile(String objectName, Long userId, String newPath) throws IOException {
        return obsMapper.RecycleFile(objectName, userId, newPath);
    }
    public List<FileVO> getDeleteFileList(String path, boolean onlyDir,Long userId){
        return obsMapper.getDeleteFileList(path,onlyDir,userId);
    }
    //回收站彻底彻底
    @Override
    public Boolean thoughDelete(String objectName, Long userId) throws IOException {
        return obsMapper.thoughDelete(objectName, userId);
    }

    /**
     * 根据ID文件复制
     * @param userId 用户ID
     * @param fileId 源文件ID
     * @param destId 目的路径ID
     * @return 结果
     * @throws IOException
     */
    @Override
    @Transactional
    public Boolean copyFile(Long userId, Long fileId, Long destId) throws IOException {
        File source = getFile(userId,fileId);
        File dest = getFile(userId,destId);
        File file = new File();
        String destPath=null;
        // 如果是文件夹
        if (source.getFilePath().endsWith("/")){
            destPath=dest.getFilePath()+source.getFileName()+"/";
        }else {
            destPath=dest.getFilePath()+source.getFileName();
        }
        return doCopy(userId, source, file, destPath);
    }

    @Override
    @Transactional
    public Boolean copyFileByPath(Long userId, Long fileId, String destPath) throws IOException {
        File source = getFile(userId,fileId);
        File file = new File();
        return doCopy(userId, source, file, destPath);
    }

    private Boolean doCopy(Long userId, File source, File file, String destPath) throws IOException {
        file.setFileName(source.getFileName());
        file.setUserId(userId);
        file.setFilePath(destPath);
        file.setFileSize(source.getFileSize());
        file.setFileUptime(new Date());
        file.setFileModitime(new Date());
        fileMapper.insert(file);
        Boolean copyFile = obsMapper.copyFile(source.getFilePath(), destPath);
        User user = userMapper.selectById(userId);
        if (copyFile&&user!=null){
            Long usedSize=user.getUserUsedsize()+source.getFileSize();
            Long remainSize=user.getUserRemainingsize()-usedSize;
            User update = new User();
            update.setUserId(userId);
            update.setUserUsedsize(usedSize);
            update.setUserRemainingsize(remainSize);
            userMapper.updateById(update);
            return true;
        }else {
            return false;
        }
    }


    private File getFile(Long userId,Long fileId){
        HashMap<String, Object> map = new HashMap<>();
        QueryWrapper<File> wrapper = new QueryWrapper<>();
        map.put("user_id",userId);
        map.put("file_id",fileId);
        wrapper.allEq(map);
        return fileMapper.selectOne(wrapper);
    }

    /**
     * 获取文件列表
     *
     * @param path    请求路径
     * @param onlyDir 是否只返回文件夹
     * @return 封装JSON的文件列表
     */
    @Override
    public List<FileVO> getFileList(String path, boolean onlyDir, Long userId) {
        return obsMapper.getFileList(path, onlyDir, userId);

    }

    @Override
    public List<FileVO> getGroupFileList(String path, boolean onlyDir) {
        return obsMapper.getGroupFileList(path,onlyDir);
    }
}
