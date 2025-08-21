package com.example.orderservices.domainclientlayer;


import com.example.orderservices.presentationlayer.customerdtos.CustomerResponseModel;
import com.example.orderservices.utils.HttpErrorInfo;
import com.example.orderservices.utils.InvalidInputException;
import com.example.orderservices.utils.NotFoundException;
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
public class CustomerServiceClient {



    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String CUSTOMER_SERVICE_BASE_URL;

    private CustomerServiceClient(RestTemplate restTemplate,
                                  ObjectMapper objectMapper,
                                  @Value("${app.customer-services.host}") String customerServicesHost,
                                  @Value("${app.customer-services.port}") String customerServicesPort) {

        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.CUSTOMER_SERVICE_BASE_URL = "http://" +
                customerServicesHost+":"+customerServicesPort+"/api/v1/customers";
    }


//    public List<CustomerResponseModel> getCustomers() {
//        try {
//            return this.restTemplate.exchange(
//                    CUSTOMER_SERVICE_BASE_URL,
//                    HttpMethod.GET,
//                    null,
//                    new ParameterizedTypeReference<List<CustomerResponseModel>>() {}
//            ).getBody();
//        } catch (HttpClientErrorException e) {
//            throw handleHttpClientException(e);
//        }
//    }

    public CustomerResponseModel getCustomerbyCustomerId(String customerId) {
        try {
            return restTemplate.getForObject(
                    CUSTOMER_SERVICE_BASE_URL + "/" + customerId,
                    CustomerResponseModel.class
            );
        } catch (HttpClientErrorException.NotFound nf) {
            log.debug("Customer {} not found → returning null", customerId);
            return null;
        } catch (HttpClientErrorException e) {
            throw handleHttpClientException(e);
        }
    }

//    public CustomerResponseModel addCustomer(CustomerRequestModel newCustomerData) {
//
////        if(newCustomerData == null || newCustomerData == isEM) {
////            throw new IllegalArgumentException("New customer data cannot be null");
////        }
//
//        try {
//            return restTemplate.postForObject(
//                    CUSTOMER_SERVICE_BASE_URL,
//                    newCustomerData,
//                    CustomerResponseModel.class
//            );
//        }catch (HttpClientErrorException e) {
//            throw handleHttpClientException(e);
//        }
//    }

//    public CustomerResponseModel updateCustomer(String customerId, CustomerRequestModel newCustomerData) {
//
//        try {
//
//                    restTemplate.put(
//                            CUSTOMER_SERVICE_BASE_URL + "/" + customerId,
//                            newCustomerData
//                    );
//                    return getCustomerbyCustomerId(customerId);
//
//        }catch (HttpClientErrorException e) {
//            throw handleHttpClientException(e);
//        }
//    }
//
//    public String deleteCustomerbyCustomerId(String customerId) {
//
//        try{
//             restTemplate.delete(CUSTOMER_SERVICE_BASE_URL + "/" + customerId);
//             return "Customer deleted successfully. ";
//
//        }catch (HttpClientErrorException e) {
//            throw handleHttpClientException(e);
//        }
//    }
//
//    public CustomerResponseModel getCustomerbyEmail(String email) {
//
//        try{
//            return restTemplate.getForObject(
//                    CUSTOMER_SERVICE_BASE_URL + "?email=" + email,
//                    CustomerResponseModel.class
//            );
//        } catch (HttpClientErrorException e) {
//            throw handleHttpClientException(e);
//        }
//
//    }

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
