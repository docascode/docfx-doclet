package com.microsoft.samples.subpackage;

/**
 * Class that describes some person
 */
public class Person<T> {

    /**
     * This is a first name of a {@link Person} entity
     */
    private String firstName;
    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * We need to have this method that takes parameter and return types declared in the current class
     */
    public static Person buildPerson(Person seed) {
        return seed;
    }

    /**
     * Class that describes person's identification
     */
    public static class IdentificationInfo {
        /**
         * Enum describes person's gender
         */
        public enum Gender {
            MALE, FEMALE
        }
    }
}
