package com.Library.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Notice {
    private Integer id;
    private String topic;
    private String content;
    private String author;
    private Date createDate;
}
