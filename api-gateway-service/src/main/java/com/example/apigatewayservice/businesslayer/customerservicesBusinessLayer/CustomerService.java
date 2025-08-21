package com.example.apigatewayservice.businesslayer.customerservicesBusinessLayer;

import com.example.apigatewayservice.presentationlayer.customersdtos.CustomerRequestModel;
import com.example.apigatewayservice.presentationlayer.customersdtos.CustomerResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerService {

    List<CustomerResponseModel> getCustomers();

    CustomerResponseModel getCustomerbyCustomerId(String customerId);

    CustomerResponseModel getCustomerbyEmail(String email);

    CustomerResponseModel addCustomer(CustomerRequestModel newCustomerData);

    CustomerResponseModel updateCustomer(String customerId, CustomerRequestModel newCustomerData);

    String deleteCustomerbyCustomerId(String customerId);
}
