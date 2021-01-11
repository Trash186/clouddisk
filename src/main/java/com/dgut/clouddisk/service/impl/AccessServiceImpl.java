package com.dgut.clouddisk.service.impl;

import com.dgut.clouddisk.entity.Access;
import com.dgut.clouddisk.mapper.AccessMapper;
import com.dgut.clouddisk.service.AccessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
@Service
public class AccessServiceImpl extends ServiceImpl<AccessMapper, Access> implements AccessService {

    @Resource
    private AccessMapper accessMapper;

    public List<Access> selectByUserId(long userId){
        List<Access> access = accessMapper.selectByUserId(userId);
        return access;
    }
}
