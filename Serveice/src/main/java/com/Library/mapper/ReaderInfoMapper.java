package com.Library.mapper;

import com.Library.entity.ReaderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReaderInfoMapper {

    ReaderInfo findByUsername(@Param("username") String username);
}
