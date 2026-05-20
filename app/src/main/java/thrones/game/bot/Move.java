package thrones.game.bot;

import ch.aplu.jcardgame.Card;

/**
 * A bots chosen action, either play the card on the pile, or pass.
 */
public class Move {

    private final Card card;
    private final int pileIndex;

    private Move(Card card, int pileIndex) {
        this.card = card;
        this.pileIndex = pileIndex;
    }

    public static Move play(Card card, int pileIndex) {
        return new Move(card, pileIndex);
    }

    public static Move pass() {
        return new Move(null, 0);
    }

    public boolean isPass() {
        return card == null;
    }

    public Card getCard() {
        return card;
    }

    public int getPileIndex() {
        return pileIndex;
    }
}
