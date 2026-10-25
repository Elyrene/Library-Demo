package com.Library.controller;

import com.Library.common.DataInfo;
import com.Library.entity.BookInfo;
import com.Library.mapper.BookInfoMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.autoconfigure.PageHelperProperties;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookInfoMapper bookInfoMapper;
    private final PageHelperProperties pageHelperProperties;

    public BookController(BookInfoMapper bookInfoMapper, PageHelperProperties pageHelperProperties) {
        this.bookInfoMapper = bookInfoMapper;
        this.pageHelperProperties = pageHelperProperties;
    }

    /**
     * 分页查询图书列表（支持按书名模糊搜索、分类、状态筛选）
     */
    @GetMapping
    public DataInfo<List<BookInfo>> list(@RequestParam(defaultValue = "1") int pageNum,
                                        @RequestParam(defaultValue = "10") int limit,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) Integer typeId,
                                        @RequestParam(required = false) Integer status) {
        PageHelper.startPage(pageNum, limit);
        List<BookInfo> list = bookInfoMapper.queryAll(name, typeId, status);
        PageInfo<BookInfo> pageInfo = PageInfo.of(list);
        return DataInfo.ok(list, pageInfo.getTotal());
    }

    /**
     * 根据 ID 查询图书
     */
    @GetMapping("/{id}")
    public DataInfo<BookInfo> getById(@PathVariable Integer id) {
        BookInfo book = bookInfoMapper.findById(id);
        if (book == null) {
            return DataInfo.fail("图书不存在");
        }
        return DataInfo.ok(book);
    }

    /**
     * 新增图书（status 默认 0 未借出）
     */
    @PostMapping
    public DataInfo<Void> add(@RequestBody BookInfo book) {
        if (book.getName() == null || book.getName().trim().isEmpty()) {
            return DataInfo.fail("图书名称不能为空");
        }
        if (book.getStatus() == null) {
            book.setStatus(0);
        }
        int rows = bookInfoMapper.insert(book);
        return rows > 0 ? DataInfo.ok() : DataInfo.fail("新增失败");
    }

    /**
     * 修改图书信息
     */
    @PutMapping("/{id}")
    public DataInfo<Void> update(@PathVariable Integer id, @RequestBody BookInfo book) {
        BookInfo exist = bookInfoMapper.findById(id);
        if (exist == null) {
            return DataInfo.fail("图书不存在");
        }
        // status（0未借出/1已借出）由借还业务流程管理，不允许通过通用编辑接口修改；
        // 用数据库现有值回填，避免前端未携带 status 时把状态覆盖为 NULL。
        book.setId(id);
        book.setStatus(exist.getStatus());
        int rows = bookInfoMapper.update(book);
        return rows > 0 ? DataInfo.ok() : DataInfo.fail("修改失败");
    }

    /**
     * 批量删除图书
     */
    @DeleteMapping
    public DataInfo<Void> delete(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return DataInfo.fail("请选择要删除的图书");
        }
        int rows = bookInfoMapper.deleteByIds(ids);
        return rows > 0 ? DataInfo.ok() : DataInfo.fail("删除失败");
    }
}
