package thrones.game;

import ch.aplu.jcardgame.Hand;

/**
 * Interface for computing the attack and defence values of a pile.
 */
public interface PileCalculationStrategy {

    /**
     * Returns an array containing the pile's attack value at index 0
     * and defence value at index 1.
     * Returns zeros for an empty pile.
     */
    int[] calculateRanks(Hand pile);
}
