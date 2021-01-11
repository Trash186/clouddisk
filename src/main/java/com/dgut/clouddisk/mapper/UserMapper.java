package com.dgut.clouddisk.mapper;

import com.dgut.clouddisk.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.io.Serializable;
import java.util.List;

import static org.apache.naming.SelectorContext.prefix;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE user_realname LIKE '${realName}' AND user_status=0 ")
    List<User> selectUserRealName(String realName);
    @Update("<script> update user " +
            "<trim prefix='set' suffixOverrides=','>" +
            "<if test='userName != null'>"+ "user_name='${userName}',"+"</if>"+
            "<if test='departmentId != null'>"+ "department_id='${departmentId}',"+"</if>"+
            "<if test='userRealname != null'>"+ "user_realname='${userRealname}',"+"</if>"+
            "<if test='userPwd != null'>"+ "user_pwd='${userPwd}',"+"</if>"+
            "<if test='userMobile != null'>"+ "user_mobile='${userMobile}',"+"</if>"+
            "<if test='userEmail != null'>"+ "user_email='${userEmail}',"+"</if>"+
            "<if test='userSpacesize != null'>"+ "user_spaceSize='${userSpacesize}',"+"</if>"+
            "<if test='userUsedsize != null'>"+ "user_usedSize='${userUsedsize}',"+"</if>"+
            "<if test='userRemainingsize != null'>"+ "user_remainingsize='${userRemainingsize}',"+"</if>"+
            "<if test='userPart != null'>"+ "user_part='${userPart}',"+"</if>"+
            "<if test='userStatus != null'>"+ "user_status='${userStatus}',"+"</if>"+
            "</trim>" + "where user_id = ${userId}" + "</script>")
    void updateUser(User user);

    @Select("select * from user where user_status=0 and user_part!=1")
    List<User> getList();
}
