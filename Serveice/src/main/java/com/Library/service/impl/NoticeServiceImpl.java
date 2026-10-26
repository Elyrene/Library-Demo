package com.Library.service.impl;

import com.Library.entity.Notice;
import com.Library.mapper.NoticeMapper;
import com.Library.service.NoticeService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeServiceImpl(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    @Override
    public Notice getById(Integer id) {
        return noticeMapper.findById(id);
    }

    @Override
    public List<Notice> list(String topic, String content) {
        return noticeMapper.queryAll(topic, content);
    }

    @Override
    public String add(Notice notice) {
        if (notice.getTopic() == null || notice.getTopic().trim().isEmpty()) {
            return "公告主题不能为空";
        }
        if (notice.getContent() == null || notice.getContent().trim().isEmpty()) {
            return "公告内容不能为空";
        }
        if (notice.getCreateDate() == null) {
            notice.setCreateDate(new Date());
        }
        int rows = noticeMapper.insert(notice);
        return rows > 0 ? null : "发布公告失败";
    }

    @Override
    public String update(Integer id, Notice notice) {
        Notice exist = noticeMapper.findById(id);
        if (exist == null) {
            return "公告不存在";
        }
        if (notice.getTopic() == null || notice.getTopic().trim().isEmpty()) {
            return "公告主题不能为空";
        }
        notice.setId(id);
        int rows = noticeMapper.update(notice);
        return rows > 0 ? null : "修改公告失败";
    }

    @Override
    public int deleteByIds(List<Integer> ids) {
        return noticeMapper.deleteByIds(ids);
    }
}
