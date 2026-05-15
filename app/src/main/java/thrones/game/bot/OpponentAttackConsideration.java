package thrones.game.bot;

import thrones.game.Suit;

/**
 * OA: pass if a club would be played on the opponent's pile
 */
public class OpponentAttackConsideration implements Consideration {
    @Override
    public Decision evaluate(Suit cardSuit, boolean isOwnPile) {
        if (cardSuit.isAttack() && !isOwnPile) {
            return Decision.PASS;
        }
        return Decision.NO_OPINION;
    }
}
