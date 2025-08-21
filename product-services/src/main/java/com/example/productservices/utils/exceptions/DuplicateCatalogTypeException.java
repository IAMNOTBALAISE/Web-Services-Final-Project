package com.example.productservices.utils.exceptions;

public class DuplicateCatalogTypeException extends RuntimeException {


    public DuplicateCatalogTypeException() {}


    public DuplicateCatalogTypeException(String CatalogType) {
        super("Catalog with type " + CatalogType + " already exists.");
    }

    public DuplicateCatalogTypeException(Throwable cause) { super(cause); }

    public DuplicateCatalogTypeException (String message, Throwable cause) { super(message, cause); }
}
