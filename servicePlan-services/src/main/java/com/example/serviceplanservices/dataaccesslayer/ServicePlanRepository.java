package com.example.serviceplanservices.dataaccesslayer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicePlanRepository extends JpaRepository<ServicePlan, Integer> {

    ServicePlan findByServicePlanIdentifier_PlanId(String servicePlanIdentifier);

    boolean existsByCoverageDetails(String coverageDetails);


}
