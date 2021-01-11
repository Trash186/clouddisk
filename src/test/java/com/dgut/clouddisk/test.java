package com.dgut.clouddisk;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dgut.clouddisk.entity.File;
import com.dgut.clouddisk.service.impl.FileServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@SpringBootTest
public class test {

    @Autowired
    FileServiceImpl fileService;

    @Test
    void testSql() {
        List<String> list=new ArrayList<>();
        list.add("12345666");
        list.add("5666");
        list.add("12345");
        String sel="666";
        String pattern=".*"+sel+".*";
        for (String string : list)
        {
            boolean isMatch = Pattern.matches(pattern, string);
            if (isMatch) System.out.println(string);
        }

    }
    @Test
    void  teststring()
    {
        String s="测试/testsql";
        String test="测试1/1309348597859766273";
        String a[];

        String b[]=s.split("/");
        int l=b.length;
        System.out.println(b[l-1]);
        b[l-1]="testsql11";
        String filePath=b[0];
        for(int x=1;x<b.length;x++)
        {
            filePath=filePath+"/"+b[x];
        }
        System.out.println(filePath);

    }
}
