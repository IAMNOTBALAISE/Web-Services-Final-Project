package com.example.orderservices.utils;

public class DuplicateOrderName extends RuntimeException{

    public DuplicateOrderName() {}

    public DuplicateOrderName(String orderName) {
        super("order with order name " + orderName + " already exists.");
    }

    public DuplicateOrderName(Throwable cause) { super(cause); }

    public DuplicateOrderName(String message, Throwable cause) { super(message, cause); }
}
