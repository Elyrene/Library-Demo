package com.Library.service;

import com.Library.controller.dto.LendRequest;
import com.Library.entity.LendList;

import java.util.List;
import java.util.Map;

public interface LendService {

    /**
     * 分页查询借阅记录（支持按书名、读者姓名模糊搜索，按 backType 过滤）
     */
    List<LendList> list(String bookName, String readerName, Integer backType);

    /**
     * 查询单条借阅记录详情
     */
    LendList getById(Integer id);

    /**
     * 借书：校验书籍存在且未借出、读者存在，写入借阅记录，并把书标记为已借出。
     * 返回 null 表示成功，否则返回错误信息。
     */
    String lend(LendRequest req);

    /**
     * 还书：回填 backDate / backType / 备注，并把对应书籍标记为未借出。
     * 返回 null 表示成功，否则返回错误信息。
     */
    String back(Integer id, Map<String, Object> body);

    /**
     * 查询某读者的借阅历史
     */
    List<LendList> listByReader(Integer readerId);

    /**
     * 查询某图书的借阅记录
     */
    List<LendList> listByBook(Integer bookId);

    /**
     * 批量删除借阅记录，返回影响行数
     */
    int deleteByIds(List<Integer> ids);
}
