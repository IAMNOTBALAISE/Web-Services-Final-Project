package com.example.customerservices.utils;

import jakarta.persistence.EntityNotFoundException;

import java.util.UUID;

public class ResourceNotFoundException extends EntityNotFoundException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public static ResourceNotFoundException saleNotFound(UUID saleId) {
        return new ResourceNotFoundException("Sale not found with ID: " + saleId);
    }
}
