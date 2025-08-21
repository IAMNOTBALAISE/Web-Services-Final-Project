package com.example.productservices.dataccesslayer.watch;

import com.example.productservices.dataccesslayer.catalog.CatalogIdentifier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Entity
@Table(name="watches")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private WatchIdentifier watchIdentifier;

    @Embedded
    private CatalogIdentifier catalogIdentifier;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "watch_status")
//    private WatchStatus watchStatus;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private UsageType usageType;

    private String model;
    private String material;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "watch_accessories",joinColumns =
    @JoinColumn(name = "watch_id",referencedColumnName = "watch_id"))
    private List<Accessory> accessories;

    @Embedded
    private WatchBrand watchBrand;

    @Embedded
    private Price price;


}
