package com.Library.entity;

import lombok.Data;

import java.util.Date;

@Data
public class LendList {
    private Integer id;
    private Integer bookId;
    private Integer readerId;
    private Date lendDate;
    private Date backDate;
    private Integer backType;
    private String exceptRemarks;

    // 关联展示字段（JOIN 查询时填充）
    private String bookName;
    private String bookAuthor;
    private String readerRealName;
    private String readerNumber;

}
