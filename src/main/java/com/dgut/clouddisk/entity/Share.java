package com.dgut.clouddisk.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
@ApiModel(value="Share对象", description="")
public class Share implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "share_id")
    @ApiModelProperty(value = "分享ID")
    private String shareId;

    @ApiModelProperty(value = "文件ID")
    private Long fileId;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "分享的时长（小时为单位）")
    private Integer shareTime;

    @ApiModelProperty(value = "创建分享链接的时间")
    @TableField(value = "share_Ctime", fill = FieldFill.INSERT)
    private Date shareCtime;

    @ApiModelProperty(value = "分享地址")
    private String shareUrl;

}
