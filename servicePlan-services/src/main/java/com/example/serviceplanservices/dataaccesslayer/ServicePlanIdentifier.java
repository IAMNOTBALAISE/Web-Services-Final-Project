package com.example.serviceplanservices.dataaccesslayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class ServicePlanIdentifier {

    @Column(name = "plan_id",nullable = false,unique = true)
    private String planId;

    public ServicePlanIdentifier() {
        this.planId = UUID.randomUUID().toString();
    }

}
