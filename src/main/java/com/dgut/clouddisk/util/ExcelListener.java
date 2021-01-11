package com.dgut.clouddisk.util;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dgut.clouddisk.entity.User;
import com.dgut.clouddisk.exception.CloudException;
import com.dgut.clouddisk.service.ObsService;
import com.dgut.clouddisk.service.UserService;
import com.dgut.clouddisk.service.impl.FileServiceImpl;
import com.dgut.clouddisk.service.impl.ObsServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.issCollege.util.MD5;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 监听类
 * 监听导入excel
 */
@Slf4j
public class ExcelListener extends AnalysisEventListener {

    private ObsServiceImpl obsService;

    /**
     * 自定义用于暂时存储data。
     * 可以通过实例获取该值
     */
    private List<User> data = new ArrayList<>();

    private UserService userService;

    private FileServiceImpl fileService;

    public ExcelListener(UserService userService,ObsServiceImpl obsService,FileServiceImpl fileService) {
        super();
        this.userService = userService;
        this.obsService  = obsService;
        this.fileService = fileService;
    }



    @SneakyThrows
    @Override
    public void invoke(Object o, AnalysisContext context) {
        System.out.println(context.getCurrentSheet());
        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writeValueAsString(o);
        User user = mapper.readValue(s,User.class);
        int rowIndex = context.readRowHolder().getRowIndex() + 1;
        if(rowIndex<=1){
            throw new CloudException(StatusCode.USER_IS_EMPTY.code(),StatusCode.USER_IS_EMPTY.message());
        }
        if(user.getDepartmentId()==null){
            throw new CloudException(StatusCode.CONTENT_HAVE_EMPTY.code(),"第"+rowIndex+"行部门ID为空");
        }
        if(user.getUserName()==null){
            throw new CloudException(StatusCode.CONTENT_HAVE_EMPTY.code(),"第"+rowIndex+"行用户名为空");
        }
        if(user.getUserRealname()==null){
            throw new CloudException(StatusCode.CONTENT_HAVE_EMPTY.code(),"第"+rowIndex+"真实姓名为空");
        }
        if(user.getUserPwd()==null){
            throw new CloudException(StatusCode.CONTENT_HAVE_EMPTY.code(),"第"+rowIndex+"行用户密码为空");
        }
        if(user.getUserEmail()==null){
            throw new CloudException(StatusCode.CONTENT_HAVE_EMPTY.code(),"第"+rowIndex+"行邮箱为空");
        }
        if(user.getUserMobile()==null){
            throw new CloudException(StatusCode.CONTENT_HAVE_EMPTY.code(),"第"+rowIndex+"行手机号码为空");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getUserEmail, user.getUserEmail()).or().eq(User::getUserName,user.getUserName());
        User one = userService.getOne(queryWrapper);
        if(one!=null){
            throw new CloudException(StatusCode.USER_IS_EXISTED.code(),StatusCode.USER_IS_EXISTED.message());
        }
        user.setUserPwd(MD5.stringMD5(user.getUserPwd()));
        data.add(user);

    }

    public void doSomething() throws IOException {
        try {
            boolean b = userService.saveBatch(data);
            if(!b)
                throw new CloudException(StatusCode.DATABASE_ERROR.code(),StatusCode.DATABASE_ERROR.message());
        }catch (Exception e ){
            throw new CloudException(StatusCode.DATABASE_ERROR.code(),StatusCode.DATABASE_ERROR.message());
        }for(User u:data){
            obsService.createDir(u.getUserId().toString());
            fileService.createDir(u.getUserId());//创建文件夹
        }
    }


    @SneakyThrows
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        doSomething();
    }

    public List<User> getData() {
        return data;
    }

    public void setData(List<User> data) {
        this.data = data;
    }


}
