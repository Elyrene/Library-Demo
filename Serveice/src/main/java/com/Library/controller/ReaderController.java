package com.Library.controller;


import com.Library.common.DataInfo;
import com.Library.controller.dto.RegisterRequest;
import com.Library.entity.ReaderInfo;
import com.Library.mapper.ReaderInfoMapper;
import com.github.pagehelper.autoconfigure.PageHelperProperties;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/readers")
public class ReaderController {
    private final ReaderInfoMapper readerInfoMapper;
    private final PageHelperProperties pageHelperProperties;

    public ReaderController(ReaderInfoMapper readerInfoMapper, PageHelperProperties pageHelperProperties) {
        this.readerInfoMapper = readerInfoMapper;
        this.pageHelperProperties = pageHelperProperties;
    }

    @PostMapping("/register")
    public DataInfo<Void> register(@Valid @RequestBody RegisterRequest res) {
        ReaderInfo exist = readerInfoMapper.findByUsername(res.getUsername());
        if (exist != null) {
            return DataInfo.fail("用户名已经存在");
        }

        ReaderInfo reader = new ReaderInfo();
        BeanUtils.copyProperties(res, reader);
        reader.setRegisterDate(new Date());
        reader.setReaderNumber(generateReaderNumber());

        int rows = readerInfoMapper.insert(reader);
        if (rows > 0) {
            return DataInfo.ok();
        }
        return DataInfo.fail("注册失败，请稍后重试");

    }

    private String generateReaderNumber() {
        String maxNumber = readerInfoMapper.findMaxReaderNumber();
        if (maxNumber == null) {
            // 表里没有数据时，从 001 开始
            return "8120116" + "001";
        }
        // 取出前缀和流水号部分
        String prefix = maxNumber.substring(0, maxNumber.length() - 3);
        int seq = Integer.parseInt(maxNumber.substring(maxNumber.length() - 3));
        seq++;
        // 流水号超过 999 时进位到 4 位（也可按需改成报错）
        return prefix + String.format("%03d", seq);
    }
}
