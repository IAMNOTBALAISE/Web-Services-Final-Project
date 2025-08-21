package com.example.productservices.dataccesslayer.catalog;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "catalogs")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catalog {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Embedded
    private CatalogIdentifier catalogIdentifier;

    private String type;

    private String description;


}
