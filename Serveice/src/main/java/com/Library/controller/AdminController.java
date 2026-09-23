package com.Library.controller;

import com.Library.common.DataInfo;
import com.Library.entity.Admin;
import com.Library.mapper.AdminMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.autoconfigure.PageHelperProperties;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/{id}")
    public DataInfo<Admin> getById(@PathVariable Integer id) {
        Admin admin = adminMapper.findById(id);
        if (admin == null) {
            return DataInfo.fail("管理员不存在");
        }
        admin.setPassword(null);
        return DataInfo.ok(admin);
    }

    @PostMapping()
    public DataInfo<Void> add(@RequestBody Admin admin) {
        Admin exist = adminMapper.findByUsername(admin.getUsername());
        if (exist != null) {
            return DataInfo.fail("用户名不存在");
        }
        if (admin.getAdminType() == null) {
            admin.setAdminType(1);
        }
        int row = adminMapper.insert(admin);
        return row > 0 ? DataInfo.ok() : DataInfo.fail("新增失败");
    }

    @PutMapping("/{id}/password")
    public DataInfo<Void> updatePassword(@PathVariable Integer id,
                                         @RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || password.trim().isEmpty()) {
            return DataInfo.fail("密码不能为空");
        }
        int rows = adminMapper.updatePassword(id, password.trim());
        return rows > 0 ? DataInfo.ok() : DataInfo.fail("修改失败");
    }

    @DeleteMapping
    public DataInfo<Void> delete(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return DataInfo.fail("请选择要删除的管理员");
        }
        int rows = adminMapper.deleteByIds(ids);
        return rows > 0 ? DataInfo.ok() : DataInfo.fail("删除失败");
    }


}
