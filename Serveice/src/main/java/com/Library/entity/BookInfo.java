package com.Library.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookInfo {
    private Integer id;
    private String name;
    private String author;
    private String publish;
    private String isbn;
    private String introduction;
    private String language;
    private Double price;
    private LocalDate publishDate;
    private Integer typeId;
    private Integer status;
}
