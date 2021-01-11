package com.dgut.clouddisk.service;

import com.dgut.clouddisk.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dgut.clouddisk.service.impl.FileServiceImpl;
import com.dgut.clouddisk.service.impl.ObsServiceImpl;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
public interface UserService extends IService<User> {
    /**
     * 用户登录验证
     * @param user 用户登录信息
     * @return 验证成功返回该用户信息，失败返回null
     */
    User login(User user);

    /**
     * 批量导入用户
     * @param file
     * @param userService
     * @return
     */
    Boolean importUsers(MultipartFile file, UserService userService, ObsServiceImpl obsService, FileServiceImpl fileService) throws Exception;

    //实现分页
    PageInfo<User> selectUserRealName(String realName, int pageCount, int pageSize);

    User selectById(long userId);

    void updateUser(User user);

    PageInfo<User> getAllUser(int pageCount, int pageSize);

    // 判断邮箱是否存在
    Boolean emailExist(String email);
}
