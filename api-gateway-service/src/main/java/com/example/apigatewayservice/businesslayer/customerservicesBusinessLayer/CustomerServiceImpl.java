package com.example.apigatewayservice.businesslayer.customerservicesBusinessLayer;

import com.example.apigatewayservice.domainclientlayer.CustomerServiceClient;
import com.example.apigatewayservice.presentationlayer.customersdtos.CustomerRequestModel;
import com.example.apigatewayservice.presentationlayer.customersdtos.CustomerResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerServiceClient customerServiceClient;

    public CustomerServiceImpl(CustomerServiceClient customerServiceClient) {
        this.customerServiceClient = customerServiceClient;
    }



    @Override
    public List<CustomerResponseModel> getCustomers() {

        return this.customerServiceClient.getCustomers();

    }

    @Override
    public CustomerResponseModel getCustomerbyCustomerId(String customer_id) {

        return this.customerServiceClient.getCustomerbyCustomerId(customer_id);

    }



    @Override
    public CustomerResponseModel addCustomer(CustomerRequestModel newCustomerData) {

        return this.customerServiceClient.addCustomer(newCustomerData);

    }

    @Override
    public CustomerResponseModel updateCustomer(String customerId, CustomerRequestModel newCustomerData) {

        return this.customerServiceClient.updateCustomer(customerId,newCustomerData);

    }

    @Override
    public String deleteCustomerbyCustomerId(String customerId) {

        return this.customerServiceClient.deleteCustomerbyCustomerId(customerId);

    }

    @Override
    public CustomerResponseModel getCustomerbyEmail(String email) {

        return this.customerServiceClient.getCustomerbyEmail(email);

    }

}
