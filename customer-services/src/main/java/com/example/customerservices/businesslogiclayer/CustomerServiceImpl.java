package com.example.customerservices.businesslogiclayer;




import com.example.customerservices.dataaccesslayer.Customer;
import com.example.customerservices.dataaccesslayer.CustomerIdentifier;
import com.example.customerservices.dataaccesslayer.CustomerRepository;
import com.example.customerservices.datamapperlayer.CustomerRequestMapper;
import com.example.customerservices.datamapperlayer.CustomerResponseMapper;
import com.example.customerservices.presentationlayer.CustomerRequestModel;
import com.example.customerservices.presentationlayer.CustomerResponseModel;
import com.example.customerservices.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerResponseMapper customerResponseMapper;
    private final CustomerRequestMapper customerRequestMapper;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository,
                               CustomerResponseMapper customerResponseMapper,
                               CustomerRequestMapper customerRequestMapper) {
        this.customerRepository = customerRepository;
        this.customerResponseMapper = customerResponseMapper;
        this.customerRequestMapper = customerRequestMapper;
    }

    @Override
    public List<CustomerResponseModel> getCustomers() {
        List<Customer> customers = this.customerRepository.findAll();
        customers.get(0).getCustomerAddress().getStreetAddress();
        return this.customerResponseMapper.entityListToResponseModelList(customers);
    }

    @Override
    public CustomerResponseModel getCustomerbyCustomerId(String customer_id) {
        Customer customer = this.customerRepository.findCustomerByCustomerIdentifier_CustomerId(customer_id);
        if (customer == null) {
            throw new NotFoundException("Customer with " + customer_id + " not found.");
        }
        return this.customerResponseMapper.entityToResponseModel(customer);
    }



    @Override
    public CustomerResponseModel addCustomer(CustomerRequestModel newCustomerData) {
        String pw1 = newCustomerData.getPassword1();
        String pw2 = newCustomerData.getPassword2();
        if (pw1 == null) pw1 = "";
        if (pw2 == null) pw2 = "";
        if (!pw1.equals(pw2)) {
            throw new IllegalArgumentException("Entered passwords do not match!");
        }


        CustomerIdentifier customerIdentifier = new CustomerIdentifier(newCustomerData.getCustomerId());


        Customer foundCustomer = this.customerRepository.findCustomerByCustomerIdentifier_CustomerId(customerIdentifier.getCustomerId());
        if (foundCustomer != null) {
            throw new IllegalArgumentException("Customer with customer id: " + customerIdentifier.getCustomerId() + " is already in repository. Choose another Customer Identifier.");
        }


        Customer customer = this.customerRequestMapper.requestModelToEntity(newCustomerData);

        // Set the CustomerIdentifier and password
        customer.setCustomerIdentifier(customerIdentifier);
        customer.setPassword(newCustomerData.getPassword1());


        Customer savedCustomer = this.customerRepository.save(customer);


        return this.customerResponseMapper.entityToResponseModel(savedCustomer);
    }


    @Override
    public CustomerResponseModel updateCustomer(String customerId, CustomerRequestModel newCustomerData) {
        if (customerId == null || customerId.isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty.");
        }


        Customer foundCustomer = this.customerRepository.findCustomerByCustomerIdentifier_CustomerId(customerId);
        if (foundCustomer == null) {
            throw new NotFoundException("Customer with id: " + customerId + " not found in repository.");
        }
        String pw1 = newCustomerData.getPassword1();
        String pw2 = newCustomerData.getPassword2();
        if (pw1 == null) pw1 = "";
        if (pw2 == null) pw2 = "";
        if (!pw1.equals(pw2)) {
            throw new IllegalArgumentException("Entered passwords do not match!");
        }
        Customer customer = this.customerRequestMapper.requestModelToEntity(newCustomerData);


        customer.setCustomerIdentifier(foundCustomer.getCustomerIdentifier());
        customer.setId(foundCustomer.getId());


        customer.setPassword(newCustomerData.getPassword1());


        Customer savedCustomer = this.customerRepository.save(customer);


        return this.customerResponseMapper.entityToResponseModel(savedCustomer);
    }

    @Override
    public String deleteCustomerbyCustomerId(String customerId) {
        Customer foundCustomer = this.customerRepository.findCustomerByCustomerIdentifier_CustomerId(customerId);
        if (foundCustomer == null) {
            throw new NotFoundException("Customer with id: " + customerId + " not found in repository.");
        }
        this.customerRepository.delete(foundCustomer);
        return "Customer with id: " + customerId + " deleted successfully.";
    }

    @Override
    public CustomerResponseModel getCustomerbyEmail(String email) {
        Customer custemail = customerRepository.findCustomerByEmailAddress(email);

        if (custemail == null) {
            throw new NotFoundException("Customer with email: " + email + " not found in repository.");

        }else
            return this.customerResponseMapper.entityToResponseModel(custemail);
    }


    // Helper methods
    public CustomerResponseModel fromEntityToModel(Customer customer){
        CustomerResponseModel customerResponseModel =
                new CustomerResponseModel();
        customerResponseModel.setFirstName(customer.getFirstName());
        customerResponseModel.setLastName(customer.getLastName());
        return customerResponseModel;
    }
    public List<CustomerResponseModel> fromEntityListToModelList(
            List<Customer> customers){
        List<CustomerResponseModel> customerResponseModels =
                new ArrayList<>();
        for (Customer c : customers) {
            customerResponseModels.add(fromEntityToModel(c));
        }
        return customerResponseModels;
    }
}