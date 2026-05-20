package thrones.game.bot;

import thrones.game.Suit;

/**
 * OD: pass if a spade would be played on the opponent's pile.
 */
public class OpponentDefenceConsideration implements Consideration {
    @Override
    public Decision evaluate(Suit cardSuit, boolean isOwnPile) {
        if (cardSuit.isDefence() && !isOwnPile) {
            return Decision.PASS;
        }
        return Decision.NO_OPINION;
    }
}
