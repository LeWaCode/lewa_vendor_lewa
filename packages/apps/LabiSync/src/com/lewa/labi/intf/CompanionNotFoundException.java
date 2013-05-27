package com.lewa.labi.intf;

public class CompanionNotFoundException extends IllegalArgumentException {
    public CompanionNotFoundException(String string) {
        super(string);
    }

    private static final long serialVersionUID = 552192174081528071L;
}
