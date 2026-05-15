package thrones.game.bot;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.Suit;

import java.util.List;

/**
 * Bot that decides whether to play or pass a card and pile, based on the
 * considerations.
 */
public class LegalBotPlayer {

    private final List<Consideration> considerations;

    public LegalBotPlayer(List<Consideration> considerations) {
        this.considerations = considerations;
    }

    /**
     * Returns PLAY or PASS for the given card and target pile.
     */
    public Decision decideMove(Card card, int selectedPileIndex, int ownPileIndex, Hand[] piles) {
        Suit suit = (Suit) card.getSuit();
        Hand targetPile = piles[selectedPileIndex];

        // A diamond cannot be played directly after a heart
        if (suit.isMagic() && !targetPile.isEmpty()) {
            Suit topSuit = (Suit) targetPile.get(targetPile.getNumberOfCards() - 1).getSuit();
            if (topSuit.isCharacter()) {
                return Decision.PASS;
            }
        }

        boolean isOwnPile = (selectedPileIndex == ownPileIndex);

        // Use the first consideration that is PLAY or PASS
        for (Consideration c : considerations) {
            Decision d = c.evaluate(suit, isOwnPile);
            if (d != Decision.NO_OPINION) {
                return d;
            }
        }

        // Default to pass action
        return Decision.PASS;
    }
}
