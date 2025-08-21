package com.example.apigatewayservice.presentationlayer.customersdtos;


import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CustomerResponseModel extends RepresentationModel<CustomerResponseModel> {

    private String customerId;
    private String lastName;
    private String firstName;
    private String emailAddress;
    private String streetAddress;
    private String postalCode;
    private String city;
    private String province;
    private List<PhoneNumber> phoneNumbers;


}
