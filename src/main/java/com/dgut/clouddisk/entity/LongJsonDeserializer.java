package com.dgut.clouddisk.entity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import java.lang.String;
import java.io.IOException;

/**
 * @author Baller_Xiao
 * @create 2020-10-04 21:08
 */
//将接收的前端字符串类型转换成Long类型
public class LongJsonDeserializer extends JsonDeserializer<Long>
{
    private static final Logger logger = LoggerFactory.getLogger(LongJsonDeserializer.class);

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException
    {
        String value = jsonParser.getText();
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
         //   logger.error("数据转换异常", e);
            return null;
        }
    }
}