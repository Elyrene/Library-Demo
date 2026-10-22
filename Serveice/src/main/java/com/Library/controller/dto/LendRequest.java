package com.Library.controller.dto;

import lombok.Data;

@Data
public class LendRequest {
    private Integer bookId;
    private Integer readerId;
}
