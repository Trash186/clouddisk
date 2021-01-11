package com.dgut.clouddisk.entity.vo;

import com.dgut.clouddisk.entity.User;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Lai Jiantian
 * @Date 2020/9/21 16:41
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTokenVO {
    private User user;
    private Integer validTime;
}
