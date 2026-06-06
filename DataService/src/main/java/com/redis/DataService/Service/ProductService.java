package com.redis.DataService.Service;

import com.redis.DataService.DTO.RequestProduct;
import com.redis.DataService.Entity.Product;
import com.redis.DataService.Exception.ProductNotFoundException;
import com.redis.DataService.Repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll(){
        log.info("Buscando todos los productos");
        List<Product> products = productRepository.findAll();
        log.info("Productos {} encontrados", products.size());
        return products;

    }

    public Product findById(Long id){
        log.info("Buscando producto con id {}", id);
        return productRepository.findById(id)
                .orElseThrow(() ->
                {
                    log.warn("Producto con id {} no encontrado", id);
                    return new ProductNotFoundException(id);
                });
    }

    public List<Product> findByName(String name){
        log.info("Buscando productos con nombre {}", name);
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        log.info("Productos {} encontrados con nombre {}", products.size(), name);
        return products;
    }

    public Product save(RequestProduct request){
        log.info("Creando Nuevo Producto {}", request.getName());
        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product saved =  productRepository.save(product);
        log.info("Producto creado con el id: {}", saved.getId());
        return saved;
    }

}
