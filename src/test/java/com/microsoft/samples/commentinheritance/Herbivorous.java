package com.microsoft.samples.commentinheritance;

/**
 * Marks animals that eat plants.
 */
public interface Herbivorous {
    /**
     * Eat the provided plant.
     *
     * @param plantToBeEaten Plant that will be eaten.
     */
    void eat(Plant plantToBeEaten);

    /**
     * Get kind from Herbivorous.
     */
    String getKind();

    public class Plant {
    }

}
