package com.Library.service;

import com.Library.entity.Notice;

import java.util.List;

public interface NoticeService {
    Notice getById(Integer id);
    List<Notice> list(String topic, String content);
    String add(Notice notice);
    String update(Integer id, Notice notice);
    int deleteByIds(List<Integer> ids);
}
