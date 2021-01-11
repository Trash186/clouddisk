package com.dgut.clouddisk.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dgut.clouddisk.entity.Department;
import com.dgut.clouddisk.entity.File;
import com.dgut.clouddisk.entity.User;
import com.dgut.clouddisk.entity.vo.FileVO;
import com.dgut.clouddisk.exception.CloudException;
import com.dgut.clouddisk.mapper.UserMapper;
import com.dgut.clouddisk.service.FileService;
import com.dgut.clouddisk.service.impl.DepartmentServiceImpl;
import com.dgut.clouddisk.service.impl.FileServiceImpl;
import com.dgut.clouddisk.service.impl.ObsServiceImpl;
import com.dgut.clouddisk.service.impl.UserServiceImpl;
import com.dgut.clouddisk.util.JsonData;
import com.dgut.clouddisk.util.StatusCode;
import com.obs.services.model.DeleteObjectResult;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import io.swagger.models.auth.In;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
@RestController
@RequestMapping("/clouddisk/file")
public class FileController {
    @Resource
    private FileServiceImpl FileService;
    @Resource
    private UserServiceImpl UserService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ObsServiceImpl obsService;
    @Resource
    private DepartmentServiceImpl departmentService;

    private final String keySuffixWithSlash = "测试1/";
    private final String RecycleuffixWithSlash = "recycleBin/";

    @RequestMapping("getProperties")
    public JsonData getproperties(long Fileid) {
        File file = FileService.getById(Fileid);
        User user = UserService.getById(file.getUserId());//获取上传者信息
        Map<String, File> map = new HashMap<>();
        //map.put(file,user.getUserRealname());
        map.put(user.getUserRealname(), file);
        return JsonData.buildSuccess(map);
    }//显示文件属性

    /**
     * @return
     * @throws IOException
     */
    @RequestMapping("setFileName")
    public JsonData setFileName(@RequestBody JSONObject jsonObject) throws IOException {
        /*
        传入重命名文件id与命名后的名字
        * */
        String filePath = jsonObject.get("filePath").toString();
        String fileNewPath = jsonObject.get("fileNewPath").toString();
        QueryWrapper<File> queryWrapper = new QueryWrapper<>();
        filePath = keySuffixWithSlash + filePath;
        fileNewPath = keySuffixWithSlash + fileNewPath;
        queryWrapper.eq("file_path", filePath);
        File F = FileService.getOne(queryWrapper);
        String name = F.getFilePath();
        String[] l = fileNewPath.split("/");
        String newname = l[l.length - 1];
        F.setFilePath(fileNewPath);
        F.setFileName(newname);
        FileService.updateById(F);
        obsService.rename(name, fileNewPath);
        QueryWrapper<File> wrapper = new QueryWrapper<>();
        wrapper.likeRight("file_path", name);
        List<File> list = FileService.list(wrapper);
        int i = 0;
        for (File file : list) {
            String[] a = file.getFilePath().split(name);
            String jia = fileNewPath + a[1];
            file.setFilePath(jia);
            FileService.updateById(file);
            i++;
        }
        F.setFilePath(fileNewPath);
        FileService.updateById(F);
        return JsonData.buildSuccess("修改成功");
    }


    @RequestMapping("obsFile")
    public JsonData obsFiles() {
        return JsonData.buildSuccess("1");
    }

    /**
     * 根据用户ID获取文件列表   已作废 谨慎使用！！！！！！ 最新的在下面
     *
     * @param userId 用户ID
     * @return
     * @throws IOException
     */
    @RequestMapping("getFileByID")
    public JsonData getFileByID(Long userId) throws IOException {
        try {
            List<ObsObject> list = obsService.getUserObsObject(userId);
            if (list != null && list.size() > 0) {
                return JsonData.buildSuccess(list);
            } else {
                return JsonData.buildError("没有对象存储");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return JsonData.buildError("obs存储访问异常");
        }
    }

    // 已作废
    @RequestMapping("deleteFile")
    public JsonData deleteFile(@RequestParam(value = "userId") Long userId, @RequestParam(value = "file") String file) {
        try {
            Boolean is = obsService.deleteFileToRecycle(null, file, userId);
            if (is) {
                String name = file.substring(file.lastIndexOf("/") + 1);
                String path = file.substring(0, file.lastIndexOf("/") + 1);
                File f = new File();
                f.setFileName(name);
                f.setUserId(userId);
                f.setFileUptime(new Date());
                f.setFilePath(file);
                f.setFileDelete(1);
                f.setFileModitime(new Date());
                FileService.save(f);
                return JsonData.buildSuccess("删除成功");
            } else {
                return JsonData.buildError("删除失败");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return JsonData.buildError("obs存储访问异常");
        }
    }

    @RequestMapping("/deleteGroupFiles")
    public JsonData deleteGroupFiles(String fileIds) throws IOException{
        String[] list = fileIds.split(",");
        for(String fid : list){
            FileService.deleteGroupFile(Long.parseLong(fid));
        }
        return JsonData.buildSuccess("删除成功");
    }

    //根据文件id单个或批量删除文件
    @RequestMapping("deleteFiles")
    public JsonData deleteFiles(@RequestParam(value = "userId") Long userId, String fileIds) throws IOException {
        String str = keySuffixWithSlash + RecycleuffixWithSlash + userId.toString() + "/";
        String[] list = fileIds.split(",");
        User user = userMapper.selectById(userId);
        long size1 = user.getUserRemainingsize();//根据用户id获得用户当前剩余可用空间
        long size2 = user.getUserUsedsize();//根据用户id获取用户删除文件前已用空间
        for (String fid : list) {
            QueryWrapper<File> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("file_id", fid).eq("file_delete", 0);
            File file = FileService.getOne(queryWrapper);
            long size3 = file.getFileSize();//获得要删除的文件的大小
            size2 = size2 - size3;//删除文件后用户已用空间=删除文件前已用空间-删除文件的大小
            size1 = size1 + size3;//删除文件后的剩余可用空间=当前剩余可用空间+删除文件的大小
            if (file == null) {
                return JsonData.buildSuccess("删除内容可能已经不存在");
            }
            String filepath = file.getFilePath();
            //传入的文件夹
            if (filepath.endsWith("/")) {
                Integer length = filepath.substring(0, filepath.lastIndexOf("/", filepath.lastIndexOf("/") - 1) + 1).length();
//                System.out.println(length+"==="+filepath.substring(0,filepath.lastIndexOf("/",filepath.lastIndexOf("/")-1)+1));
                //更新File
                folderORfile(filepath, userId, length, str);
                UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("file_id", file.getFileId()).set("file_delete", 1).set("file_rpath", str + filepath.substring(length)).set("file_modiTime", new Date());
                FileService.update(updateWrapper);
                //更新User
                UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                userUpdateWrapper.eq("user_id", user.getUserId()).set("user_usedSize", size2).set("user_remainingSize", size1);
                UserService.update(userUpdateWrapper);
                DeleteObjectResult is = obsService.deleteByPath(filepath);
                if (is == null) {
                    return JsonData.buildError("OBS删除失败");
                }
            }
            //传入的文件
            else {
                Boolean is = obsService.deleteFileToRecycle(filepath, filepath.substring(filepath.lastIndexOf("/") + 1), userId);
                if (!is) {
                    return JsonData.buildError("OBS删除失败");
                } else {
                    UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("file_id", file.getFileId()).set("file_delete", 1).set("file_rpath", str + filepath.substring(filepath.lastIndexOf("/") + 1)).set("file_modiTime", new Date());
                    FileService.update(updateWrapper);
                    //更新User
                    UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                    userUpdateWrapper.eq("user_id", user.getUserId()).set("user_usedSize", size2).set("user_remainingSize", size1);
                    UserService.update(userUpdateWrapper);
                }
            }
        }
        return JsonData.buildSuccess("删除成功");
    }

    public Boolean updateFile(Long fileId, String filepath, Long userId, Integer length, String str) {
        System.out.println(filepath);
        //删除文件夹
        UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("file_id", fileId).set("file_delete", 1).set("file_rpath", str + filepath.substring(length)).set("file_modiTime", new Date());
        FileService.update(updateWrapper);
        //obs删除
        Boolean is = null;
        try {
            is = obsService.deleteFileToRecycle(filepath, filepath.substring(length), userId);
        } catch (IOException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
        return true;

    }

    public Boolean folderORfile(String filepath, Long userId, Integer length, String str) throws IOException {
        //文件夹
        try {
            if (filepath.endsWith("/")) {
                QueryWrapper<File> queryWrapper2 = new QueryWrapper<>();
                queryWrapper2.likeRight("file_path", filepath).eq("file_delete", 0);
                List<File> fList = FileService.list(queryWrapper2);
                //空文件夹
                if (fList.size() == 1) {
                    for (File f : fList) {
                        Boolean is = updateFile(f.getFileId(), f.getFilePath(), userId, length, str);
                        if (!is) {
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return false;
                        }
                    }
                } else {
                    for (File f : fList) {
                        //还有文件夹
                        if (f.getFilePath().endsWith("/")) {
                            System.out.println(f.getFilePath());
                            if (!f.getFilePath().equals(filepath)) {
                                folderORfile(f.getFilePath(), userId, length, str);
                                UpdateWrapper<File> updateWrapper = new UpdateWrapper<>();
                                updateWrapper.eq("file_id", f.getFileId()).set("file_delete", 1).set("file_rpath", str + f.getFilePath().substring(length));
                                FileService.update(updateWrapper);
                                obsService.deleteByPath(f.getFilePath());
                            }
                        }
                        //文件,创建文件,---  除去filepath的第二个“/”之前
                        else {
                            Boolean is = updateFile(f.getFileId(), f.getFilePath(), userId, length, str);
                            if (!is) {
                                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                                return false;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @RequestMapping("/testForUpload")
    public JsonData testForUpload(MultipartFile file, Long userId) {
        User user = userMapper.selectById(userId);
        File file1 = new File();
        long size = user.getUserRemainingsize();
        if (size <= 0 || size - file.getSize() <= 0) {
            return JsonData.buildError("空间不足，上传失败，请联系管理员提升您的权限！");
        } else if (file != null) {
            String path = keySuffixWithSlash + userId + "/" + file.getOriginalFilename();
            //防止上传重复的文件名...
            QueryWrapper<File> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda().eq(File::getFilePath,path);
            File one = FileService.getOne(queryWrapper);
            if(one!=null){
                throw new CloudException(StatusCode.FILE_IS_EXISTED.code(),StatusCode.FILE_IS_EXISTED.message());
            }
            file1.setFileName(file.getOriginalFilename());
            file1.setUserId(userId);
            file1.setFilePath(path);
            file1.setFileSize((int) file.getSize());
            file1.setFileModitime(new Date(System.currentTimeMillis()));
            file1.setFileUptime(new Date(System.currentTimeMillis()));
            try {
                PutObjectResult putObjectResult = obsService.testForUploadFile(path, file.getBytes());
                if (putObjectResult != null) {
                    //用户已用空间=已用空间+上传的文件大小
                    long size1 = user.getUserUsedsize() + file1.getFileSize();
                    //用户剩余空间=用户总空间-用户已用空间
                    long size2 = user.getUserSpacesize() - size1;
                    UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                    userUpdateWrapper.eq("user_id", user.getUserId()).set("user_usedSize", size1).set("user_remainingSize", size2);
                    UserService.update(userUpdateWrapper);

                    FileService.save(file1);
                    return JsonData.buildSuccess("文件上传成功");
                } else
                    return JsonData.buildError("上传失败！");
            } catch (IOException e) {
                e.printStackTrace();
                return JsonData.buildError("obs访问失败");
            }
        } else {
            return JsonData.buildError("没有数据");
        }
    }

    /**
     * 上传文件到群组文件夹
     *
     * @param file
     * @param userId
     * @param path   当前路径
     * @return JsonData
     * @throws IOException
     */
    @RequestMapping("/uploadToGroup")
    public JsonData uploadToGroup(MultipartFile file, Long userId, String path) throws IOException {
        User user = UserService.getById(userId);
        Department department = departmentService.getById(user.getDepartmentId());
        File f = new File();
        if (file != null) {
            String fPath = keySuffixWithSlash+department.getDepartmentName()+path + file.getOriginalFilename();
            QueryWrapper<File> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda().eq(File::getFilePath,fPath);
            File one = FileService.getOne(queryWrapper);
            if(one!=null){
                throw new CloudException(StatusCode.FILE_IS_EXISTED.code(),StatusCode.FILE_IS_EXISTED.message());
            }
            f.setFileName(file.getOriginalFilename());
            f.setUserId(userId);
            f.setFilePath(fPath);
            f.setFileSize((int) file.getSize());
            f.setFileModitime(new Date());
            f.setFileUptime(new Date());
            PutObjectResult putObjectResult = obsService.testForUploadFile(fPath, file.getBytes());
            if (putObjectResult != null) {
                FileService.save(f);
                return JsonData.buildSuccess("文件上传成功");
            } else
                return JsonData.buildError("上传失败！");
        } else {
            return JsonData.buildError("没有数据");
        }
    }

    @RequestMapping("downloadFile")
    public JsonData downloadFile(String objectName) throws IOException {
        byte[] bytes = obsService.downloadFile(objectName);
        return JsonData.buildSuccess("文件下载成功");
    }

    @RequestMapping("downloadFileByIds")
    public JsonData downloadFileById(String fileIds) throws IOException {
        byte[] bytes = obsService.downloadFileById(fileIds);
        return JsonData.buildSuccess("文件下载成功");
    }


    /* 演示例子：
    请求：
    {
        "userId": 1307962995784585217,
        "path": "/"
    }
    "/" 指根路径   根路径下的TestA文件夹 写作==> "path":"/TestA/"
     */

    /**
     * 获取文件列表
     *
     * @param jsonObject 请求用户ID、路径
     * @return 文件列表
     */
    @PostMapping("/getFileList")
    public JsonData getFileList(@RequestBody JSONObject jsonObject) {
        return getObsList(jsonObject, false);
    }

    /**
     * 获取文件夹列表
     *
     * @param jsonObject 请求用户ID、路径
     * @return 文件夹列表
     */
    @PostMapping("/getDirList")
    public JsonData getDirList(@RequestBody JSONObject jsonObject) {
        return getObsList(jsonObject, true);
    }

    @PostMapping("/getAllList")
    public JsonData getAllFile(@RequestBody JSONObject jsonObject) {
        Long userId = jsonObject.getLong("userId");
        if (userId == null) {
            return JsonData.buildError("用户ID为空");
        }
        String path = userId.toString() + jsonObject.get("path").toString();
        List<FileVO> dirList = obsService.getFileList(path, true, userId);
        List<FileVO> fileList = obsService.getFileList(path, false, userId);
        List<FileVO> all = new ArrayList<>();
        all.addAll(dirList);
        all.addAll(fileList);
        return JsonData.buildSuccess(all);
    }

    @RequestMapping("/getAllGroupFile")
    public JsonData getAllGroupFile(@RequestBody JSONObject jsonObject){
        Long userId = jsonObject.getLong("userId");
        if (userId == null) {
            return JsonData.buildError("用户信息错误");
        }
        User user = UserService.selectById(userId);
        Integer departmentId = user.getDepartmentId();
        Department department = departmentService.getById(departmentId);
        if (department==null){
            return JsonData.buildError("用户信息错误");
        }
        String path = department.getDepartmentName()+jsonObject.get("path").toString();
        List<FileVO> dirList = obsService.getGroupFileList(path, true);
        List<FileVO> fileList = obsService.getGroupFileList(path, false);
        List<FileVO> all = new ArrayList<>();
        all.addAll(dirList);
        all.addAll(fileList);
        return JsonData.buildSuccess(all);
    }


    /**
     * 获取列表
     *
     * @param jsonObject 请求用户ID、路径
     * @param b          返回文件或文件夹 true为文件夹
     * @return 列表
     */
    private JsonData getObsList(@RequestBody JSONObject jsonObject, boolean b) {
        Long userId = jsonObject.getLong("userId");
        if (userId == null) {
            return JsonData.buildError("用户ID为空");
        }
        String path = userId.toString() + jsonObject.get("path").toString();
        List<FileVO> fileList = obsService.getFileList(path, b, userId);
        if (fileList != null) {
            return JsonData.buildSuccess(fileList);
        } else {
            return JsonData.buildError("获取失败");
        }
    }

    /**
     * @param jsonObject 请求用户ID、路径
     * @return
     */
    @RequestMapping("/selectFile")//文件模糊查询
    public JsonData selectFileList(@RequestBody JSONObject jsonObject) {
        Long userId = jsonObject.getLong("userId");
        if (userId == null) {
            return JsonData.buildError("用户ID为空");
        }
        String path = userId.toString() + jsonObject.get("path").toString();
        String keywords = jsonObject.get("keywords").toString();
        List<FileVO> dirList = obsService.getFileList(path, true, userId);
        List<FileVO> fileList = obsService.getFileList(path, false, userId);
        List<FileVO> all = new ArrayList<>();
        all.addAll(dirList);
        all.addAll(fileList);
        List<FileVO> select = new ArrayList<>();
        String pattern = ".*" + keywords + ".*";
        for (FileVO file : all) {
            boolean isMatch = Pattern.matches(pattern, file.getFileName());
            if (isMatch) select.add(file);
        }
        return JsonData.buildSuccess(select);
    }

    @RequestMapping("/cutFile")
    public JsonData cutFile() {
        return null;
    }

    /**
     * 用户在当前路径下创建文件夹
     *
     * @param jsonObject 用户ID，当前路径，文件夹名字
     * @return
     */
    @PostMapping("/createNewDir")
    public JsonData createNewDir(@RequestBody JSONObject jsonObject) {
        Long userId = Long.valueOf(jsonObject.get("userId").toString());
        if (userId == null) {
            return JsonData.buildError("用户ID为空");
        }
        String path = keySuffixWithSlash + userId + jsonObject.get("path").toString();
        String dirName = jsonObject.get("dirName").toString();
        FileService.createNewDir(userId, path, dirName);
        return JsonData.buildSuccess("创建文件夹成功");
    }


    /**
     * 群组创建文件夹在当前路径
     *
     * @param jsonObject 用户ID，当前路径，文件夹名字
     * @return
     */
    @PostMapping("/createNewGroupDir")
    public JsonData createNewGroupDir(@RequestBody JSONObject jsonObject) {
        Long userId = Long.valueOf(jsonObject.get("userId").toString());
        User user = UserService.getById(userId);
        Department department = departmentService.getById(user.getDepartmentId());
        if (userId == null) {
            return JsonData.buildError("用户ID为空");
        }
        String path = keySuffixWithSlash + department.getDepartmentName() + jsonObject.get("path").toString();
        String dirName = jsonObject.get("dirName").toString();
        FileService.createNewDir(userId, path, dirName);
        return JsonData.buildSuccess("创建文件夹成功");
    }

    /**
     * 创建群组文件夹
     *
     * @param jsonObject 用户ID，文件夹名字(部门名)
     * @return
     */
    @RequestMapping("/createGroupDir")
    public JsonData createGroupDir(@RequestBody JSONObject jsonObject) throws IOException {
        Long userId = Long.valueOf(jsonObject.get("userId").toString());
        if (userId == null) {
            return JsonData.buildError("用户ID为空");
        }
        String dirName = jsonObject.get("dirName").toString();
        FileService.createGroupDir(userId, dirName);
        return JsonData.buildSuccess("创建群组文件夹成功");
    }

    /**
     * 根据文件的绝对路径得到文件ID
     *
     * @param path 绝对路径
     * @return FileId 文件ID
     */
    @RequestMapping("/getFileId")
    public String getFileId(String path) {
        QueryWrapper<File> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(File::getFilePath, path);
        File one = FileService.getOne(queryWrapper);
        return one.getFileId().toString();
    }

    /**
     * 文件复制
     *
     * @param jsonObject 用户ID，需要复制的文件ID，复制到的目标目录ID
     * @return 结果
     */
    @RequestMapping("/copyFile")
    public JsonData copyFile(@RequestBody JSONObject jsonObject) {
        // 获取需要复制的文件ID
        JSONArray sourceIds = jsonObject.getJSONArray("sourceIds");
        if (sourceIds.size() > 0) {
            // 获取用户ID、需要复制的文件ID列表、目的路径ID
            Long userId = jsonObject.getLong("userId");
            List<Long> lists = sourceIds.toJavaList(Long.class);
            Long destId = jsonObject.getLong("destId");
            for (Long sourceId : lists) {
                if (userId != null && sourceId != null && destId != null) {
                    File file = FileService.getById(sourceId);
                    File dest = FileService.getById(destId);
                    User user = UserService.selectById(userId);
                    if (file == null || dest == null || user == null) {
                        return JsonData.buildError("请正确输入信息");
                    } else {
                        // 文件名
                        String fileName = file.getFileName();
                        // 原路径
                        String sourcePath = file.getFilePath();
                        // 目的路径
                        String destPath = dest.getFilePath() + fileName;
                        // 如果是文件夹，需加 /
                        if (sourcePath.endsWith("/")) {
                            destPath += "/";
                            // 判断是否复制成功
                            if (!copyDir(userId,  sourcePath, destPath)) {
                                return JsonData.buildError(-2,"文件名重名，复制失败");
                            }
                        }
                        // 普通文件
                        else {
                            if (!sendCopy(userId, sourceId, destPath)) {
                                return JsonData.buildError(-2,"文件名重名，复制失败");
                            }
                        }
                    }
                } else {
                    return JsonData.buildError("请正确输入信息");
                }
            }
            return JsonData.buildSuccess(true);
        } else {
            return JsonData.buildError("请正确输入信息");
        }
    }


    /**
     * 复制单个文件
     * @param userId 用户ID
     * @param sourceId 源ID
     * @param destPath 目标地址
     * @return 复制结果
     */
    private Boolean sendCopy(Long userId, Long sourceId, String destPath) {
        // 判断是否重名
        if (FileService.isSame(userId, destPath)) {
            return false;
        } else {
//            System.out.println("==============================================================================================================================");
//            System.out.println("用户ID==>" + userId + "源文件ID==>" + sourceId + "目的地址==>" + destPath);
//            return JsonData.buildSuccess(true);
            try {
                Boolean isCopy = obsService.copyFileByPath(userId, sourceId, destPath);
                return isCopy;
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * 复制文件夹
     * @param userId 用户ID
     * @param sourcePath 源地址
     * @param destPath 目标地址
     * @return 结果
     */
    private Boolean copyDir(Long userId,  String sourcePath, String destPath) {
        // 判断是否重名
        if (FileService.isSame(userId, destPath)) {
            return false;
        } else {
            // 如果是文件夹
            if (destPath.endsWith("/")) {
                QueryWrapper<File> wrapper = new QueryWrapper<>();
                wrapper.likeRight("file_path", sourcePath).eq("file_delete", 0);
                List<File> fileList = FileService.list(wrapper);
                for (File file : fileList) {
                    String filePath = file.getFilePath();
                    // 拼接路径
                    String addPath = filePath.substring(filePath.indexOf(file.getFileName()));
                    // 文件夹
                    if (filePath.endsWith("/")) {
                        // 如果是该文件夹本身
                        if (filePath.equals(sourcePath)) {
                            sendCopy(userId, file.getFileId(), destPath);
                        } else {
                            sendCopy(userId, file.getFileId(), destPath + addPath);
                        }
                    } else {
                        // 普通文件
                        String aFPath = filePath.substring(filePath.lastIndexOf("/", filePath.lastIndexOf("/") - 1) + 1);
                        aFPath = aFPath.substring(0, aFPath.lastIndexOf("/") + 1);
                        if (filePath.substring(0, filePath.lastIndexOf("/") + 1).equals(sourcePath)) {
                            String dPath = destPath + file.getFileName();
                            sendCopy(userId, file.getFileId(), dPath);
                        } else {
                            String dPath = destPath + aFPath + file.getFileName();
                            sendCopy(userId, file.getFileId(), dPath);
                        }
                    }
                }

            }
            return true;
        }
    }

    /**
     * 获取路的ID
     *
     * @param jsonObject 用户ID，路径名
     * @return 路径的ID
     */
    @RequestMapping("/getPathId")
    public JsonData getPathIdByPath(@RequestBody JSONObject jsonObject) {
        Long userId = jsonObject.getLong("userId");
        String path = jsonObject.get("path").toString();
        String finalPath = keySuffixWithSlash + userId.toString() + path;
        QueryWrapper<File> wrapper = new QueryWrapper<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("user_id", userId);
        map.put("file_path", finalPath);
        wrapper.allEq(map);
        File file = FileService.getOne(wrapper);
        if (file != null) {
            return JsonData.buildSuccess(file.getFileId().toString());
        } else {
            return JsonData.buildError("查询不到");
        }
    }


}

