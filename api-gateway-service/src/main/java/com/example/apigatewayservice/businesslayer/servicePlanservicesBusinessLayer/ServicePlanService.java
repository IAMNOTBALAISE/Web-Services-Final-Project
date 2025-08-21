package com.example.apigatewayservice.businesslayer.servicePlanservicesBusinessLayer;


import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanRequestModel;
import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ServicePlanService {
    
    
    List<ServicePlanResponseModel> getServicePlans();

    ServicePlanResponseModel getServicePlansById(String planId);

    ServicePlanResponseModel addServicePlan(ServicePlanRequestModel servicePlanRequestModel);

    ServicePlanResponseModel updateServicePlan(String planId, ServicePlanRequestModel servicePlanRequestModel);

    String deleteServicePlanById(String planId);
}
