package com.dgut.clouddisk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
@ApiModel(value="File对象", description="")
public class File implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件ID")
    @TableId(value = "file_id", type = IdType.ASSIGN_ID)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long fileId;

    @ApiModelProperty(value = "文件名")
    private String fileName;

    @ApiModelProperty(value = "上传者")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = LongJsonDeserializer.class)
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;

    @ApiModelProperty(value = "文件路径")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String filePath;

    @ApiModelProperty(value = "回收站地址")
    private String fileRpath;


    @ApiModelProperty(value = "文件大小")
    private Integer fileSize;

    @ApiModelProperty(value = "文件上传时间")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @TableField(fill = FieldFill.INSERT)
    private Date fileUptime;

    @ApiModelProperty(value = "最后修改时间")
    @TableField(value = "file_modiTime", fill = FieldFill.INSERT_UPDATE)
    private Date fileModitime;

    @ApiModelProperty(value = "是否被删除")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer fileDelete;


    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public File(String fileName, Integer fileSize, Date fileModitime) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileModitime = fileModitime;
    }

}
