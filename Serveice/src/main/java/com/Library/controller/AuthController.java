package com.Library.controller;

import com.Library.common.Contants;
import com.Library.common.DataInfo;
import com.Library.controller.dto.LoginRequest;
import com.Library.entity.Admin;
import com.Library.entity.ReaderInfo;
import com.Library.mapper.AdminMapper;
import com.Library.mapper.ReaderInfoMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminMapper adminmapper;
    private final ReaderInfoMapper readerInfoMapper;

    public AuthController(AdminMapper adminmapper, ReaderInfoMapper readerInfoMapper) {
        this.adminmapper = adminmapper;
        this.readerInfoMapper = readerInfoMapper;
    }

    @PostMapping("/login")
    public DataInfo<Void> login(@Valid @RequestBody LoginRequest res, HttpSession session) {
        if ("admin".equals(res.getRole())) {
            Admin admin = adminmapper.findByUsername(res.getUsername());
            if (admin == null || admin.getPassword() == null || !admin.getPassword().equals(res.getPassword())) {
                return DataInfo.fail("账户或者密码错误");
            }
            session.setAttribute(Contants.SESSION_USER, admin);
            session.setAttribute(Contants.SESSION_TYPE, "admin");
            return DataInfo.ok();
        }

        ReaderInfo reader =  readerInfoMapper.findByUsername(res.getUsername());
        if (reader == null || reader.getPassword() == null || !reader.getPassword().equals(res.getPassword())) {
            return DataInfo.fail("账户或者密码错误");
        }
        session.setAttribute(Contants.SESSION_USER, reader);
        session.setAttribute(Contants.SESSION_TYPE, "reader");
        return DataInfo.ok();
    }

    @PostMapping("/logout")
    public DataInfo<Void> logout(HttpSession session) {
        session.removeAttribute(Contants.SESSION_USER);
        session.removeAttribute(Contants.SESSION_TYPE);
        session.invalidate();
        return DataInfo.ok();
    }

    @GetMapping("/me")
    public DataInfo<Map<String, Object>> me (HttpSession session) {
        Object user = session.getAttribute(Contants.SESSION_USER);
        Object type = session.getAttribute(Contants.SESSION_TYPE);

        if (user == null || type == null) {
            return DataInfo.fail("未登录");

        }

        HashMap<String, Object> res = new HashMap<>();
        res.put("role", type);
        if (user instanceof Admin a) {
            Admin safe = new Admin();
            safe.setId(a.getId());
            safe.setUsername(a.getUsername());
            safe.setAdminType(a.getAdminType());
            res.put("user", safe);
        } else if (user instanceof ReaderInfo r) {
            ReaderInfo safe = new ReaderInfo();
            safe.setId(r.getId());
            safe.setUsername(r.getUsername());
            safe.setRealName(r.getRealName());
            safe.setSex(r.getSex());
            safe.setBirthday(r.getBirthday());
            safe.setAddress(r.getAddress());
            safe.setTel(r.getTel());
            safe.setEmail(r.getEmail());
            safe.setRegisterDate(r.getRegisterDate());
            safe.setReaderNumber(r.getReaderNumber());
            res.put("user", safe);
        } else {
            res.put("user", null);

        }

        return DataInfo.ok(res);
    }
}
