package thrones.game.bot;

import thrones.game.Suit;

/**
 * OM: play if a diamond would be played on the opponent's pile.
 */
public class OpponentMagicConsideration implements Consideration {
    @Override
    public Decision evaluate(Suit cardSuit, boolean isOwnPile) {
        if (cardSuit.isMagic() && !isOwnPile) {
            return Decision.PLAY;
        }
        return Decision.NO_OPINION;
    }
}
