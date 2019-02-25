package com.microsoft.samples.subpackage;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Class that describes some person
 *
 * This comment has links to:
 * <ul>
 * <li>Owner class {@link Person}</li>
 * <li>Its inner class {@link Person.IdentificationInfo}</li>
 * <li>Its method {@link Person#setLastName(String lastName)}</li>
 * <li>Its method without params {@link Person#setLastName()}</li>
 * <li>Its public field {@link Person#age}</li>
 * <li>Another class which used here {@link Set}</li>
 * <li>Another class which not used here {@link List}</li>
 * <li>Broken link {@link sdfdsagdsfghfgh}</li>
 * <li>Plain link {@linkplain someContent}</li>
 * <li>Link that starts from '#' {@link #setLastName()}</li>
 * <li>Link with label {@link Set WordOne}</li>
 * </ul>
 *
 * This is an "at" symbol: {@literal @}
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

    public void setLastName() {
        this.lastName = null;
    }

    public Set<String> getSomeSet() {
        return Collections.emptySet();
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
