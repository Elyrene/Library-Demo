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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
