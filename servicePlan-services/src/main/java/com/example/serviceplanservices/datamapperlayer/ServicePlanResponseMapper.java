package com.example.serviceplanservices.datamapperlayer;


import com.example.serviceplanservices.dataaccesslayer.ServicePlan;
import com.example.serviceplanservices.presentationlayer.ServicePlanResponseModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServicePlanResponseMapper {

    @Mapping(expression = "java(servicePlan.getServicePlanIdentifier().getPlanId())", target = "planId")
    ServicePlanResponseModel entityToResponseModel(ServicePlan servicePlan);

    List<ServicePlanResponseModel> entityListToResponseModelList(List<ServicePlan> servicePlan);
}
