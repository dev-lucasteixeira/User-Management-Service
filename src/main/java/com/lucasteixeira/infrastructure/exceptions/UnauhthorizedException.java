package com.lucasteixeira.infrastructure.exceptions;

import javax.naming.AuthenticationException;

public class UnauhthorizedException extends AuthenticationException {
    public UnauhthorizedException(String message) {
        super(message);
    }
}
