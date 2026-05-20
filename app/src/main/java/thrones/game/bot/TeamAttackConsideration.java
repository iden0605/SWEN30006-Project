package thrones.game.bot;

import thrones.game.Suit;

/**
 * TA: play if a club would be played on the bot's own pile.
 */
public class TeamAttackConsideration implements Consideration {
    @Override
    public Decision evaluate(Suit cardSuit, boolean isOwnPile) {
        if (cardSuit.isAttack() && isOwnPile) {
            return Decision.PLAY;
        }
        return Decision.NO_OPINION;
    }
}
