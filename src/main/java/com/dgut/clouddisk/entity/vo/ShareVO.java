package com.dgut.clouddisk.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareVO implements Serializable {
    private String shareId;
    private Long fileId;
    private String fileName;
    private Long userId;
    private Integer shareTime;
    private String deadTime;  //失效时间
    private Date shareCtime;
    private String virtualUrl;  //虚拟地址
}