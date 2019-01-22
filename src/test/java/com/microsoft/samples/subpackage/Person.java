package com.microsoft.samples.subpackage;

import java.util.List;

/**
 * Class that describes some person
 *
 * This comment has links to:
 * <ul>
 *   <li>Class {@link Person}</li>
 *   <li>Its method {@link Person#setLastName(String)}</li>
 *   <li>Its public field {@link Person#age}</li>
 *   <li>Another class {@link List}</li>
 * </ul>
 * @see Display
 */
public class Person<T> {

    private String firstName;
    private String lastName;
    public int age;

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
