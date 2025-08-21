package com.example.serviceplanservices.utils.exceptions;

public class DuplicateCoverageDetailsException extends RuntimeException {

  public DuplicateCoverageDetailsException() {}

  public DuplicateCoverageDetailsException(String coverageDetails) {
    super("Coverage details " + coverageDetails + " already exists.");
  }

  public DuplicateCoverageDetailsException(Throwable cause) { super(cause); }

  public DuplicateCoverageDetailsException(String message, Throwable cause) { super(message, cause); }
}
