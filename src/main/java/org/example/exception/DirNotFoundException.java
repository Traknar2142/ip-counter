package org.example.exception;

public class DirNotFoundException extends RuntimeException{
    public DirNotFoundException(String message) {
        super(message);
    }
}
