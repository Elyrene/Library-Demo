package com.Library.mapper;

import com.Library.entity.BookInfo;
import com.Library.entity.LendList;
import com.Library.entity.ReaderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LendListMapper {
    List<LendList> queryAll(@Param("bookName") String bookName,
                            @Param("readerName") String readerName,
                            @Param("backType") Integer backType);
    LendList findById(@Param("id") Integer id);
    int insert(LendList lendList);
    int updateBack(@Param("id") Integer id,
                   @Param("backDate") java.util.Date backDate,
                   @Param("backType") Integer backType,
                   @Param("exceptRemarks") String exceptRemarks);
    int deleteByIds(@Param("ids") List<Integer> ids);
    List<LendList> findByReaderId(@Param("readerId") Integer readerId);
    List<LendList> findByBookId(@Param("bookId") Integer bookId);
    List<LendList> findActiveByBookId(@Param("bookId") Integer bookId);
}
