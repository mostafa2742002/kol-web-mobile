package com.nano.soft.kol.bloger.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonapi.io.github.classgraph.json.Id;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "categories")
public class Category {

    @Id
    @Schema(hidden = true)
    private String id;

    @NotNull(message = "category name shouldn't be null")
    private String name;
    
}
