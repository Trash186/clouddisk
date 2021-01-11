package com.dgut.clouddisk.mapper;

import com.dgut.clouddisk.entity.Share;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dgut.clouddisk.entity.vo.ShareVO;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import javax.xml.crypto.Data;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Group04
 * @since 2020-09-21
 */
public interface ShareMapper extends BaseMapper<Share> {

    @Insert("insert into share (share_id,file_id,user_id,share_time,share_Ctime,share_url) values ('${shareId}','${fileId}','${userId}','${shareTime}','${share_Ctime}','${shareUrl}')")
    void insertShareLink(String shareId,Long fileId, Long userId , Integer shareTime, Date share_Ctime,String shareUrl);

    @Select("select * from share where share_id='${shareId}'")
    Share getByshareId(String shareId);

    @Select("select a.*,b.file_name from share a ,file b where a.user_id='${userId}' and a.file_id=b.file_id ")
    List<ShareVO> getByUserId(Long userId);
    @Select("select a.*,b.file_name from share a ,file b where a.share_id='${shareId}' and a.file_id=b.file_id ")
    ShareVO getByShareId(String shareId);

    @Select("delete from share where share_id='${shareId}'")
    void deleteByShareId(String shareId);


}
