package com.example.apigatewayservice.businesslayer.servicePlanservicesBusinessLayer;



import com.example.apigatewayservice.domainclientlayer.servicePlanServiceClient;
import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanRequestModel;
import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicePlanServiceImpl implements ServicePlanService {


    private final servicePlanServiceClient servicePlanServiceClient;


    @Autowired
    public ServicePlanServiceImpl(servicePlanServiceClient servicePlanServiceClient) {
        this.servicePlanServiceClient = servicePlanServiceClient;
    }


    @Override
    public List<ServicePlanResponseModel> getServicePlans() {

        return this.servicePlanServiceClient.getServicePlans();
    }

    @Override
    public ServicePlanResponseModel getServicePlansById(String planId) {

        return this.servicePlanServiceClient.getServicePlansById(planId);
    }

    @Override
    public ServicePlanResponseModel addServicePlan(ServicePlanRequestModel servicePlanRequestModel) {

        return this.servicePlanServiceClient.addServicePlan(servicePlanRequestModel);
    }

    @Override
    public ServicePlanResponseModel updateServicePlan(String planId, ServicePlanRequestModel servicePlanRequestModel) {

        return this.servicePlanServiceClient.updateServicePlan(planId,servicePlanRequestModel);
    }

    @Override
    public String deleteServicePlanById(String planId) {

        return this.servicePlanServiceClient.deleteServicePlanById(planId);
    }

}