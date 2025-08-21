package com.example.productservices.utils.exceptions;

public class DuplicateWatchModelException extends RuntimeException {

  public DuplicateWatchModelException() {}

  public DuplicateWatchModelException(String watchModel) {
    super("Watch with model " + watchModel + " already exists.");
  }

  public DuplicateWatchModelException(Throwable cause) { super(cause); }

  public DuplicateWatchModelException(String message, Throwable cause) { super(message, cause); }
}
