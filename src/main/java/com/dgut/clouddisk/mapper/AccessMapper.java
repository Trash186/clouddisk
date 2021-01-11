package com.dgut.clouddisk.mapper;

import com.dgut.clouddisk.entity.Access;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
public interface AccessMapper extends BaseMapper<Access> {
    @Select("SELECT * FROM access WHERE user_id = ${userId} ")
    List<Access> selectByUserId(long userId);


}
