package com.dgut.clouddisk.entity;

import java.io.Serializable;
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
@ApiModel(value="Access对象", description="")
public class Access implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "文件ID")
    private Long fileId;

    @ApiModelProperty(value = "是否可读")
    private Integer accessRead;

    @ApiModelProperty(value = "是否可写")
    private Integer accessWrite;

    @ApiModelProperty(value = "是否可上传")
    private Integer accessrUp;

    @ApiModelProperty(value = "是否可下载")
    private Integer accessDown;

    @ApiModelProperty(value = "是否可分享")
    private Integer accessShare;


}
