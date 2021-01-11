package com.dgut.clouddisk.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author Lai Jiantian
 * @Date 2020/10/6 13:18
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileVO implements Serializable {
    private String FileId;
    private String fileName;
    private Integer fileSize;
    private Date fileModitime;
}
