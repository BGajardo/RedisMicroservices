package com.redis.DataService.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestProduct {
    @NotBlank(message = "El Nombre no puede estar vacio")
    @Size(min = 2, max = 100, message = "El Nombre debe tener entre 2 y 100 caracteres")
    String name;
    @NotBlank(message = "La Descripcion no puede estar vacia")
    @Size(min = 2, max = 255, message = "La Descripcion debe tener entre 2 y 255 caracteres")
    String description;
    @NotNull(message = "El Precio no puede estar nulo")
    @Positive(message = "El Precio debe ser positivo")
    double price;
    @NotNull(message = "El Stock no puede estar nulo")
    @Positive(message = "El Stock debe ser positivo")
    Integer stock;
}
