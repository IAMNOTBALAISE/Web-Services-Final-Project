package com.example.apigatewayservice.presentationlayer.customersdtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CustomerRequestModel {

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "First name is required")
    private String firstName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String emailAddress;

    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Province is required")
    private String province;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password1 is required")
    private String password1;

    @NotBlank(message = "Password2 is required")
    private String password2;

    @NotNull(message = "Phone numbers list cannot be null")
    @Size(min = 1, message = "At least one phone number is required")
    private List<PhoneNumber> phoneNumbers;


}
