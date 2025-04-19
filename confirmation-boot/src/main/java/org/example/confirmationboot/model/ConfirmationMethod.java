package org.example.confirmationboot.model;

import org.example.confirmationboot.exception.InvalidConfirmationMethodException;

public enum ConfirmationMethod {
    EMAIL,
    SMS;

    public static ConfirmationMethod getConfirmationMethod(String confirmationMethod) {
        try{
            return ConfirmationMethod.valueOf(confirmationMethod);
        } catch (IllegalArgumentException e){
            throw new InvalidConfirmationMethodException("Invalid confirmation method: " + confirmationMethod);
        }
    }
}
