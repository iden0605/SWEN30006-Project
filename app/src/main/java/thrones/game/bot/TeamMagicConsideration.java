package thrones.game.bot;

import thrones.game.Suit;

/**
 * TM: pass if a diamond would be played on the bot's own pile.
 */
public class TeamMagicConsideration implements Consideration {
    @Override
    public Decision evaluate(Suit cardSuit, boolean isOwnPile) {
        if (cardSuit.isMagic() && isOwnPile) {
            return Decision.PASS;
        }
        return Decision.NO_OPINION;
    }
}
