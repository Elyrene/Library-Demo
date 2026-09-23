package com.Library.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReaderInfo {
    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String sex;
    private LocalDate birthday;
    private String address;
    private String tel;
    private String email;
    private LocalDateTime registerDate;
    private String readerNumber;
}
