package com.microsoft.samples.commentinheritance;

/**
 * Mammals that give birth to young that develop within the mother's body.
 */
public interface Viviparous {

    void giveBirth(int numberOfOffspring);

    /**
     * Get kind from Viviparous.
     */
    String getKind();
}
