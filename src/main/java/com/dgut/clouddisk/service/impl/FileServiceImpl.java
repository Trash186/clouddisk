package com.dgut.clouddisk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dgut.clouddisk.entity.Access;
import com.dgut.clouddisk.entity.File;
import com.dgut.clouddisk.exception.CloudException;
import com.dgut.clouddisk.mapper.FileMapper;
import com.dgut.clouddisk.mapper.ObsMapper;
import com.dgut.clouddisk.service.FileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.obs.services.ObsClient;
import org.springframework.beans.factory.annotation.Autowired;
import com.dgut.clouddisk.util.StatusCode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
    @Resource
    private FileMapper fileMapper;

    @Resource
    private ObsServiceImpl obsService;

    @Resource
    private AccessServiceImpl accessService;

    private final String keySuffixWithSlash = "测试1/";
    private final String RecycleuffixWithSlash = "recycleBin/";


    /**
     * 在当前路径下创建新的文件夹
     *
     * @param userId  用户ID
     * @param path    当前文件夹路径
     * @param dirName 文件夹的名字
     * @return
     */
    @Override
    public boolean createNewDir(Long userId, String path, String dirName) {
        QueryWrapper<File> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(File::getUserId, userId).eq(File::getFilePath, path + dirName+'/');
        File file = getOne(queryWrapper);
        if (file != null) {
            throw new CloudException(StatusCode.FILE_IS_EXISTED.code(), StatusCode.FILE_IS_EXISTED.message());
        }
        File f = new File();
        f.setUserId(userId);
        f.setFileSize(0);
        f.setFileName(dirName);
        f.setFilePath(path + dirName + "/");
        f.setFileUptime(new Date());
        f.setFileModitime(new Date());
        boolean save = save(f);
        if (!save) throw new CloudException(StatusCode.DATABASE_ERROR.code(), StatusCode.DATABASE_ERROR.message());
        obsService.createNewDir(path + dirName + "/");
        return true;
    }

    /**
     * 创建文件夹
     *
     * @param userId
     * @return
     */
    @Override
    public boolean createDir(Long userId) {
        File file = new File();
        file.setUserId(userId);
        file.setFileSize(0);
        file.setFilePath(keySuffixWithSlash + userId + "/");
        file.setFileName(userId.toString());
        file.setFileModitime(new Date());
        file.setFileUptime(new Date());
        int i = fileMapper.insert(file);
        if (1 != i) throw new CloudException(StatusCode.DATABASE_ERROR.code(), StatusCode.DATABASE_ERROR.message());
        File recFile = new File();
        recFile.setFilePath(keySuffixWithSlash + RecycleuffixWithSlash + userId + "/");
        recFile.setUserId(userId);
        recFile.setFileSize(0);
        recFile.setFileModitime(new Date());
        recFile.setFileUptime(new Date());
        recFile.setFileName(userId.toString());
        int insert = fileMapper.insert(recFile);
        if (1 != insert)
            throw new CloudException(StatusCode.DATABASE_ERROR.code(), StatusCode.DATABASE_ERROR.message());
        return true;
    }


    /**
     * 创建群组文件夹
     *
     * @param userId
     * @param fileName
     * @return
     */
    @Override
    public boolean createGroupDir(Long userId, String fileName) throws IOException {
        File file = new File();
        file.setUserId(userId);
        file.setFileName(fileName);
        file.setFileSize(0);
        file.setFilePath(keySuffixWithSlash + fileName + "/");
        file.setFileModitime(new Date());
        file.setFileUptime(new Date());
        int i = fileMapper.insert(file);
        if (1 != i) throw new CloudException(StatusCode.DATABASE_ERROR.code(), StatusCode.DATABASE_ERROR.message());
        obsService.createDir(fileName);
        return true;
    }

    /**
     * 删除群组文件
     * @param fileId
     * @return
     */
    public boolean deleteGroupFile(Long fileId){
        File file = getById(fileId);
        String filePath = file.getFilePath();
        //如果是文件夹
        if(filePath.endsWith("/")){
            QueryWrapper<File> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().likeRight(File::getFilePath,filePath).eq(File::getFileDelete, 0).orderByDesc(File::getFilePath);
            List<File> list = list(queryWrapper);
            for (File f:list){
                String path = f.getFilePath();
                boolean b = removeById(f.getFileId());
                if(b){
                    obsService.deleteObject(path);
                }
            }
        }
        //如果是文件
        else{
            boolean b = removeById(fileId);
            if(b){
                obsService.deleteObject(filePath);
            }
            return b;
        }
        return true;
    }

    /**
     * 删除个人文件
     *
     * @param userId
     * @return
     */
    @Override
    public boolean deletePerFile(Long userId) {
        QueryWrapper<File> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(File::getUserId, userId).orderByDesc(File::getFilePath);
        List<File> files = fileMapper.selectList(queryWrapper);
        for (File file : files) {
            String filePath = file.getFilePath();
            obsService.deleteObject(filePath);
            fileMapper.deleteById(file.getFileId());
        }
        return true;
    }

    /**
     * 判断文件是否重名
     * @param userId 用户ID
     * @param destPath 模板地址
     * @return
     */
    @Override
    public Boolean isSame(Long userId, String destPath) {
        QueryWrapper<File> wrapper = new QueryWrapper<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        map.put("file_path", destPath);
        wrapper.allEq(map);
        File file = fileMapper.selectOne(wrapper);
        return file != null;
    }


}
