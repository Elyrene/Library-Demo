package com.Library.mapper;

import com.Library.entity.Admin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminMapper {
    List<Admin> queryAll(@Param("username") String username, @Param("adminType") Integer adminType);

    Admin findByUsername(@Param("username") String username);
    int insert(Admin admin);
    int updatePassword(@Param("id") Integer id, @Param("password") String password);
    int deleteByIds(@Param("ids") List<Integer> ids);

}
