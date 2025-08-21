package com.example.customerservices.utils.exceptions;

public class DuplicateCustomerEmailException extends RuntimeException{

    public DuplicateCustomerEmailException() {}

    public DuplicateCustomerEmailException(String customerEmail) {
        super("Customer with email " + customerEmail + " already exists.");
    }

    public DuplicateCustomerEmailException(Throwable cause) { super(cause); }

    public DuplicateCustomerEmailException(String message, Throwable cause) { super(message, cause); }
}
