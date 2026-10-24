package com.Library.mapper;

import com.Library.entity.BookInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookInfoMapper {
    BookInfo findById(@Param("id") Integer id);
    List<BookInfo> queryAll(@Param("name") String name,
                            @Param("typeId") Integer typeId,
                            @Param("status") Integer status);
    int updateStatus(BookInfo book);
    int updateStatusById(@Param("id") Integer id, @Param("status") Integer status);
}
