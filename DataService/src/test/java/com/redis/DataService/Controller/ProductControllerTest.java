package com.redis.DataService.Controller;

import com.redis.DataService.DTO.RequestProduct;
import com.redis.DataService.Entity.Product;
import com.redis.DataService.Exception.ProductNotFoundException;
import com.redis.DataService.Service.JwtService;
import com.redis.DataService.Service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp(){
        product = new Product();
        product.setId(1L);
        product.setName("Notebook");
        product.setDescription("Notebook Gamer");
        product.setPrice(100000.00);
        product.setStock(10);
    }

    @Test
    @WithMockUser
    void getAllProducts_shouldReturn200_withProductList() throws Exception{

        when(productService.findAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Notebook"))
                .andExpect(jsonPath("$[0].price").value(100000.00));
    }

    @Test
    @WithMockUser
    void getAllProducts_shouldReturn200_withEmptyList() throws Exception{
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/products/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser
    void getProductById_shouldReturn200_withProduct() throws Exception{
        when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Notebook"));
    }

    @Test
    @WithMockUser
    void getProductById_shouldReturn404_whenProductNotFound() throws Exception{
        when(productService.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser
    void searchProducts_shouldReturn200_withMatchingProducts() throws Exception{
        when(productService.findByName("Notebook")).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products/search").param("name", "Notebook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Notebook"));
    }

    @Test
    @WithMockUser
    void searchProducts_shouldReturn200_withEmptyList_whenNoProductsMatch() throws Exception {
        when(productService.findByName("noexiste")).thenReturn(List.of());

        mockMvc.perform(get("/api/products/search").param("name", "noexiste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    @WithMockUser
    void saveProduct_shouldReturn200_whenProductIsValid() throws Exception{
        RequestProduct req = new RequestProduct();
        req.setName("Notebook");
        req.setDescription("Notebook Gamer");
        req.setPrice(100000.00);
        req.setStock(10);

        when(productService.save(req)).thenReturn(product);

        mockMvc.perform(post("/api/products/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook"));
    }

    @Test
    @WithMockUser
    void saveProduct_shouldReturn400_whenProductIsInvalid() throws Exception{
        RequestProduct req = new RequestProduct();
        req.setName("");
        req.setDescription("");
        req.setPrice(-100000.00);
        req.setStock(-10);

        mockMvc.perform(post("/api/products/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }





}
