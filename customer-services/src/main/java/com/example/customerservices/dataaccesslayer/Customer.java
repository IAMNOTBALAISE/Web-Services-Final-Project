package com.example.customerservices.dataaccesslayer;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Entity
@Table(name="customers")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;    //private identifier

    @Embedded
    private CustomerIdentifier customerIdentifier;

    private String lastName;
    private String firstName;
    private String emailAddress;
    private String username;
    private String password;
    @Embedded
    private Address customerAddress;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "customer_phonenumbers",
            joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    )
    private List<PhoneNumber> phoneNumbers;



}

