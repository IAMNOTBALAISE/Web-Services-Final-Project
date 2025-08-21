package com.example.apigatewayservice.presentationlayer.customersdtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PhoneNumber {

    @NotNull
    private PhoneType type;

    @NotNull
    private String number;


}
