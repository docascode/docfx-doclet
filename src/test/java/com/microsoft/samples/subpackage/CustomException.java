package com.microsoft.samples.subpackage;

public class CustomException extends Exception {

    public CustomException(String message) {
        super(message);
    }

    /**
     * We need to have such method that throw exception declared in the same class
     *
     * @throws CustomException with reason message
     */
    public void makeSomething() throws CustomException {
        throw new CustomException("It happened!");
    }

    private class PrivateException {
        private String message;
    }
}
