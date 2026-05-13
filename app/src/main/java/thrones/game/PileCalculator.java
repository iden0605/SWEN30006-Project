package thrones.game;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.List;

/**
 * Computes the attack and defence values of a pile by processing the cards in
 * play order.
 */
public class PileCalculator implements PileCalculationStrategy {

    public static final int ATTACK = GameOfThrones.ATTACK_RANK_INDEX;
    public static final int DEFENCE = GameOfThrones.DEFENCE_RANK_INDEX;

    private static final int NO_TARGET = -1;

    @Override
    public int[] calculateRanks(Hand pile) {
        int[] ranks = new int[] { 0, 0 };
        if (pile == null || pile.isEmpty()) {
            return ranks;
        }

        List<Card> cards = pile.getCardList();

        // The first card is always a heart
        Card characterCard = cards.get(0);
        int characterValue = ((Rank) characterCard.getRank()).getScoreValue();
        ranks[ATTACK] = characterValue;
        ranks[DEFENCE] = characterValue;

        // Target of the most recent effect card, diamonds inherit it.
        int lastEffectTarget = NO_TARGET;

        for (int i = 1; i < cards.size(); i++) {
            Card current = cards.get(i);
            Card previous = cards.get(i - 1);
            Suit currentSuit = (Suit) current.getSuit();
            int currentValue = ((Rank) current.getRank()).getScoreValue();
            int previousValue = ((Rank) previous.getRank()).getScoreValue();

            // Equal rank doubling
            int effect = (currentValue == previousValue) ? currentValue * 2 : currentValue;

            if (currentSuit.isAttack()) {
                ranks[ATTACK] += effect;
                lastEffectTarget = ATTACK;
            } else if (currentSuit.isDefence()) {
                ranks[DEFENCE] += effect;
                lastEffectTarget = DEFENCE;
            } else if (currentSuit.isMagic()) {
                // A diamond after a heart is illegal
                if (lastEffectTarget == NO_TARGET) {
                    continue;
                }
                ranks[lastEffectTarget] -= effect;

                // Floor to zero
                if (ranks[lastEffectTarget] < 0) {
                    ranks[lastEffectTarget] = 0;
                }
            }
        }

        return ranks;
    }
}
