package com.example.orderservices.dataaccesslayer;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class CustomerIdentifier {

//    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;


    public CustomerIdentifier() {
        this.customerId = UUID.randomUUID().toString();
    }
}
