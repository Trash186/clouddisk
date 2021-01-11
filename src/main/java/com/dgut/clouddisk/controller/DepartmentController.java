package com.dgut.clouddisk.controller;


import com.alibaba.fastjson.JSONObject;
import com.dgut.clouddisk.entity.Department;
import com.dgut.clouddisk.entity.File;
import com.dgut.clouddisk.service.impl.DepartmentServiceImpl;
import com.dgut.clouddisk.service.impl.FileServiceImpl;
import com.dgut.clouddisk.service.impl.ObsServiceImpl;
import com.dgut.clouddisk.util.JsonData;
import com.obs.services.model.PutObjectResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
@RestController
@RequestMapping("/clouddisk/department")
public class DepartmentController {
    @Resource
    DepartmentServiceImpl departmentService;
    @Resource
    ObsServiceImpl obsService;

    @Resource
    private FileServiceImpl fileService;


//    /**
//     * 添加一个部门
//     *
//     * @param department JSON格式化部门信息
//     * @return JsonData
//     */
//    @RequestMapping("/saveDep")
//    public JsonData saveADepartment(@RequestBody Department department,Long userId) throws IOException {
//        if (departmentService.save(department)) {
//            PutObjectResult dir = obsService.createDir(department.getDepartmentName());
//            if(dir==null){
//                return JsonData.buildError("OBS创建部门文件夹失败");
//            }
//            fileService.createDepartDir(userId,department.getDepartmentName());
//            return JsonData.buildSuccess(department);
//        } else {
//            return JsonData.buildError("创建部门失败");
//        }
//    }
//
//    /**
//     * 批量添加部门
//     *
//     * @param departments JSON格式化部门信息
//     * @return JsonData
//     */
//    @RequestMapping("/saveDeps")
//    public JsonData saveMoreDepartments(@RequestBody List<Department> departments,Long userId) {
//        if (departmentService.saveBatch(departments)) {
//            for (Department d :departments){
//                fileService.createDepartDir(userId,d.getDepartmentName());
//            }
//            return JsonData.buildSuccess(departments);
//        } else {
//            return JsonData.buildError("创建部门失败");
//        }
//    }

    @RequestMapping("/list")
    public JsonData list() {
        List<Department> list = departmentService.list();
        if (list != null){
            return JsonData.buildSuccess(list);
        }else {
            return JsonData.buildError("查找部门失败");
        }
    }

    //通过Id来获取部门名
    @RequestMapping("/getById")
    public JsonData getById(Integer departmentId) {
        Department department = departmentService.getById(departmentId);
        if (department != null) {
            return JsonData.buildSuccess(department);
        } else {
            return JsonData.buildError("暂未加入部门！");
        }
    }

    /**
     * 根据部门ID获取部门名 JSON版
     * @param jsonObject 部门ID
     * @return 部门名
     */
    @RequestMapping("/getDepById")
    public JsonData getDepByIdJson(@RequestBody JSONObject jsonObject){
        Integer departmentId = jsonObject.getInteger("departmentId");
        if (departmentId!=null){
            Department department = departmentService.getById(departmentId);
            if (department!=null){
                return JsonData.buildSuccess(department.getDepartmentName());
            }else {
                return JsonData.buildError(-2,"暂未加入部门");
            }
        }else {
            return JsonData.buildError(-1,"请正确输入部门ID");
        }
    }
}

