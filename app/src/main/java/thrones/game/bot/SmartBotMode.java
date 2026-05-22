package thrones.game.bot;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.PileCalculator;
import thrones.game.Suit;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for bot move selection modes.
 * Defines a skeleton for selecting a card and pile on a single turn.
 */
public abstract class SmartBotMode {

    protected static final int ATTACK = PileCalculator.ATTACK;
    protected static final int DEFENCE = PileCalculator.DEFENCE;

    protected final int ownPileIndex;
    protected final PileCalculator calc;

    protected SmartBotMode(int ownPileIndex, PileCalculator calc) {
        this.ownPileIndex = ownPileIndex;
        this.calc = calc;
    }

    // Subclasses control which axis is targeted via hooks
    public Move selectBestMove(Hand hand, Hand[] piles) {
        int bestEffect = 0;
        Move best = null;
        boolean bestIsOpp = false;

        for (Card c : hand.getCardList()) {
            Suit s = (Suit) c.getSuit();
            if (s.isCharacter())
                continue;

            // Club in attacking mode, spade in defending mode
            if (isOwnBenefit(s)) {
                int delta = simulateDelta(piles[ownPileIndex], c, ownAxis());
                if (delta > 0 && isBetter(delta, false, bestEffect, bestIsOpp)) {
                    bestEffect = delta;
                    bestIsOpp = false;
                    best = Move.play(c, ownPileIndex);
                }
            }

            // Diamond reduces the opponent's targeted axis
            if (s.isMagic() && canPlayDiamondOn(piles[oppPileIndex()])) {
                int before = calc.calculateRanks(piles[oppPileIndex()])[oppAxis()];
                int after = simulateNewRanks(piles[oppPileIndex()], c)[oppAxis()];
                int delta = before - after;
                if (delta > 0 && isBetter(delta, true, bestEffect, bestIsOpp)) {
                    bestEffect = delta;
                    bestIsOpp = true;
                    best = Move.play(c, oppPileIndex());
                }
            }
        }

        return best != null ? best : Move.pass();
    }

    /** The own-pile axis this mode aims to improve. */
    protected abstract int ownAxis();

    /** The opponent-pile axis this mode aims to reduce. */
    protected abstract int oppAxis();

    /** On tie, prefer the opponent move. */
    protected abstract boolean prefersOppOnTie();

    /** Returns true if the suit provides a benefit. */
    protected abstract boolean isOwnBenefit(Suit suit);

    protected final int oppPileIndex() {
        return 1 - ownPileIndex;
    }

    // Diamond is illegal directly on top of a heart card
    protected final boolean canPlayDiamondOn(Hand pile) {
        if (pile.isEmpty())
            return false;
        Suit top = (Suit) pile.get(pile.getNumberOfCards() - 1).getSuit();
        return !top.isCharacter();
    }

    protected final int simulateDelta(Hand pile, Card candidate, int axis) {
        return simulateNewRanks(pile, candidate)[axis] - calc.calculateRanks(pile)[axis];
    }

    // Appends candidate
    protected final int[] simulateNewRanks(Hand pile, Card candidate) {
        List<Card> copy = new ArrayList<>(pile.getCardList());
        copy.add(candidate);
        return calc.calculateRanks(copy);
    }

    private boolean isBetter(int delta, boolean isOpp, int bestEffect, boolean bestIsOpp) {
        if (delta > bestEffect)
            return true;
        if (delta == bestEffect && bestEffect > 0)
            return prefersOppOnTie() && isOpp && !bestIsOpp;
        return false;
    }
}
