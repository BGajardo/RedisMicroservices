package com.redis.DataService.Controller;

import com.redis.DataService.DTO.RequestProduct;
import com.redis.DataService.Entity.Product;
import com.redis.DataService.Service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts(){
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id){
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name){
        return ResponseEntity.ok(productService.findByName(name));
    }

    @PostMapping("/create")
    public ResponseEntity<Product> saveProduct(@RequestBody RequestProduct product){
        return ResponseEntity.ok(productService.save(product));
    }


}
