package com.dgut.clouddisk.mapper;

import com.dgut.clouddisk.entity.File;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dgut.clouddisk.entity.User;
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

public interface FileMapper extends BaseMapper<File> {

    @Select("select file_path from file where file_id='${fileId}'")
    String getFilePathById(Long fileId);
}
