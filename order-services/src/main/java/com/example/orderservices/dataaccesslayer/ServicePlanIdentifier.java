package com.example.orderservices.dataaccesslayer;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ServicePlanIdentifier {

//    @Column(name = "plan_id",nullable = false,unique = true)
    private String planId;

    public ServicePlanIdentifier() {
        this.planId = UUID.randomUUID().toString();
    }

}
