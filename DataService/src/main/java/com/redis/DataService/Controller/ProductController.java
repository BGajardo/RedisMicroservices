package com.redis.DataService.Controller;

import com.redis.DataService.DTO.RequestProduct;
import com.redis.DataService.Entity.Product;
import com.redis.DataService.Service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Endpoints de gestion de productos")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Obtener todos los productos")
    @ApiResponse(responseCode = "200", description = "Lista de productos")
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts(){
        return ResponseEntity.ok(productService.findAll());
    }

    @Operation(summary = "Obtener producto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Operation(summary = "Buscar productos por nombre")
    @ApiResponse(responseCode = "200", description = "Lista de productos encontrados")
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name){
        return ResponseEntity.ok(productService.findByName(name));
    }

    @Operation(summary = "Crear un nuevo producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos Invalidos")
    })
    @PostMapping("/create")
    public ResponseEntity<Product> saveProduct(@RequestBody @Valid RequestProduct  product){
        return ResponseEntity.ok(productService.save(product));
    }


}
