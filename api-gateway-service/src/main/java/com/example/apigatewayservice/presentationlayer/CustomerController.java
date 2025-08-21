package com.example.apigatewayservice.presentationlayer;


import com.example.apigatewayservice.businesslayer.customerservicesBusinessLayer.CustomerService;
import com.example.apigatewayservice.presentationlayer.customersdtos.CustomerRequestModel;
import com.example.apigatewayservice.presentationlayer.customersdtos.CustomerResponseModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("api/v1/customers")
public class CustomerController {


    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping()
    public ResponseEntity<List<CustomerResponseModel>> getCustomers() {
        List<CustomerResponseModel> list = customerService.getCustomers();
        list.forEach(c -> {
            c.add(linkTo(methodOn(CustomerController.class)
                    .getCustomerById(c.getCustomerId())).withSelfRel());
            c.add(linkTo(methodOn(CustomerController.class)
                    .getCustomers()).withRel("all-customers"));
        });
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{customer_id}")
    public ResponseEntity<CustomerResponseModel> getCustomerById(@PathVariable String customer_id) {
        CustomerResponseModel c = customerService.getCustomerbyCustomerId(customer_id);
        c.add(linkTo(methodOn(CustomerController.class)
                .getCustomerById(customer_id)).withSelfRel());
        c.add(linkTo(methodOn(CustomerController.class)
                .getCustomers()).withRel("all-customers"));
        return ResponseEntity.ok(c);
    }

    @GetMapping(params = "email")
    public ResponseEntity<CustomerResponseModel> getCustomerByEmail(@RequestParam String email) {

        CustomerResponseModel c = customerService.getCustomerbyEmail(email);
        c.add(linkTo(methodOn(CustomerController.class)
                .getCustomerByEmail(email)).withSelfRel());
        c.add(linkTo(methodOn(CustomerController.class)
                .getCustomers()).withRel("all-customers"));
        return ResponseEntity.ok(c);
    }

    @PostMapping()
    public ResponseEntity<CustomerResponseModel> addCustomer(@RequestBody @Valid CustomerRequestModel newCustomerData) {

        CustomerResponseModel c = customerService.addCustomer(newCustomerData);
        c.add(linkTo(methodOn(CustomerController.class)
                .getCustomerById(c.getCustomerId())).withSelfRel());
        c.add(linkTo(methodOn(CustomerController.class)
                .getCustomers()).withRel("all-customers"));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(c);
    }

    @PutMapping("/{customer_id}")
    public ResponseEntity<CustomerResponseModel> updateCustomer(
            @PathVariable String customer_id,
            @RequestBody CustomerRequestModel newCustomerData) {

        CustomerResponseModel c = customerService.updateCustomer(customer_id, newCustomerData);
        c.add(linkTo(methodOn(CustomerController.class)
                .getCustomerById(customer_id)).withSelfRel());
        c.add(linkTo(methodOn(CustomerController.class)
                .getCustomers()).withRel("all-customers"));
        return ResponseEntity.ok(c);
    }

    @DeleteMapping("/{customer_id}")
    public ResponseEntity<String> deleteCustomerById(@PathVariable String customer_id) {
        customerService.deleteCustomerbyCustomerId(customer_id);

        String msg = "Deleted " + customer_id;

        Link all = linkTo(methodOn(CustomerController.class).getCustomers()).withRel("all-customers");
        return ResponseEntity
                .noContent()
                .header(HttpHeaders.LINK, all.toUri().toString())
                .build();
    }



}
