package com.example.customerservices.dataaccesslayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
public class PhoneNumber {
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PhoneType type;

    @Column(name = "number")
    private String number;
}
