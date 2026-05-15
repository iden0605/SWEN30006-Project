package thrones.game.bot;

import thrones.game.Suit;

/**
 * TD: play if a spade would be played on the bot's own pile.
 */
public class TeamDefenceConsideration implements Consideration {
    @Override
    public Decision evaluate(Suit cardSuit, boolean isOwnPile) {
        if (cardSuit.isDefence() && isOwnPile) {
            return Decision.PLAY;
        }
        return Decision.NO_OPINION;
    }
}
