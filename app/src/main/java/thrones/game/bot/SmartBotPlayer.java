package thrones.game.bot;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.PileCalculator;
import thrones.game.Rank;
import thrones.game.Suit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Smart bot variant that picks a card and pile based on the current
 * attack/defence values of the two piles.
 */
public class SmartBotPlayer {

    private static final int ATTACK = PileCalculator.ATTACK;
    private static final int DEFENCE = PileCalculator.DEFENCE;

    private final int ownPileIndex;
    private final Random random;
    private final PileCalculator calc = new PileCalculator();

    private boolean pendingMinimalPlay = false;

    public SmartBotPlayer(int ownPileIndex, Random random) {
        this.ownPileIndex = ownPileIndex;
        this.random = random;
    }

    /**
     * Resets the minimal play flag between play phases.
     */
    public void resetForNewPlay() {
        pendingMinimalPlay = false;
    }

    /**
     * Selects a heart from the hand for the first or second player turn.
     */
    public Card selectCharacter(Hand hand) {
        List<Card> hearts = new ArrayList<>();
        Map<Integer, Integer> rankFrequency = new HashMap<>();

        for (Card c : hand.getCardList()) {
            int value = ((Rank) c.getRank()).getScoreValue();
            rankFrequency.merge(value, 1, Integer::sum);
            if (((Suit) c.getSuit()).isCharacter()) {
                hearts.add(c);
            }
        }

        // Prioritise the heart rank that appears most often in the whole hand
        int bestFreq = -1;
        int bestRank = -1;
        for (Card h : hearts) {
            int value = ((Rank) h.getRank()).getScoreValue();
            int freq = rankFrequency.getOrDefault(value, 0);
            if (freq > bestFreq || (freq == bestFreq && value > bestRank)) {
                bestFreq = freq;
                bestRank = value;
            }
        }

        // Pick the heart with highest score value
        List<Card> winners = new ArrayList<>();
        for (Card h : hearts) {
            int value = ((Rank) h.getRank()).getScoreValue();
            int freq = rankFrequency.getOrDefault(value, 0);
            if (freq == bestFreq && value == bestRank) {
                winners.add(h);
            }
        }

        // Random choice for tiebreaks
        return winners.get(random.nextInt(winners.size()));
    }

    /**
     * Chooses a card and pile for a non-heart turn, or returns a pass.
     */
    public Move selectMove(Hand hand, Hand[] piles) {
        if (pendingMinimalPlay) {
            pendingMinimalPlay = false;
            Move m = selectMinimalMove(hand, piles);
            return m;
        }

        int[] ownRanks = calc.calculateRanks(piles[ownPileIndex]);
        int[] oppRanks = calc.calculateRanks(piles[opponentPileIndex()]);

        Move move;
        if (ownRanks[ATTACK] <= oppRanks[DEFENCE]) {
            move = selectAttackingMove(hand, piles);
        } else {
            move = selectDefendingMove(hand, piles);
        }

        if (move.isPass()) {
            pendingMinimalPlay = true;
        }
        return move;
    }

    private Move selectAttackingMove(Hand hand, Hand[] piles) {
        return selectByMode(hand, piles, ATTACK, DEFENCE, true);
    }

    private Move selectDefendingMove(Hand hand, Hand[] piles) {
        return selectByMode(hand, piles, DEFENCE, ATTACK, false);
    }

    /**
     * Scans the hand for the legal move that produces the largest effect on either
     * ownAxis or oppAxis.
     * Returns a pass if no helpful move, and prefers oppAxis on exact ties.
     */
    private Move selectByMode(Hand hand, Hand[] piles, int ownAxis, int oppAxis, boolean preferOppOnTie) {
        int bestEffect = 0;
        Move best = null;
        boolean bestIsOpp = false;

        for (Card c : hand.getCardList()) {
            Suit s = (Suit) c.getSuit();
            if (s.isCharacter())
                continue;

            // Attempt to play on own pile
            if (s.isAttack() && ownAxis == ATTACK || s.isDefence() && ownAxis == DEFENCE) {
                int delta = simulateDelta(piles[ownPileIndex], c, ownAxis);
                if (delta > 0 && betterMove(delta, false, bestEffect, bestIsOpp, preferOppOnTie)) {
                    bestEffect = delta;
                    bestIsOpp = false;
                    best = Move.play(c, ownPileIndex);
                }
            }

            // Attempt to play a diamond on the opponent pile to reduce oppAxis
            if (s.isMagic() && canPlayDiamondOn(piles[opponentPileIndex()])) {
                int before = calc.calculateRanks(piles[opponentPileIndex()])[oppAxis];
                int after = simulateNewRanks(piles[opponentPileIndex()], c)[oppAxis];
                int delta = before - after;
                if (delta > 0 && betterMove(delta, true, bestEffect, bestIsOpp, preferOppOnTie)) {
                    bestEffect = delta;
                    bestIsOpp = true;
                    best = Move.play(c, opponentPileIndex());
                }
            }
        }

        return best != null ? best : Move.pass();
    }

    /**
     * Picks the legal card with the smallest rank, used after a pass.
     * Ties broken by suit/target priority.
     * Returns a pass if no legal helpful move.
     */
    private Move selectMinimalMove(Hand hand, Hand[] piles) {
        List<Move> candidates = new ArrayList<>();
        List<Integer> effects = new ArrayList<>();
        List<Integer> priorities = new ArrayList<>();

        for (Card c : hand.getCardList()) {
            Suit s = (Suit) c.getSuit();
            if (s.isCharacter())
                continue;
            int rank = ((Rank) c.getRank()).getScoreValue();

            // Highest priority is Diamond on opponent
            if (s.isMagic() && canPlayDiamondOn(piles[opponentPileIndex()])) {
                int before = calc.calculateRanks(piles[opponentPileIndex()])[ATTACK]
                        + calc.calculateRanks(piles[opponentPileIndex()])[DEFENCE];
                int after = simulateNewRanks(piles[opponentPileIndex()], c)[ATTACK]
                        + simulateNewRanks(piles[opponentPileIndex()], c)[DEFENCE];
                if (after < before) {
                    candidates.add(Move.play(c, opponentPileIndex()));
                    effects.add(rank);
                    priorities.add(1);
                }
            }
            // Club on its own
            if (s.isAttack()) {
                candidates.add(Move.play(c, ownPileIndex));
                effects.add(rank);
                priorities.add(2);
            }
            // Spade on its own
            if (s.isDefence()) {
                candidates.add(Move.play(c, ownPileIndex));
                effects.add(rank);
                priorities.add(3);
            }
        }

        if (candidates.isEmpty()) {
            return Move.pass();
        }

        // Smallest rank wins, breaking ties with suit or target
        int bestIdx = 0;
        for (int i = 1; i < candidates.size(); i++) {
            if (effects.get(i) < effects.get(bestIdx)
                    || (effects.get(i).equals(effects.get(bestIdx)) && priorities.get(i) < priorities.get(bestIdx))) {
                bestIdx = i;
            }
        }
        return candidates.get(bestIdx);
    }

    /**
     * Returns true if the candidate move beats the current best move.
     */
    private boolean betterMove(int delta, boolean isOpp, int bestEffect, boolean bestIsOpp, boolean preferOppOnTie) {
        if (delta > bestEffect)
            return true;
        if (delta == bestEffect && bestEffect > 0) {
            return preferOppOnTie && isOpp && !bestIsOpp;
        }
        return false;
    }

    /**
     * Diamond cannot come after a heart.
     */
    private boolean canPlayDiamondOn(Hand pile) {
        if (pile.isEmpty()) {
            return false;
        }
        Suit top = (Suit) pile.get(pile.getNumberOfCards() - 1).getSuit();
        return !top.isCharacter();
    }

    /**
     * Calculates the change to a single axis when a card is added.
     */
    private int simulateDelta(Hand pile, Card candidate, int axis) {
        int before = calc.calculateRanks(pile)[axis];
        int after = simulateNewRanks(pile, candidate)[axis];
        return after - before;
    }

    /**
     * Runs a caclulator on a copy of the pile's card list with the candidate
     * appended.
     */
    private int[] simulateNewRanks(Hand pile, Card candidate) {
        List<Card> simulated = new ArrayList<>(pile.getCardList());
        simulated.add(candidate);
        return calc.calculateRanks(simulated);
    }

    /**
     * Returns the index of the opponent's pile.
     */
    private int opponentPileIndex() {
        return 1 - ownPileIndex;
    }
}
