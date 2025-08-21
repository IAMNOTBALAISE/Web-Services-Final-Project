package com.example.serviceplanservices.businesslayer;

import com.example.serviceplanservices.presentationlayer.ServicePlanRequestModel;
import com.example.serviceplanservices.presentationlayer.ServicePlanResponseModel;
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
