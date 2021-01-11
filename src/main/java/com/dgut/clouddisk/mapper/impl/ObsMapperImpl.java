package com.dgut.clouddisk.mapper.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dgut.clouddisk.config.ObsConfig;
import com.dgut.clouddisk.entity.vo.FileVO;
import com.dgut.clouddisk.mapper.FileMapper;
import com.dgut.clouddisk.mapper.ObsMapper;
import com.dgut.clouddisk.service.FileService;
import com.dgut.clouddisk.service.impl.FileServiceImpl;
import com.dgut.clouddisk.util.JsonData;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class ObsMapperImpl implements ObsMapper {
    @Autowired
    ObsConfig obsConfig;
    @Resource
    private FileServiceImpl FileService;

    @Resource
    private FileMapper fileMapper;

    private final String keySuffixWithSlash = "测试1/";
    private final String RecycleuffixWithSlash = "recycleBin/";

    /**
     * 创建ObsClient实例
     *
     * @return ObsClient实例
     */
    public ObsClient getObsClient() {
        return new ObsClient(obsConfig.getAccessKeyId(), obsConfig.getSecretAccessKey(), obsConfig.getEndpoint());
    }

    /**
     * 下载文件
     *
     * @param objectName
     * @return ObsObject
     */
    @Override
    public byte[] downloadFile(String objectName) throws IOException {
        //获取文件名
        String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
        // 创建ObsClient实例
        ObsClient obsClient = getObsClient();
        ObsObject obsObject = obsClient.getObject(obsConfig.getBucketName(), objectName);
        // 读取对象内容
        System.out.println("Object content:");
        InputStream input = obsObject.getObjectContent();
        byte[] b = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len;
        while ((len = input.read(b)) != -1) {
            bos.write(b, 0, len);
        }
        FileOutputStream fileOutputStream = new FileOutputStream(new File("D:/temp/" + fileName));
        bos.writeTo(fileOutputStream);
        fileOutputStream.flush();
        System.out.println(new String(bos.toByteArray()));
        bos.close();
        input.close();
        return bos.toByteArray();
    }

    @Override
    public byte[] downloadFileById(String fileIds) throws IOException {
        String[] list = fileIds.split(",");
        for (String id : list) {
            QueryWrapper<com.dgut.clouddisk.entity.File> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("file_id", id);
            com.dgut.clouddisk.entity.File file = FileService.getOne(queryWrapper);
            //获取文件名
            String fileName=file.getFileName();
            String filepath = file.getFilePath();
            String objectName=filepath;
            // 创建ObsClient实例
            ObsClient obsClient = getObsClient();
            ObsObject obsObject = obsClient.getObject(obsConfig.getBucketName(), objectName);
            // 读取对象内容
            System.out.println("Object content:");
            InputStream input = obsObject.getObjectContent();
            byte[] b = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            while ((len = input.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            FileOutputStream fileOutputStream = new FileOutputStream(new File("D:/temp/" + fileName));
            bos.writeTo(fileOutputStream);
            fileOutputStream.flush();
            System.out.println(new String(bos.toByteArray()));
            bos.close();
            input.close();
        }
        //return bos.toByteArray();
        return null;
    }


    /**
     * 删除文件
     *
     * @param objectName
     * @return
     */
    @Override
    public DeleteObjectResult deleteFile(String objectName) {
        ObsClient obsClient = this.getObsClient();
        return obsClient.deleteObject(obsConfig.getBucketName(), keySuffixWithSlash + objectName + "/");

    }

    @Override
    public DeleteObjectResult deleteFiles(String ids) {
        ObsClient obsClient=this.getObsClient();
        return obsClient.deleteObject(obsConfig.getBucketName(),ids);

    }

    /**
     * 断点续传
     *
     * @return 当所有分段都上传成功时返回上传成功的结果
     * @throws IOException 否则抛出异常提醒用户再次调用接口进行重新上传
     */
    @Override
    public PutObjectResult upBreakPoint() {
        //创建obsClient实例
        ObsClient obsClient = getObsClient();

        UploadFileRequest request = new UploadFileRequest("bucketname", "obsjectKey");
        // 设置待上传的本地文件，localfile为待上传的本地文件路径，需要指定到具体的文件名
        request.setUploadFile("localfile");
        // 设置分段上传时的最大并发数
        request.setTaskNum(5);
        // 设置分段大小为10MB
        request.setPartSize(10 * 1024 * 1024);
        // 开启断点续传模式
        request.setEnableCheckpoint(true);
        try {
            // 进行断点续传上传
            CompleteMultipartUploadResult result = obsClient.uploadFile(request);
        } catch (ObsException e) {
            // 发生异常时可再次调用断点续传上传接口进行重新上传
        }
        return null;
    }

    //查看桶
    @Override
    public List<ObsBucket> getAllObsBucket() throws IOException {
        ListBucketsRequest request = new ListBucketsRequest();
        request.setQueryLocation(true);
        ObsClient obsClient = this.getObsClient();
        List<ObsBucket> buckets = obsClient.listBuckets(request);
        obsClient.close();
        return buckets;
    }

    //查看所有对象
    @Override
    public List<ObsObject> getAllObsObject() throws IOException {
        ObsClient obsClient = this.getObsClient();
        ObjectListing objectListing = obsClient.listObjects(obsConfig.getBucketName());
        List<ObsObject> objects = objectListing.getObjects();
        obsClient.close();
        return objects;
    }

    //删除单个对象
    @Override
    public DeleteObjectResult deleteObject(String objectName) {
        ObsClient obsClient = this.getObsClient();
        return obsClient.deleteObject(obsConfig.getBucketName(), objectName);

    }


    /**
     * 创建用户时创建文件夹
     *
     * @return 返回新建文件夹对象
     * @throws IOException 返回错误
     */
    @Override
    public PutObjectResult createDir(String objectName) {
        //创建obsClient实例
        ObsClient obsClient = getObsClient();
        return obsClient.putObject(obsConfig.getBucketName(), keySuffixWithSlash + objectName + "/", new ByteArrayInputStream(new byte[0]));
        // obsClient.putObject(obsConfig.getBucketName(), keySuffixWithSlash + objectName, new ByteArrayInputStream("Hello OBS".getBytes()));
    }

    /**
     * 在当前路径下创建文件夹
     * @param path
     * @return
     */
    @Override
    public PutObjectResult createNewDir(String path) {
        ObsClient obsClient = getObsClient();
        return obsClient.putObject(obsConfig.getBucketName(),path,new ByteArrayInputStream(new byte[0]));
    }

    //重命名
    @Override
    public JsonData rename(String name, String newname) throws IOException {
        ObsClient obsClient = getObsClient();
        RenameObjectRequest request = new RenameObjectRequest();

        request.setBucketName("obs-laixingzhen-file01");
        request.setObjectKey(name);
        request.setNewObjectKey(newname);
        System.out.println(request.getObjectKey());
        System.out.println(request.getNewObjectKey());
        RenameObjectResult result = obsClient.renameObject(request);
        obsClient.close();
        return null;
    }

    //查看指定位置所有对象 已作废 请使用新的！！！！！！
    @Override
    public List<ObsObject> getUserObsObject(Long userID) throws IOException {
        String marker = keySuffixWithSlash + userID.toString() + "/";
        ObsClient obsClient = this.getObsClient();
        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(obsConfig.getBucketName());
//        request.setMarker(marker);
        request.setPrefix(marker);
        ObjectListing objectListing = obsClient.listObjects(request);
        List<ObsObject> objects = objectListing.getObjects();
        obsClient.close();
        return objects;
    }

    //查看指定位置所有对象,作废
    @Override
    public List<ObsObject> getUserObsDeleteObject(Long userID) throws IOException {
        String marker = keySuffixWithSlash + RecycleuffixWithSlash + userID.toString() + "/";
        ObsClient obsClient = this.getObsClient();
        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(obsConfig.getBucketName());
//        request.setMarker(marker);
        request.setPrefix(marker);
        ObjectListing objectListing = obsClient.listObjects(request);
        List<ObsObject> objects = objectListing.getObjects();
        obsClient.close();
        return objects;
    }

    @Override
    public PutObjectResult testForUploadFile(String path, byte[] bytes) throws IOException {
        // 创建ObsClient实例
        ObsClient obsClient = getObsClient();
        // 上传文件
        return obsClient.putObject(obsConfig.getBucketName(),  path, new ByteArrayInputStream(bytes));
    }


    //创建回收站
    @Override
    public PutObjectResult createRecycleBin(String userId) throws IOException {
        return this.createDir(RecycleuffixWithSlash + "" + userId);
    }

    //删除到回收站
    @Override
    public Boolean deleteFileToRecycle(String sourcePath,String objectName, Long userId) throws IOException {
        ObsClient obsClient = getObsClient();
        String str = keySuffixWithSlash + RecycleuffixWithSlash + userId.toString() + "/";
        try {
            CopyObjectResult result = obsClient.copyObject(obsConfig.getBucketName(), sourcePath,
                    obsConfig.getBucketName(), str + objectName);
            deleteByPath(sourcePath);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    //查看指定位置所有对象,作废
    @Override
    public List<ObsObject> getObjectByPath(String str) throws IOException {
        ObsClient obsClient = this.getObsClient();
        ListObjectsRequest request = new ListObjectsRequest();
        request.setBucketName(obsConfig.getBucketName());
        request.setPrefix(str);
        ObjectListing objectListing = obsClient.listObjects(request);
        List<ObsObject> objects = objectListing.getObjects();
        obsClient.close();
        return objects;
    }
    //删除指定路径下obs文件
    @Override
    public DeleteObjectResult deleteByPath(String objectName) throws IOException{
        ObsClient obsClient = this.getObsClient();
        return obsClient.deleteObject(obsConfig.getBucketName(), objectName);
    }
    //回收站:恢复文件
    @Override
    public Boolean RecycleFile(String objectName, Long userId, String newPath) throws IOException {
        ObsClient obsClient = getObsClient();
        try {
            System.out.println(objectName + "====" + newPath);
            CopyObjectResult result = obsClient.copyObject(obsConfig.getBucketName(), objectName,
                    obsConfig.getBucketName(), newPath);
            //objectName.lastIndexOf(userId.toString())+userId.toString().length()+1
            obsClient.deleteObject(obsConfig.getBucketName(), objectName);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //彻底删除:
    @Override
    public Boolean thoughDelete(String objectName,Long userId) throws IOException {
        ObsClient obsClient = getObsClient();

        String str = keySuffixWithSlash + RecycleuffixWithSlash + userId.toString() + "/";
        System.out.println("删除文件为："+str+objectName);
        try {
            obsClient.deleteObject(obsConfig.getBucketName(), str + objectName);
        } catch (Exception e) {
            return false;
        }
        return true;
    }




    @Override
    public Boolean copyFile(String sourceObjName, String destObjName) throws IOException {
        // 创建ObsClient实例
        ObsClient obsClient = getObsClient();
        try {
            obsClient.copyObject(obsConfig.getBucketName(),
                    sourceObjName, obsConfig.getBucketName(), destObjName);
        } catch (ObsException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public List<FileVO> getDeleteFileList(String path, boolean onlyDir,Long userId){
        // 创建ObsClient实例
        ObsClient obsClient = getObsClient();
        ListObjectsRequest request = new ListObjectsRequest();
        // 设置桶名
        request.setBucketName(obsConfig.getBucketName());
        // 设置用于对对象名进行分组的字符
        request.setDelimiter("/");
        // 设置列举对象时的对象名前缀
        request.setPrefix(path);
        ObjectListing objectListing = obsClient.listObjects(request);
        // 获取文件夹
        List<String> prefixes = objectListing.getCommonPrefixes();

        List<FileVO> files = new ArrayList<>();

        for (String prefix : prefixes) {
            System.out.println(prefix);
            String prefixName = prefix.substring(path.lastIndexOf("/") + 1);
            if (!onlyDir) {
                continue;
            }
            com.dgut.clouddisk.entity.File file = setDeleteFileId(userId, prefix);
            files.add(new FileVO(file.getFileId().toString(),prefixName, null, null));
        }
        if (!onlyDir) {
            // 获取文件
            List<ObsObject> objects = objectListing.getObjects();
            for (ObsObject object : objects) {
                String prefix=object.getObjectKey();
                String fileName = prefix.substring(path.lastIndexOf("/") + 1);
                Long fileSize = object.getMetadata().getContentLength();
                Date fileModifyTime = object.getMetadata().getLastModified();
                if (fileSize==0){
                    continue;
                }
                com.dgut.clouddisk.entity.File file = setDeleteFileId(userId, prefix);
                files.add(new FileVO(file.getFileId().toString(),fileName, Math.toIntExact(fileSize), fileModifyTime));
            }
        }
        return files;
    }
    private com.dgut.clouddisk.entity.File setDeleteFileId(Long userId, String prefix) {
        QueryWrapper<com.dgut.clouddisk.entity.File> wrapper = new QueryWrapper<>();
        HashMap<String , Object> map = new HashMap<>();
        map.put("user_id",userId);
        map.put("file_rpath",prefix);

        wrapper.allEq(map);
        return fileMapper.selectOne(wrapper);
    }
    /**
     * 获取文件列表
     * @param path 请求路径
     * @param onlyDir 是否只返回文件夹
     * @return 封装JSON的文件列表
     */
    @Override
    public List<FileVO> getFileList(String path, boolean onlyDir,Long userId) {
        path = keySuffixWithSlash + path;
//        System.out.println(path);
        // 创建ObsClient实例
        ObsClient obsClient = getObsClient();
        ListObjectsRequest request = new ListObjectsRequest();
        // 设置桶名
        request.setBucketName(obsConfig.getBucketName());
        // 设置用于对对象名进行分组的字符
        request.setDelimiter("/");
        // 设置列举对象时的对象名前缀
        request.setPrefix(path);
        ObjectListing objectListing = obsClient.listObjects(request);
        // 获取文件夹
        List<String> prefixes = objectListing.getCommonPrefixes();

        List<FileVO> files = new ArrayList<>();

        for (String prefix : prefixes) {
            String prefixName = prefix.substring(path.lastIndexOf("/") + 1);
            if (!onlyDir) {
                continue;
            }
            com.dgut.clouddisk.entity.File file = setFileId(userId, prefix);
            files.add(new FileVO(file.getFileId().toString(),prefixName, null, null));
        }
        if (!onlyDir) {
            // 获取文件
            List<ObsObject> objects = objectListing.getObjects();
            for (ObsObject object : objects) {
                String prefix=object.getObjectKey();
                String fileName = prefix.substring(path.lastIndexOf("/") + 1);
                Long fileSize = object.getMetadata().getContentLength();
                Date fileModifyTime = object.getMetadata().getLastModified();
                if (fileSize==0){
                    continue;
                }
                com.dgut.clouddisk.entity.File file = setFileId(userId, prefix);
                files.add(new FileVO(file.getFileId().toString(),fileName, Math.toIntExact(fileSize), fileModifyTime));
            }
        }
        return files;
    }

    @Override
    public List<FileVO> getGroupFileList(String path, boolean onlyDir) {
        path = keySuffixWithSlash + path;
        System.out.println(path);
        // 创建ObsClient实例
        ObsClient obsClient = getObsClient();
        ListObjectsRequest request = new ListObjectsRequest();
        // 设置桶名
        request.setBucketName(obsConfig.getBucketName());
        // 设置用于对对象名进行分组的字符
        request.setDelimiter("/");
        // 设置列举对象时的对象名前缀
        request.setPrefix(path);
        ObjectListing objectListing = obsClient.listObjects(request);
        // 获取文件夹
        List<String> prefixes = objectListing.getCommonPrefixes();

        List<FileVO> files = new ArrayList<>();
        for (String prefix : prefixes) {
            String prefixName = prefix.substring(path.lastIndexOf("/") + 1);
            if (!onlyDir) {
                continue;
            }
            com.dgut.clouddisk.entity.File file = getGroupFile(prefix);
            files.add(new FileVO(file.getFileId().toString(),prefixName, null, null));
        }
        if (!onlyDir) {
            // 获取文件
            List<ObsObject> objects = objectListing.getObjects();
            for (ObsObject object : objects) {
                String prefix=object.getObjectKey();
                String fileName = prefix.substring(path.lastIndexOf("/") + 1);
                Long fileSize = object.getMetadata().getContentLength();
                Date fileModifyTime = object.getMetadata().getLastModified();
                if (fileSize==0){
                    continue;
                }
                com.dgut.clouddisk.entity.File file = getGroupFile(prefix);
                files.add(new FileVO(file.getFileId().toString(),fileName, Math.toIntExact(fileSize), fileModifyTime));
            }
        }
        return files;
    }

    private com.dgut.clouddisk.entity.File getGroupFile(String prefix) {
        QueryWrapper<com.dgut.clouddisk.entity.File> wrapper = new QueryWrapper<>();
        wrapper.eq("file_path",prefix);
        return fileMapper.selectOne(wrapper);
    }

    private com.dgut.clouddisk.entity.File setFileId(Long userId, String prefix) {
        QueryWrapper<com.dgut.clouddisk.entity.File> wrapper = new QueryWrapper<>();
        HashMap<String , Object> map = new HashMap<>();
        map.put("user_id",userId);
        map.put("file_path",prefix);
        wrapper.allEq(map);
        return fileMapper.selectOne(wrapper);
    }
}

