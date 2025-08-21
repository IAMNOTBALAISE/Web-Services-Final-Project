package com.example.orderservices.dataaccesslayer;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CatalogIdentifier {

//    @Column(name = "catalog_id",nullable = false,unique = true)
    private String catalogId;

    public CatalogIdentifier() {
        this.catalogId  = UUID.randomUUID().toString();
    }
}
