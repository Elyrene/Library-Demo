package com.Library.controller;

import com.Library.common.DataInfo;
import com.Library.entity.Admin;
import com.Library.mapper.AdminMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.autoconfigure.PageHelperProperties;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {
    private final AdminMapper adminMapper;
    private final PageHelperProperties pageHelperProperties;

    private AdminController(AdminMapper adminMapper, PageHelperProperties pageHelperProperties) {
        this.adminMapper = adminMapper;
        this.pageHelperProperties = pageHelperProperties;
    }

    @GetMapping
    public DataInfo<List<Admin>> list (@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int limit,
                                       @RequestParam(required = false) String username,
                                       @RequestParam(required = false) Integer adminType) {

        PageHelper.startPage(pageNum, limit);
        List<Admin> list = adminMapper.queryAll(username, adminType);
        PageInfo<Admin> pageInfo = PageInfo.of(list);
        list.forEach(a -> a.setPassword(null));
        return DataInfo.ok(list, pageInfo.getTotal());

    }
}
