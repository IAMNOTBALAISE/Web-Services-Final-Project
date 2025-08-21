package com.example.customerservices.dataaccesslayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class CustomerIdentifier {


    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;


    public CustomerIdentifier() {
        this.customerId = UUID.randomUUID().toString();
    }

}

