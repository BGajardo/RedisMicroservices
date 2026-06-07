package com.redis.DataService.Service;

import com.redis.DataService.DTO.RequestProduct;
import com.redis.DataService.Entity.Product;
import com.redis.DataService.Exception.ProductNotFoundException;
import com.redis.DataService.Repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Notebook");
        product.setDescription("Notebook Gamer");
        product.setPrice(800000.00);
        product.setStock(10);
    }


    @Test
    void findAll_shouldReturnAllProducts(){
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<Product> results = productService.findAll();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Notebook", results.getFirst().getName());
    }


    @Test
    void findAll_shouldReturnEmptyList_whenNoProducts(){
        when(productRepository.findAll()).thenReturn(List.of());

        List<Product> products = productService.findAll();

        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    void findById_shouldReturnProduct_whenProductExists(){
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Notebook", result.getName());
    }


    @Test
    void findById_shouldThrowProductNotFoundException_whenProductNotFound(){
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(99L));
    }

    @Test
    void findByName_shouldReturnProduct_whenProductExists(){
        when(productRepository.findByNameContainingIgnoreCase("Notebook")).thenReturn(List.of(product));

        List<Product> results = productService.findByName("Notebook");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Notebook", results.getFirst().getName());
    }

    @Test
    void findByName_shouldReturnEmptyList_whenProductNotMatch(){
        when(productRepository.findByNameContainingIgnoreCase("Producto Lala")).thenReturn(List.of());

        List <Product> products = productService.findByName("Producto Lala");

        assertNotNull(products);
        assertTrue(products.isEmpty());
    }

    @Test
    void save_shouldSaveProduct(){
        RequestProduct req = new RequestProduct();
        req.setName("Notebook");
        req.setDescription("Notebook Gamer");
        req.setPrice(800000.00);
        req.setStock(10);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.save(req);

        assertNotNull(result);
        assertEquals("Notebook", result.getName());
        verify(productRepository, times(1)).save(any(Product.class));

    }




}
