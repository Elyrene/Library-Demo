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
    int insert(BookInfo book);
    int update(BookInfo book);
    int updateStatusById(@Param("id") Integer id, @Param("status") Integer status);
    int deleteByIds(@Param("ids") List<Integer> ids);
}
