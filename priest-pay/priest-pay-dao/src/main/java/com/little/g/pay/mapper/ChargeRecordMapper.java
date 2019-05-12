package com.little.g.pay.mapper;

import com.little.g.pay.model.ChargeRecord;
import com.little.g.pay.model.ChargeRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface ChargeRecordMapper {
    long countByExample(ChargeRecordExample example);

    int deleteByExample(ChargeRecordExample example);

    int deleteByPrimaryKey(String tranNum);

    int insert(ChargeRecord record);

    int insertSelective(ChargeRecord record);

    List<ChargeRecord> selectByExampleWithRowbounds(ChargeRecordExample example, RowBounds rowBounds);

    List<ChargeRecord> selectByExample(ChargeRecordExample example);

    ChargeRecord selectByPrimaryKey(String tranNum);

    int updateByExampleSelective(@Param("record") ChargeRecord record, @Param("example") ChargeRecordExample example);

    int updateByExample(@Param("record") ChargeRecord record, @Param("example") ChargeRecordExample example);

    int updateByPrimaryKeySelective(ChargeRecord record);

    int updateByPrimaryKey(ChargeRecord record);
}