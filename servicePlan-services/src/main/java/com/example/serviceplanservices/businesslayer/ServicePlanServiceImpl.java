package com.example.serviceplanservices.businesslayer;


import com.example.serviceplanservices.dataaccesslayer.ServicePlan;
import com.example.serviceplanservices.dataaccesslayer.ServicePlanIdentifier;
import com.example.serviceplanservices.dataaccesslayer.ServicePlanRepository;
import com.example.serviceplanservices.datamapperlayer.ServicePlanRequestMapper;
import com.example.serviceplanservices.datamapperlayer.ServicePlanResponseMapper;
import com.example.serviceplanservices.presentationlayer.ServicePlanRequestModel;
import com.example.serviceplanservices.presentationlayer.ServicePlanResponseModel;
import com.example.serviceplanservices.utils.exceptions.DuplicateCoverageDetailsException;
import com.example.serviceplanservices.utils.exceptions.InvalidInputException;
import com.example.serviceplanservices.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicePlanServiceImpl implements ServicePlanService {

    private final ServicePlanRepository servicePlanRepository;
    private final ServicePlanResponseMapper servicePlanResponseMapper;
    private final ServicePlanRequestMapper servicePlanRequestMapper;

    @Autowired
    public ServicePlanServiceImpl(ServicePlanRepository servicePlanRepository,
                                  ServicePlanResponseMapper servicePlanResponseMapper,
                                  ServicePlanRequestMapper servicePlanRequestMapper) {
        this.servicePlanRepository = servicePlanRepository;
        this.servicePlanResponseMapper = servicePlanResponseMapper;
        this.servicePlanRequestMapper = servicePlanRequestMapper;
    }

    @Override
    public List<ServicePlanResponseModel> getServicePlans() {
        List<ServicePlan> servicePlans = servicePlanRepository.findAll();
        return servicePlanResponseMapper.entityListToResponseModelList(servicePlans);
    }

    @Override
    public ServicePlanResponseModel getServicePlansById(String planId) {
        ServicePlan servicePlan = this.servicePlanRepository.findByServicePlanIdentifier_PlanId(planId);
        if (servicePlan == null) {
            throw new NotFoundException("Service Plan with ID: " + planId + " not found.");
        }
        return servicePlanResponseMapper.entityToResponseModel(servicePlan);
    }

    @Override
    public ServicePlanResponseModel addServicePlan(ServicePlanRequestModel servicePlanRequestModel) {

        if (servicePlanRepository.existsByCoverageDetails(servicePlanRequestModel.getCoverageDetails())) {
            throw new DuplicateCoverageDetailsException(servicePlanRequestModel.getCoverageDetails());
        }


        ServicePlan servicePlan = servicePlanRequestMapper
                .requestModelToEntity(servicePlanRequestModel);


        servicePlan.setServicePlanIdentifier(new ServicePlanIdentifier());

        ServicePlan savedPlan = servicePlanRepository.save(servicePlan);
        return servicePlanResponseMapper.entityToResponseModel(savedPlan);
    }

    @Override
    public ServicePlanResponseModel updateServicePlan(String planId, ServicePlanRequestModel servicePlanRequestModel) {
        ServicePlan existingPlan = servicePlanRepository
                .findByServicePlanIdentifier_PlanId(planId);
        if (existingPlan == null) {
            throw new NotFoundException("Service Plan with ID: " + planId + " not found.");
        }

        String newCoverage = servicePlanRequestModel.getCoverageDetails();
        if (!existingPlan.getCoverageDetails().equals(newCoverage)
                && servicePlanRepository.existsByCoverageDetails(newCoverage)) {
            throw new DuplicateCoverageDetailsException(newCoverage);
        }


        existingPlan.setCoverageDetails(newCoverage);
        existingPlan.setExpirationDate(servicePlanRequestModel.getExpirationDate());

        ServicePlan updatedPlan = servicePlanRepository.save(existingPlan);
        return servicePlanResponseMapper.entityToResponseModel(updatedPlan);
    }

    @Override
    public String deleteServicePlanById(String planId) {
        ServicePlan existingPlan = servicePlanRepository.findByServicePlanIdentifier_PlanId(planId);
        if (existingPlan == null) {
            throw new NotFoundException("Service Plan with ID: " + planId + " not found.");
        }

        servicePlanRepository.delete(existingPlan);
        return "Service Plan with ID " + planId + " deleted successfully.";
    }
}
