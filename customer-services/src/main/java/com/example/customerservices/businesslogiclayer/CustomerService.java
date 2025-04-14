package com.example.customerservices.businesslogiclayer;



import com.example.customerservices.presentationlayer.CustomerRequestModel;
import com.example.customerservices.presentationlayer.CustomerResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerService {

    List<CustomerResponseModel> getCustomers();
    CustomerResponseModel getCustomerbyCustomerId(String customer_id);

    CustomerResponseModel addCustomer(CustomerRequestModel newCustomerData);

    CustomerResponseModel updateCustomer(String customerId, CustomerRequestModel newCustomerData);

    String deleteCustomerbyCustomerId(String customerId);

    CustomerResponseModel getCustomerbyEmail(String email);
}
