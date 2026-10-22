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
}
