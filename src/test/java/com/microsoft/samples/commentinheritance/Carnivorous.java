package com.microsoft.samples.commentinheritance;

/**
 * Marks an Animal that eats other animals.
 */
public interface Carnivorous {
    /**
     * Eat the provided animal.
     *
     * @param animalBeingEaten Animal that will be eaten.
     */
    void eat(Animal animalBeingEaten);

    /**
     * Get kind from Carnivorous.
     */
    String getKind();
}
