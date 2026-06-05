package com.redis.DataService.Exception;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(Long id) {
        super("Producto no encontrado: " + id);
    }
}
