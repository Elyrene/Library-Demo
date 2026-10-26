package com.Library.mapper;

import com.Library.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    Notice findById(@Param("id") Integer id);
    List<Notice> queryAll(@Param("topic") String topic,
                          @Param("content") String content);
    int insert(Notice notice);
    int update(Notice notice);
    int deleteByIds(@Param("ids") List<Integer> ids);
}
