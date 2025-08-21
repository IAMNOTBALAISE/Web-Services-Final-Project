package com.example.serviceplanservices.datamapperlayer;


import com.example.serviceplanservices.dataaccesslayer.ServicePlan;
import com.example.serviceplanservices.presentationlayer.ServicePlanRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServicePlanRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "servicePlanIdentifier", ignore = true)
    ServicePlan requestModelToEntity(ServicePlanRequestModel servicePlanRequestModel);

}
