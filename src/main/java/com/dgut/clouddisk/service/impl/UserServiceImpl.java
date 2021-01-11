package com.dgut.clouddisk.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dgut.clouddisk.entity.User;
import com.dgut.clouddisk.exception.CloudException;
import com.dgut.clouddisk.mapper.UserMapper;
import com.dgut.clouddisk.service.ObsService;
import com.dgut.clouddisk.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dgut.clouddisk.util.ExcelListener;
import com.dgut.clouddisk.util.JsonData;
import com.dgut.clouddisk.util.StatusCode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 用户登录验证
     * @param user 用户登录信息
     * @return 验证成功返回该用户信息，失败返回null
     */
    @Override
    public User login(User user) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        HashMap<String, Object> map = new HashMap<>();
        if (user.getUserName()==null){
            // 如果用户名为空，则判断邮箱和密码
            map.put("user_email",user.getUserEmail());
        }else {
            map.put("user_name",user.getUserName());
        }
        map.put("user_pwd", user.getUserPwd());
        wrapper.allEq(map);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 批量导入用户
     * @param file
     * @param userService
     */
    @Override
    @Transactional
    public Boolean importUsers(MultipartFile file, UserService userService, ObsServiceImpl obsService,FileServiceImpl fileService) throws Exception{
        String originalFilename = file.getOriginalFilename();
        if (!originalFilename.endsWith(ExcelTypeEnum.XLS.getValue()) && !originalFilename.endsWith(ExcelTypeEnum.XLSX.getValue())) {
            System.out.println("Excel导入错误文件名称" + originalFilename);
            throw new CloudException(StatusCode.FILE_FORMAT_ERROR.code(), StatusCode.FILE_FORMAT_ERROR.message());
        }
        InputStream in = file.getInputStream();
        EasyExcel.read(in,User.class,new ExcelListener(userService,obsService,fileService)).sheet().doRead();
        in.close();
        return true;
    }

    public PageInfo<User> getAllUser(int pageCount, int pageSize){
        PageHelper.startPage(pageCount,pageSize);
        List<User> users = userMapper.getList();
        return  new PageInfo(users);
    }

    @Override
    public Boolean emailExist(String email) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_email", email);
        User one = userMapper.selectOne(wrapper);
        return email != null && one != null;
    }

    //分页
    public PageInfo<User> selectUserRealName(String realName, int pageCount, int pageSize){

        PageHelper.startPage(pageCount,pageSize);
        List<User> user = userMapper.selectUserRealName('%' + realName + '%');
        return new PageInfo(user);
    }
    public User selectById(long userId){
        User user = userMapper.selectById(userId);
        return user;
    }

    public void updateUser(User user){
        userMapper.updateUser(user);
    }

}
