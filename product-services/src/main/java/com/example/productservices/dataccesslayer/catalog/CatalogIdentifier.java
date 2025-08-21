package com.example.productservices.dataccesslayer.catalog;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class CatalogIdentifier {


    @Column(name = "catalog_id",nullable = false,unique = true)
    private String catalogId;

    public CatalogIdentifier() {
        this.catalogId  = UUID.randomUUID().toString();
    }


}
