package com.dgut.clouddisk.controller;


import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dgut.clouddisk.entity.Access;
import com.dgut.clouddisk.entity.File;
import com.dgut.clouddisk.entity.User;
import com.dgut.clouddisk.entity.vo.UserTokenVO;
import com.dgut.clouddisk.mapper.AccessMapper;
import com.dgut.clouddisk.mapper.FileMapper;
import com.dgut.clouddisk.mapper.UserMapper;
import com.dgut.clouddisk.service.AccessService;
import com.dgut.clouddisk.service.ObsService;
import com.dgut.clouddisk.service.impl.*;
import com.dgut.clouddisk.util.Email;
import com.dgut.clouddisk.util.ExcelListener;
import com.dgut.clouddisk.util.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.issCollege.util.MD5;
import com.obs.services.model.DeleteObjectResult;
import com.obs.services.model.PutObjectResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
@RestController
@RequestMapping("/clouddisk/user")
public class UserController {
    @Resource
    private UserServiceImpl userService;

    @Resource
    private ObsServiceImpl obsService;

    @Resource
    private FileServiceImpl fileService;

    @Resource
    private AccessServiceImpl accessService;

    @Resource
    private EmailServiceImpl emailService;

    @Resource
    private JedisPool jedisPool;

    /**
     * 获取所有用户(已废弃)
     * @return
     */
    @RequestMapping("/getAllUser")
    public JsonData getAllUser(){
        List<User> list = userService.list();
        return JsonData.buildSuccess(list);
    }
    /**
     * 获取所有用户
     * @return
     */
    @RequestMapping("/getAllUsers")
    public JsonData getAllUsers(int pageCount,int pageSize){
        PageInfo<User> users = userService.getAllUser(pageCount,pageSize);
        System.out.println("哈哈哈哈"+JsonData.buildSuccess(users));
        return JsonData.buildSuccess(users);
    }




    //添加单个用户
    @RequestMapping("/saveUser")
    public JsonData saveAUser(@RequestBody User user) throws IOException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getUserEmail, user.getUserEmail()).or().eq(User::getUserName,user.getUserName());
        User one = userService.getOne(queryWrapper);
        //查找用户是否存在
        if (one != null) {
            return JsonData.buildError("用户已存在");
        }
        user.setUserPwd(MD5.stringMD5(user.getUserPwd()));//将密码加密
        if (userService.save(user)) {
            PutObjectResult dir = obsService.createDir(user.getUserId().toString());//obs创建文件夹
            PutObjectResult Recycledir = obsService.createRecycleBin(user.getUserId().toString());
            if(dir==null) {
                return JsonData.buildError("添加用户成功，但是创建文件夹失败");
            }
            if(Recycledir==null) {
                return JsonData.buildError("添加用户成功，但是创建文件夹失败");
            }
            System.out.println(dir);
            fileService.createDir(user.getUserId());//创建文件夹
            return JsonData.buildSuccess(user);
        } else {
            return JsonData.buildError("添加用户失败");
        }
    }

    /**
     * 批量导入用户
     *
     * @param file
     * @return JsonData
     */
    @PostMapping("/importUsers")
    public JsonData importUsers(MultipartFile file) throws Exception {
        Boolean result = userService.importUsers(file, userService, obsService,fileService);
        if(result){
            return JsonData.buildSuccess("导入成功");
        }else{
            return JsonData.buildError("导入失败");
        }

    }

    /**
     * 删除单个用户
     * @param userId
     * @return
     */
    @RequestMapping("/deleteUser")
    public JsonData deleteUser(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return JsonData.buildError("该用户不存在");
        }
        boolean result = userService.removeById(userId);
        if (result == true) {
            //obsService.deleteObject(userId.toString());
            fileService.deletePerFile(userId);
            return JsonData.buildSuccess("删除成功");
        }
        return JsonData.buildError("删除失败");
    }


    /**
     * 批量删除用户
     * 前端传来ids
     * @param ids
     * @return
     */
    @RequestMapping("/deleteUsers")
    public JsonData deleteUsers(String ids){
        String[] list=ids.split(",");
        for(String id :list){
            long l = Long.parseLong(id);
            User user = userService.getById(l);
            boolean result = userService.removeById(l);
            obsService.deleteObject(id);
            fileService.deletePerFile(l);
            if(result==false){
                return JsonData.buildError(user.getUserName()+"删除失败");
            }
        }
        return JsonData.buildSuccess("删除成功");
    }


    /**
     * 重设密码
     *
     * @param jsonObject 传入的旧密码与新密码
     * @return 返回提示信息
     * @throws JsonProcessingException 反序列化异常
     */
    @PostMapping("resetPwd")
    public JsonData resetPwd(@RequestBody(required = false) JSONObject jsonObject,HttpServletRequest request) throws JsonProcessingException {
        Jedis jedis = jedisPool.getResource();
        String token = request.getHeader("token");
        String newPwd = jsonObject.get("newPwd").toString();
        String oldPwd = jsonObject.get("oldPwd").toString();
        //根据token获取序列化后的当前用户
        String tokenVal = jedis.get(token);
        ObjectMapper mapper = new ObjectMapper();
        // 反序列化已封装的token对象
        UserTokenVO userTokenVO = mapper.readValue(tokenVal, UserTokenVO.class);
        jedis.close();
        //根据id找出当前的用户
        User currentUser = userService.getById(userTokenVO.getUser().getUserId());
        if(MD5.stringMD5(oldPwd).equals(currentUser.getUserPwd())) {
            if (newPwd == null || newPwd.replace(" ", "").isEmpty()) {
                return JsonData.buildError("请输入新的密码");
            } else if (MD5.stringMD5(newPwd).equals(currentUser.getUserPwd())) {
                return JsonData.buildError("请输入与旧密码不相同的新密码");
            } else
                //重新设置当前用户的密码
                currentUser.setUserPwd(MD5.stringMD5(newPwd));
            Boolean flag = userService.updateById(currentUser);
            if (flag)
                return JsonData.buildSuccess(null);
            else
                return JsonData.buildError("更新密码失败");
        }
        return JsonData.buildError("请输入正确的旧密码");

    }

    /**
     * 当前登录人员查看个人信息
     *
     * @return 返回提示信息
     * @throws JsonProcessingException 反序列化异常
     */
    @RequestMapping("showUserInfo")
    public JsonData showUserInfo(HttpServletRequest request) throws JsonProcessingException {
        Jedis jedis = jedisPool.getResource();
        String token = request.getHeader("token");
        //根据token获取序列化后的当前用户
        String tokenVal = jedis.get(token);
        ObjectMapper mapper = new ObjectMapper();
        // 反序列化已封装的token对象
        UserTokenVO userTokenVO = mapper.readValue(tokenVal, UserTokenVO.class);
        jedis.close();
        //根据id找出当前的用户
        User currentUser = userService.getById(userTokenVO.getUser().getUserId());
        return JsonData.buildSuccess(currentUser);
    }


    /**
     * 管理员查找用户（搜索框输入真实姓名模糊查询）
     *
     * @param realName  真实姓名
     * @param pageCount 当前页数
     * @param pageSize  每页显示的总记录数
     * @return 成功返回符合条件的用户信息，失败返回提示信息
     */
    @RequestMapping("selectUser")
    public JsonData selectUserByRealName(String realName,int pageCount,int pageSize) {
        //模糊查询,有分页功能
        PageInfo<User> user = userService.selectUserRealName(realName,pageCount,pageSize);
        if (user != null) {
            return JsonData.buildSuccess(user);
        } else {
            //这个存在该用户，或者该账号用户是管理员
            return JsonData.buildError("不存在该用户！");
        }
    }

    /**
     * 管理员查看该用户个人详情
     *
     * @param userId 用户Id
     * @return 成功返回该用户信息，失败返回提示信息
     */
    @RequestMapping("selectUserById")
    public JsonData selectUserById(long userId) {
        User user = userService.selectById(userId);
        if (user != null) {
            return JsonData.buildSuccess(user);
        } else {
            //这个存在该用户，或者该账号用户是管理员
            return JsonData.buildError("不存在该用户！");
        }
    }

    /**
     * 管理员查看该用户在群组文件夹的权限
     *
     * @param userId 用户Id
     * @return 成功返回该用户信息，失败返回提示信息
     */
    @RequestMapping("selectPowerById")
    public JsonData selectPowerById(long userId) {
        List<Access> access = accessService.selectByUserId(userId);
        if (access != null) {
            return JsonData.buildSuccess(access);
        } else {
            return JsonData.buildError("该用户在群组没有任何权限！");
        }
    }

    /**
     * 管理员修改用户信息
     *
     * @param user
     * @return 返回修改后的用户信息
     */
    @RequestMapping("updateUser")
    public JsonData updateUser(@RequestBody User user) {
        //UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        //根据id更新信息
        //userUpdateWrapper.eq("user_id", user.getUserId());

        userService.updateUser(user);
        user = userService.selectById(user.getUserId());
        System.out.println("user:"+user);

        //userService.update(user, userUpdateWrapper);
        return JsonData.buildSuccess(user);
    }

    /**
     * 管理员修改该用户在群组文件夹的权限
     *
     * @param access
     * @return 返回修改后该用户的权限信息
     */
    @RequestMapping("updataPower")
    public JsonData updataPower(@RequestBody Access access) {
        UpdateWrapper<Access> accessUpdateWrapper = new UpdateWrapper<>();
        accessUpdateWrapper.eq("file_id", access.getFileId());
        accessUpdateWrapper.eq("user_id", access.getUserId());

        accessService.update(access, accessUpdateWrapper);
        return JsonData.buildSuccess(access);
    }

    /**
     * 根据ID 获取容量信息
     * @param jsonObject 用户ID
     * @return 容量信息
     */
    @RequestMapping("/getSizeById")
    public JsonData getUserSize(@RequestBody JSONObject jsonObject){
        HashMap<String, Object> map = new HashMap<>();
        Long id=jsonObject.getLong("ID");
        if (id!=null){
            User user = userService.selectById(id);
            map.put("spaceSize",user.getUserSpacesize());
            map.put("usedSize",user.getUserUsedsize());
            map.put("remain",user.getUserRemainingsize());
            return JsonData.buildSuccess(map);
        }else
            return JsonData.buildError("请正确输入ID");
    }

    /**
     * 根据ID获取邮箱地址
     * @param jsonObject 用户ID
     * @return 返回查找信息
     */
    @RequestMapping("/getEmailById")
    public JsonData getEmailById(@RequestBody JSONObject jsonObject){
        Long id=jsonObject.getLong("ID");
        if (id!=null){
            User user = userService.selectById(id);
            String userEmail = user.getUserEmail();
            return JsonData.buildSuccess(userEmail);
        }else
            return JsonData.buildError("请正确输入ID");
    }

    /**
     * 验证邮箱和验证码是否匹配
     *
     * @param jsonObject Email: 邮箱 Code: 验证码
     * @return 成功返回邮箱号码，失败返回提示信息
     * @throws JsonProcessingException 反序列化异常
     */
    @RequestMapping("/verifyEmail")
    public JsonData unBindEmail(@RequestBody JSONObject jsonObject) throws JsonProcessingException {
        if (emailService.verifyEmail(jsonObject)) {
            return JsonData.buildSuccess(jsonObject.get("Email"));
        } else {
            return JsonData.buildError("验证码错误或验证码过期");
        }
    }


    /**
     * 邮箱换绑
     *
     * @param jsonObject Email：新邮箱，Code：验证码，OldEmail:旧邮箱
     * @return 成功返回新邮箱，失败返回提示信息
     * @throws JsonProcessingException JSON反序列化异常
     * @throws MessagingException      发送邮件异常
     */
    @RequestMapping("/unbind")
    public JsonData unbindEmail(@RequestBody JSONObject jsonObject) throws JsonProcessingException, MessagingException {
        if (emailService.verifyEmail(jsonObject)) {
            User user = new User();
            String newEmail = jsonObject.get("Email").toString();
            String oldEmail = jsonObject.get("OldEmail").toString();
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            if (newEmail.equals(oldEmail)){
                return JsonData.buildError(-4,"新邮箱与旧邮箱相同");
            }
            user.setUserEmail(newEmail);
            wrapper.eq("user_email", oldEmail);
            if (userService.update(user, wrapper)) {
                Boolean sendEmail = emailService.sendSuccess(new Email(newEmail, "账号安全中心-邮箱换绑", "邮箱"));
                if (!sendEmail) {
                    return JsonData.buildError(-3,"发送通知邮件失败");
                }
                wrapper.clear();
                wrapper.eq("user_email", newEmail);
                User one = userService.getOne(wrapper);
                return JsonData.buildSuccess(one);
            } else {
                return JsonData.buildError(-2,"修改失败");
            }
        } else {
            return JsonData.buildError(-1,"验证码错误或验证码过期");
        }
    }

    /**
     * 邮箱是否存在
     *
     * @param jsonObject Email:需要判断的邮箱地址
     * @return 存在返回 该邮箱地址，失败返回提示信息
     */
    @PostMapping("/isEmail")
    public JsonData emailExist(@RequestBody JSONObject jsonObject) {
        String email = jsonObject.get("Email").toString();
//        User one = null;
//        try {
//            one = userService.getOne(wrapper);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//        }
        if (userService.emailExist(email)) {
            return JsonData.buildSuccess(email);
        } else {
            return JsonData.buildError("用户不存在");
        }
    }

    @PostMapping("/findPwd")
    public JsonData findPwd(@RequestBody JSONObject jsonObject) throws JsonProcessingException, MessagingException {
        if (emailService.verifyEmail(jsonObject)) {
            // 验证码正确
            String newPwd = jsonObject.get("NewPwd").toString();
            String md5 = MD5.stringMD5(newPwd);
            String email = jsonObject.get("Email").toString();
            if (email != null && newPwd != null) {
                QueryWrapper<User> wrapper = new QueryWrapper<>();
                wrapper.eq("user_email", email);
                User user = new User();
                user.setUserPwd(md5);
                if (userService.update(user, wrapper)) {
                    Boolean sendEmail = emailService.sendSuccess(new Email(email, "账号安全中心-修改密码", "密码"));
                    if (!sendEmail) {
                        return JsonData.buildError("发送通知邮件失败");
                    }
                    return JsonData.buildSuccess(email);
                } else {
                    return JsonData.buildError("更改密码错误");
                }
            } else {
                return JsonData.buildError("邮箱或密码不能为空");
            }
        } else {
            return JsonData.buildError("验证码错误或验证码过期");
        }
    }
}

