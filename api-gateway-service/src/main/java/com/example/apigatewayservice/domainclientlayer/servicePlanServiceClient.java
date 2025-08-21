package com.example.apigatewayservice.domainclientlayer;


import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanRequestModel;
import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import com.example.apigatewayservice.utils.HttpErrorInfo;
import com.example.apigatewayservice.utils.InvalidInputException;
import com.example.apigatewayservice.utils.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class servicePlanServiceClient {


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String PLAN_BASE_URL;


    public servicePlanServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper,
                                    @Value("${app.service-plan-services.host}") String servicePlanServicesHost,
                                    @Value("${app.service-plan-services.port}") String servicePlanServicesPort) {

        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        this.PLAN_BASE_URL = "http://" + servicePlanServicesHost + ":" + servicePlanServicesPort + "/api/v1/plans";
    }




    public List<ServicePlanResponseModel> getServicePlans() {

        try {
            return restTemplate.exchange(
                    PLAN_BASE_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ServicePlanResponseModel>>() {}
            ).getBody();
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }

    }


    public ServicePlanResponseModel getServicePlansById(String planId) {
        try {
            return restTemplate.getForObject(
                    PLAN_BASE_URL + "/" + planId,
                    ServicePlanResponseModel.class
            );
        }
        catch (HttpClientErrorException.NotFound nf) {
            log.debug("Plan {} not found → returning null", planId);
            return null;
        }
        catch (HttpClientErrorException ce) {
            throw handleHttpClientException(ce);
        }
    }

    public ServicePlanResponseModel addServicePlan(ServicePlanRequestModel servicePlanRequestModel) {

        try {
            return restTemplate.postForObject(
                    PLAN_BASE_URL,
                    servicePlanRequestModel,
                    ServicePlanResponseModel.class
            );
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    public ServicePlanResponseModel updateServicePlan(String planId, ServicePlanRequestModel servicePlanRequestModel) {

        try {
            restTemplate.put(
                    PLAN_BASE_URL + "/" + planId,
                    servicePlanRequestModel
            );
            // after PUT, re-fetch the updated resource
            return getServicePlansById(planId);
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    public String deleteServicePlanById(String planId) {

        try {
            restTemplate.delete(PLAN_BASE_URL + "/" + planId);
            return "Service plan deleted successfully.";
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }catch (IOException ioex){
            return ioex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex){

        HttpStatus statusCode = (HttpStatus) ex.getStatusCode();
        String errorMessage = getErrorMessage(ex);

        if (statusCode == HttpStatus.UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(errorMessage);
        } else if (statusCode == HttpStatus.NOT_FOUND) {
            return new NotFoundException(errorMessage);
        } else if (statusCode == HttpStatus.BAD_REQUEST ||
                statusCode == HttpStatus.CONFLICT) {
            // Handle DuplicateCustomerEmailException or other validation errors
            return new InvalidInputException(errorMessage);
        }

        // Log the unexpected error
        log.warn("Unexpected HTTP error: {}. Error body: {}", statusCode, ex.getResponseBodyAsString());
        return new InvalidInputException(errorMessage); // Convert to InvalidInputException instead of returning ex directly

    }
}
