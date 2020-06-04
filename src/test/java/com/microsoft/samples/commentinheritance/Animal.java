package com.microsoft.samples.commentinheritance;

/**
 * Animal.
 */
public abstract class Animal implements Organism{
    /**
     * Breathe.
     */
    public void breathe() {
    }

    /**
     * Communicate verbally.
     */
    public abstract void verballyCommunicate();

    /**
     * Feed offspring.
     */
    public abstract void feed();

    /**
     * {@inheritDoc}
     * Get kind from Animal.
     */
    public abstract String getKind();
}
