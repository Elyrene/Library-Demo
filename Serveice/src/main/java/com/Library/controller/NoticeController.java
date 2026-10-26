package com.Library.controller;

import com.Library.common.Contents;
import com.Library.common.DataInfo;
import com.Library.entity.Notice;
import com.Library.service.NoticeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.autoconfigure.PageHelperProperties;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;
    private final PageHelperProperties pageHelperProperties;

    public NoticeController(NoticeService noticeService, PageHelperProperties pageHelperProperties) {
        this.noticeService = noticeService;
        this.pageHelperProperties = pageHelperProperties;
    }

    /**
     * 判断当前登录用户是否为管理员（Session 中 SESSION_TYPE == "admin"）。
     */
    private boolean isAdmin(HttpSession session) {
        return "admin".equals(session.getAttribute(Contents.SESSION_TYPE));
    }

    /**
     * 分页查询公告列表（按发布时间倒序），支持按主题 / 内容关键词筛选
     */
    @GetMapping
    public DataInfo<List<Notice>> list(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int limit,
                                       @RequestParam(required = false) String topic,
                                       @RequestParam(required = false) String content) {
        PageHelper.startPage(pageNum, limit);
        List<Notice> list = noticeService.list(topic, content);
        PageInfo<Notice> pageInfo = PageInfo.of(list);
        return DataInfo.ok(list, pageInfo.getTotal());
    }

    /**
     * 根据 ID 查询公告
     */
    @GetMapping("/{id}")
    public DataInfo<Notice> getById(@PathVariable Integer id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return DataInfo.fail("公告不存在");
        }
        return DataInfo.ok(notice);
    }

    /**
     * 发布公告（仅管理员）
     */
    @PostMapping
    public DataInfo<Void> add(@RequestBody Notice notice, HttpSession session) {
        if (!isAdmin(session)) {
            return DataInfo.fail("仅管理员可发布公告");
        }
        if (notice.getAuthor() == null || notice.getAuthor().trim().isEmpty()) {
            notice.setAuthor("admin");
        }
        String error = noticeService.add(notice);
        return error == null ? DataInfo.ok() : DataInfo.fail(error);
    }

    /**
     * 修改公告主题 / 内容（仅管理员）
     */
    @PutMapping("/{id}")
    public DataInfo<Void> update(@PathVariable Integer id, @RequestBody Notice notice, HttpSession session) {
        if (!isAdmin(session)) {
            return DataInfo.fail("仅管理员可修改公告");
        }
        String error = noticeService.update(id, notice);
        return error == null ? DataInfo.ok() : DataInfo.fail(error);
    }

    /**
     * 批量删除公告（仅管理员）
     */
    @DeleteMapping
    public DataInfo<Void> delete(@RequestBody List<Integer> ids, HttpSession session) {
        if (!isAdmin(session)) {
            return DataInfo.fail("仅管理员可删除公告");
        }
        if (ids == null || ids.isEmpty()) {
            return DataInfo.fail("请选择要删除的公告");
        }
        int rows = noticeService.deleteByIds(ids);
        return rows > 0 ? DataInfo.ok() : DataInfo.fail("删除失败");
    }
}
