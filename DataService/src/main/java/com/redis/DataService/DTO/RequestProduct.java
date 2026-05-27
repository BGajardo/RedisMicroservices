package com.redis.DataService.DTO;

import lombok.Data;

@Data
public class RequestProduct {
    String description;
    String name;
    double price;
    Integer stock;
}
