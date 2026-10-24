package com.Library.service.impl;

import com.Library.controller.dto.LendRequest;
import com.Library.entity.BookInfo;
import com.Library.entity.LendList;
import com.Library.entity.ReaderInfo;
import com.Library.mapper.BookInfoMapper;
import com.Library.mapper.LendListMapper;
import com.Library.mapper.ReaderInfoMapper;
import com.Library.service.LendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class LendServiceImpl implements LendService {

    private final LendListMapper lendListMapper;
    private final BookInfoMapper bookInfoMapper;
    private final ReaderInfoMapper readerInfoMapper;

    public LendServiceImpl(LendListMapper lendListMapper,
                           BookInfoMapper bookInfoMapper,
                           ReaderInfoMapper readerInfoMapper) {
        this.lendListMapper = lendListMapper;
        this.bookInfoMapper = bookInfoMapper;
        this.readerInfoMapper = readerInfoMapper;
    }

    @Override
    public List<LendList> list(String bookName, String readerName, Integer backType) {
        return lendListMapper.queryAll(bookName, readerName, backType);
    }

    @Override
    public LendList getById(Integer id) {
        return lendListMapper.findById(id);
    }

    @Override
    @Transactional
    public String lend(LendRequest req) {
        if (req.getBookId() == null || req.getReaderId() == null) {
            return "bookId 和 readerId 不能为空";
        }
        BookInfo book = bookInfoMapper.findById(req.getBookId());
        if (book == null) {
            return "图书不存在";
        }
        if (book.getStatus() != null && book.getStatus() == 1) {
            return "该书已被借出，无法重复借阅";
        }
        ReaderInfo reader = readerInfoMapper.findById(req.getReaderId());
        if (reader == null) {
            return "读者不存在";
        }

        LendList lend = new LendList();
        lend.setBookId(req.getBookId());
        lend.setReaderId(req.getReaderId());
        lend.setLendDate(new Date());
        int rows = lendListMapper.insert(lend);
        if (rows <= 0) {
            return "借书失败";
        }
        book.setStatus(1);
        bookInfoMapper.updateStatus(book);
        return null;
    }

    @Override
    @Transactional
    public String back(Integer id, Map<String, Object> body) {
        LendList lend = lendListMapper.findById(id);
        if (lend == null) {
            return "借阅记录不存在";
        }
        if (lend.getBackDate() != null) {
            return "该记录已归还，请勿重复操作";
        }
        Object backTypeObj = body.get("backType");
        Integer backType = backTypeObj == null ? null : Integer.valueOf(String.valueOf(backTypeObj));
        String exceptRemarks = body.get("exceptRemarks") == null ? null : String.valueOf(body.get("exceptRemarks"));

        int rows = lendListMapper.updateBack(id, new Date(), backType, exceptRemarks);
        if (rows <= 0) {
            return "还书失败";
        }
        bookInfoMapper.updateStatusById(lend.getBookId(), 0);
        return null;
    }

    @Override
    public List<LendList> listByReader(Integer readerId) {
        return lendListMapper.findByReaderId(readerId);
    }

    @Override
    public List<LendList> listByBook(Integer bookId) {
        return lendListMapper.findByBookId(bookId);
    }

    @Override
    public int deleteByIds(List<Integer> ids) {
        return lendListMapper.deleteByIds(ids);
    }
}
