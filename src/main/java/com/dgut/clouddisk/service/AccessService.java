package com.dgut.clouddisk.service;

import com.dgut.clouddisk.entity.Access;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
public interface AccessService extends IService<Access> {
    List<Access> selectByUserId(long userId);

}
