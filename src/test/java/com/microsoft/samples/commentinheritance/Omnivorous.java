package com.microsoft.samples.commentinheritance;

/**
 * Eats plants and animals.
 */
public interface Omnivorous extends Carnivorous, Herbivorous {
    @Override
    void eat(Animal animalToBeEaten);

    @Override
    void eat(Plant plantToBeEaten);

    /**
     * {@inheritDoc}
     * Get kind from Omnivorous.
     */
    @Override
    String getKind();
}
