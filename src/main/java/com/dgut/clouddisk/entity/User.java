package com.dgut.clouddisk.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * <p>
 * 
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="User对象", description="")
public class User extends BaseRowModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)   // 用户ID自动注入，由雪球算法生成的随机且唯一ID，insert时ID字段不填即可
    @JsonDeserialize(using = LongJsonDeserializer.class)
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;

    @ApiModelProperty(value = "部门ID")
    @ExcelProperty(value = "部门ID",index = 0)
    private Integer departmentId;

    @ApiModelProperty(value = "用户名")
    @ExcelProperty(value = "用户名",index = 1)
    private String userName;

    @ApiModelProperty(value = "真实姓名")
    @ExcelProperty(value = "真实姓名",index = 2)
    private String userRealname;

    @ApiModelProperty(value = "密码")
    @ExcelProperty(value = "密码",index = 3)
    private String userPwd;

    @ApiModelProperty(value = "手机号码")
    @ExcelProperty(value = "手机号码",index = 4)
    private String userMobile;

    @ApiModelProperty(value = "用户邮箱")
    @ExcelProperty(value = "用户邮箱",index = 5)
    private String userEmail;

    @ApiModelProperty(value = "可用空间")
    @TableField("user_spaceSize")
    private Long userSpacesize;

    @ApiModelProperty(value = "已使用空间")
    @TableField("user_usedSize")
    private Long userUsedsize;

    @ApiModelProperty(value = "剩余空间")
    @TableField("user_remainingSize")
    private Long userRemainingsize;

    @ApiModelProperty(value = "用户角色")
    private Integer userPart;

    @ApiModelProperty(value = "用户状态")
    @TableLogic
    private Integer userStatus;     // 用户删除设置为逻辑删除，删除用户时，用户状态置为1，update select操作时自动跳过该记录

    @TableField(exist = false)
    @ExcelIgnore
    private Map<Integer, CellStyle> cellStyleMap = new HashMap<Integer, CellStyle>();
}
