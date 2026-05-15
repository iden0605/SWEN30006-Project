package thrones.game.bot;

import thrones.game.Suit;

/**
 * A single bot decision-making rule. Each consideration can
 * return PLAY or PASS, otherwise NO_OPINION.
 */
public interface Consideration {
    Decision evaluate(Suit cardSuit, boolean isOwnPile);
}
