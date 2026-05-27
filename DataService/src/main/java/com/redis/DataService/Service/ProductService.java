package com.redis.DataService.Service;

import com.redis.DataService.DTO.RequestProduct;
import com.redis.DataService.Entity.Product;
import com.redis.DataService.Repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll(){
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id){
        return productRepository.findById(id);
    }

    public List<Product> findByName(String name){
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public Product save(RequestProduct request){

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        return productRepository.save(product);
    }

}
