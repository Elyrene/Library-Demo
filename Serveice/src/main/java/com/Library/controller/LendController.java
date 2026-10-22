package com.Library.controller;

import com.Library.common.DataInfo;
import com.Library.controller.dto.LendRequest;
import com.Library.entity.LendList;
import com.Library.service.LendService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.autoconfigure.PageHelperProperties;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lends")
public class LendController {

    private final LendService lendService;
    private final PageHelperProperties pageHelperProperties;

    public LendController(LendService lendService,
                          PageHelperProperties pageHelperProperties) {
        this.lendService = lendService;
        this.pageHelperProperties = pageHelperProperties;
    }

    /**
     * 分页查询借阅记录（支持按书名、读者姓名模糊搜索，按 backType 过滤）
     */
    @GetMapping
    public DataInfo<List<LendList>> list(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int limit,
                                         @RequestParam(required = false) String bookName,
                                         @RequestParam(required = false) String readerName,
                                         @RequestParam(required = false) Integer backType) {
        PageHelper.startPage(pageNum, limit);
        List<LendList> list = lendService.list(bookName, readerName, backType);
        PageInfo<LendList> pageInfo = PageInfo.of(list);
        return DataInfo.ok(list, pageInfo.getTotal());
    }

    /**
     * 查询单条借阅记录详情
     */
    @GetMapping("/{id}")
    public DataInfo<LendList> getById(@PathVariable Integer id) {
        LendList lend = lendService.getById(id);
        if (lend == null) {
            return DataInfo.fail("借阅记录不存在");
        }
        return DataInfo.ok(lend);
    }

    /**
     * 借书：校验书籍存在且未借出、读者存在，写入借阅记录，并把书标记为已借出
     */
    @PostMapping
    public DataInfo<Void> lend(@RequestBody LendRequest req) {
        String error = lendService.lend(req);
        return error == null ? DataInfo.ok() : DataInfo.fail(error);
    }

    /**
     * 还书：回填 backDate / backType / 备注，并把对应书籍标记为未借出
     */
    @PutMapping("/{id}/back")
    public DataInfo<Void> back(@PathVariable Integer id,
                              @RequestBody Map<String, Object> body) {
        String error = lendService.back(id, body);
        return error == null ? DataInfo.ok() : DataInfo.fail(error);
    }

    /**
     * 查询某读者的借阅历史
     */
    @GetMapping("/reader/{readerId}")
    public DataInfo<List<LendList>> listByReader(@PathVariable Integer readerId) {
        List<LendList> list = lendService.listByReader(readerId);
        return DataInfo.ok(list, list.size());
    }

    /**
     * 查询某图书的借阅记录
     */
    @GetMapping("/book/{bookId}")
    public DataInfo<List<LendList>> listByBook(@PathVariable Integer bookId) {
        List<LendList> list = lendService.listByBook(bookId);
        return DataInfo.ok(list, list.size());
    }

    /**
     * 批量删除借阅记录
     */
    @DeleteMapping
    public DataInfo<Void> delete(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return DataInfo.fail("请选择要删除的借阅记录");
        }
        int rows = lendService.deleteByIds(ids);
        return rows > 0 ? DataInfo.ok() : DataInfo.fail("删除失败");
    }
}
