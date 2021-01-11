package com.dgut.clouddisk.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dgut.clouddisk.entity.Access;
import com.dgut.clouddisk.entity.File;
import com.dgut.clouddisk.entity.Share;
import com.dgut.clouddisk.entity.User;
import com.dgut.clouddisk.entity.vo.FileVO;
import com.dgut.clouddisk.mapper.FileMapper;
import com.dgut.clouddisk.service.UserService;
import com.dgut.clouddisk.service.impl.*;
import com.dgut.clouddisk.util.JsonData;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/clouddisk/RecycleBin")
public class RecycleBinController {
    @Autowired
    private FileServiceImpl fileService;
    @Autowired
    private AccessServiceImpl accessService;
    @Resource
    private ObsServiceImpl obsService;
    @Resource
    private UserServiceImpl userService;
    @Resource
    private ShareServiceImpl shareService;

    private final String keySuffixWithSlash = "测试1/";
    private final String RecycleuffixWithSlash = "recycleBin/";


//    @RequestMapping("/listUserDeleteFile")
//    public JsonData listDeleteFiles(@RequestBody JSONObject jsonObject) throws IOException {
//        Long userId = jsonObject.getLong("userId");
//        if (userId == null) {
//            return JsonData.buildError("用户ID为空");
//        }
//        String path = jsonObject.get("path").toString();
//        //System.out.println(userId+"=="+path);
//        String str =  keySuffixWithSlash + RecycleuffixWithSlash + userId.toString() + path;
//        List<FileVO> dirList = obsService.getDeleteFileList(str, true, userId);
//        List<FileVO> fileList = obsService.getDeleteFileList(str, false, userId);
//        List<FileVO> list = new ArrayList<>();
//        list.addAll(dirList);
//        list.addAll(fileList);
//        if (list != null && list.size() > 0) {
//            return JsonData.buildSuccess(list);
//        } else {
//            return JsonData.buildError("当前用户无删除文件");
//        }
//    }

    @RequestMapping("/listUserDeleteFile")
    public JsonData listDeleteFiles(@RequestParam(value = "userId") Long userId,@RequestParam(value = "path") String path) throws IOException {
        String str =  keySuffixWithSlash + RecycleuffixWithSlash + userId.toString() + path;
        List<FileVO> dirList = obsService.getDeleteFileList(str, true, userId);
        List<FileVO> fileList = obsService.getDeleteFileList(str, false, userId);
        List<FileVO> list = new ArrayList<>();
        list.addAll(dirList);
        list.addAll(fileList);
        if (list != null && list.size() > 0) {
            return JsonData.buildSuccess(list);
        } else {
            return JsonData.buildError("当前用户无删除文件");
        }

    }
    //恢复文件
    @RequestMapping("/recycleFile")
    public JsonData recycleFile(@RequestParam(value = "userId") Long userId,@RequestParam(value = "fileIds") String fileIds) throws IOException {
        String str = keySuffixWithSlash + RecycleuffixWithSlash + userId.toString() + "/";
        String[] list = fileIds.split(",");
        for (String fileId : list) {
            QueryWrapper<File> Wrapper = new QueryWrapper<>();
            Wrapper.eq("file_id", fileId).eq("user_id", userId).eq("file_delete", 1);
            File f = fileService.getOne(Wrapper);
            if(f == null){
                return JsonData.buildSuccess("当前内容可能已经不存在");
            }
            if (f.getFileRpath().endsWith("/")) {
                //文件夹中文件夹
                Boolean is = folderORfile(f.getFileRpath(),userId);
                if(is) {
                    obsService.deleteByPath(f.getFileRpath());
                } else{
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return JsonData.buildError("文件恢复失败");
                }
                UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("file_id", f.getFileId()).set("file_delete", 0).set("file_delete", 0).set("file_rpath",null).set("file_modiTime",new Date());
                fileService.update(updateWrapper);

            }
            else {
              try {
                  Boolean is = obsService.RecycleFile(f.getFileRpath(), userId, f.getFilePath());
                  if(!is){
                      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                      return JsonData.buildError("文件恢复失败");
                  }
                  UpdateWrapper<File> updateWrapper2 = new UpdateWrapper<>();
                  updateWrapper2.eq("file_id", fileId).eq("user_id", userId).eq("file_delete", 1).set("file_delete", 0).set("file_rpath",null).set("file_modiTime",new Date());
                  fileService.update(updateWrapper2);
                  User user = userService.selectById(userId);
                  //更新User
                  UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                  userUpdateWrapper.eq("user_id", user.getUserId()).set("user_usedSize", user.getUserUsedsize()+f.getFileSize()).set("user_remainingSize",  user.getUserRemainingsize()-f.getFileSize());
                  userService.update(userUpdateWrapper);
              } catch (Exception e) {
                  TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                  return JsonData.buildError("文件恢复失败");
              }
          }
        }
        return JsonData.buildSuccess("文件已恢复");
    }

//    @RequestMapping("/one")
//    public JsonData folder(Long userId,String path){
//        List<FileVO> list = new ArrayList<>();
//        List<FileVO> olist = listOBS(userId,path);
//        list.addAll(olist);
//        return JsonData.buildSuccess(list);
//    }
//    public List<FileVO> listOBS(Long userId,String path){
//        List<FileVO> list = new ArrayList<>();
//        String str =  keySuffixWithSlash + RecycleuffixWithSlash + userId.toString() + path;
//        System.out.println("===================="+str);
//        List<FileVO> fileList = obsService.getDeleteFileList(str, false, userId);
//        list.addAll(fileList);
//        List<FileVO> dirList = obsService.getDeleteFileList(str, true, userId);
//        list.addAll(dirList);
//        if (dirList != null){
//            for (FileVO fileVo : dirList){
//                List<FileVO> olist = listOBS(userId,path+fileVo.getFileName());
//                list.addAll(olist);
//            }
//        }
//        return list;
//    }

    // filepath : 要恢复的文件夹 , userId : 用户名
    public Boolean folderORfile(String filepath,Long userId) throws IOException {
        //文件夹
        try {
            if (filepath.endsWith("/")) {
                QueryWrapper<File> queryWrapper = new QueryWrapper<>();
                queryWrapper.likeRight("file_rpath", filepath).eq("file_delete",1);
                List<File> fList = fileService.list(queryWrapper);
                //一个文件夹
                if (fList.size() == 1) {
                    //要恢复的文件夹中有没有（可能已经恢复）删除的文件
                    queryWrapper.likeRight("file_rpath", filepath).eq("file_delete",0);
                    List<File> files = fileService.list(queryWrapper);
                    //文件夹但有文件已恢复 , 一个文件夹
                    if( files.size() != 0){
                        for (File f : fList) {
                            UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
                            updateWrapper.eq("file_id", f.getFileId()).set("file_delete", 0).set("file_rpath",null);
                            //文件夹内
                            obsService.deleteByPath(f.getFileRpath());
                            fileService.update(updateWrapper);
                        }
                    }
                    //空壳文件夹
                    else {
                        for (File f : fList) {
                            //删除文件夹
                            UpdateWrapper<File> updateWrapper2 = new UpdateWrapper<>();
                            updateWrapper2.eq("file_id", f.getFileId()).set("file_delete", 0).set("file_rpath",null);
                            //obs恢复
                            Boolean is = obsService.RecycleFile(f.getFileRpath(), userId, f.getFilePath());
                            if(!is){
                                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                                return false;
                            }
                            fileService.update(updateWrapper2);
                        }
                    }
                }
                //文件夹还有子文件
                else {
                    for (File f : fList) {
                        if (f.getFileRpath().endsWith("/")) {
                            //还有子文件夹
                            if (!f.getFileRpath().equals(filepath)) {
                                folderORfile(f.getFileRpath(), userId);
                                UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
                                updateWrapper.eq("file_id", f.getFileId()).set("file_delete", 0).set("file_rpath",null);
                                obsService.deleteByPath(f.getFileRpath());
                                fileService.update(updateWrapper);
                            }
                        }
                        //文件,创建文件,---  除去filepath的第二个“/”之前
                        else {
                            //String sourcePath 原路径,String objectName 目的路径 ，
                            UpdateWrapper<File> updateWrapper3 = new UpdateWrapper<>();
                            updateWrapper3.eq("file_id", f.getFileId()).set("file_delete", 0).set("file_rpath",null);
                            //obs删除
                            Boolean is = obsService.RecycleFile(f.getFileRpath(), userId, f.getFilePath());
                            fileService.update(updateWrapper3);
                            User user = userService.selectById(userId);
                            //更新User
                            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                            userUpdateWrapper.eq("user_id", user.getUserId()).set("user_usedSize", user.getUserUsedsize()+f.getFileSize()).set("user_remainingSize",  user.getUserRemainingsize()-f.getFileSize());
                            userService.update(userUpdateWrapper);
                            if(!is){
                                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                                return false;
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @RequestMapping("/thoughDelete")
    public JsonData thoughDelete(@RequestParam(value = "userId") Long userId,@RequestParam(value = "fileIds") String fileIds){
            String[] list = fileIds.split(",");
            for (String fileId : list){
                QueryWrapper<File> Wrapper = new QueryWrapper<>();
                Wrapper.eq("file_id", fileId).eq("user_id", userId).eq("file_delete", 1);
                File f = fileService.getOne(Wrapper);
                //文件夹
                if(f.getFileRpath().endsWith("/")){
                    //文件夹
                    Boolean is = null;
                    try {
                        is = deleteFolder(f.getFileRpath(),userId);
                        obsService.deleteByPath(f.getFileRpath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return JsonData.buildError("文件删除失败");
                    }
                    //obsDeleteMethod(f.getUserId(),f.getFileRpath());
                    if(!is){
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return JsonData.buildError("文件删除失败");

                    }
                }
                //回收站单个文件
                else{
                    System.out.println(f.getFileId()+"==="+f.getFileRpath());

                    try {
                        obsDeleteMethod(f.getUserId(),f.getFileRpath(),f.getFileId());
                        obsService.deleteByPath(f.getFileRpath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return JsonData.buildError("文件删除失败");
                    }
                }
            }
            return JsonData.buildSuccess("删除成功");
    }
    public void obsDeleteMethod(Long userId,String fileRpath,Long filedId) throws IOException {
        QueryWrapper<File> fileWrapper = new QueryWrapper<>();
        fileWrapper.eq("user_id", userId).eq("file_rpath",fileRpath).eq("file_id",filedId).eq("file_delete", 1);
        //File f2 = fileService.getOne(fileWrapper);
        QueryWrapper<Access> accessWrapper = new QueryWrapper<>();
        accessWrapper.eq("user_id", userId).eq("file_id", filedId);
        QueryWrapper<Share> shareQueryWrapper = new QueryWrapper<>();
        shareQueryWrapper.eq("file_id",filedId).eq("user_id",userId);
        Boolean iaremove = accessService.remove(accessWrapper);
        Boolean isremove = shareService.remove(shareQueryWrapper);
        Boolean ifremove = fileService.remove(fileWrapper);
    }
    public Boolean deleteFolder(String fileRpath,Long userId) throws IOException {
        //文件夹
        try {
            if (fileRpath.endsWith("/")) {
                QueryWrapper<File> queryWrapper = new QueryWrapper<>();
                queryWrapper.likeRight("file_rpath", fileRpath).eq("user_id",userId).eq("file_delete",1);
                List<File> fList = fileService.list(queryWrapper);
                //空文件夹 -->直接删除
                if (fList.size() == 1) {
                    for (File f : fList){
                        obsDeleteMethod(f.getUserId(),f.getFileRpath(),f.getFileId());
                    }
                }
                //文件夹还有子文件
                else {
                    for (File f : fList){
                        //文件夹
                        if(f.getFileRpath().endsWith("/")) {
                            if (!f.getFileRpath().equals(fileRpath)){
                                deleteFolder(f.getFileRpath(),userId);
                                obsService.deleteByPath(f.getFileRpath());
                            }
                        }
                        else {
                            obsDeleteMethod(f.getUserId(),f.getFileRpath(),f.getFileId());
                            obsService.deleteByPath(f.getFileRpath());
                        }
                    }
                }
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean deleteRecycleBin(Long userId) throws IOException {
            String str =  keySuffixWithSlash + RecycleuffixWithSlash + userId.toString() + "/";
            List<FileVO> dirList = obsService.getDeleteFileList(str, true, userId);
            List<FileVO> fileList = obsService.getDeleteFileList(str, false, userId);
            List<FileVO> list = new ArrayList<>();
            list.addAll(dirList);
            list.addAll(fileList);
            for (FileVO fileVo : list) {
                QueryWrapper<File> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id",userId).eq("file_delete",1).eq("file_id",fileVo.getFileId());
                File f = fileService.getOne(queryWrapper);
                if (f == null) {
                    break;
                }
                if (f.getFileRpath().endsWith("/")) {
                    //文件夹
                    Boolean is = deleteFolder(f.getFileRpath(), userId);
                    obsService.deleteByPath(f.getFileRpath());
                    if (!is) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    }
                }
                //回收站单个文件
                else {
                    obsDeleteMethod(f.getUserId(), f.getFileRpath(), f.getFileId());
                    obsService.deleteByPath(f.getFileRpath());
                }
            }
       // }
        return true;
    }

//    @RequestMapping("cleanRecycleBin")
//    public JsonData cleanRecycleBin(@RequestBody JSONObject jsonObject) {
//        Long userId = jsonObject.getLong("userId");
//        if (userId == null) {
//            return JsonData.buildError("用户ID为空");
//        }
//        try {
//            deleteRecycleBin(userId);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return JsonData.buildSuccess("回收站已清空");
//
//    }

    @RequestMapping("cleanRecycleBin")
    public JsonData cleanRecycleBin(@RequestParam(value = "userId") Long userId) {

        try {
            deleteRecycleBin(userId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JsonData.buildSuccess("回收站已清空");

    }
}

