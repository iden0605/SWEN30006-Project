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

    private final SmartBotMode attackingMode;
    private final SmartBotMode defendingMode;
    private final SmartBotMode minimalPlayMode;

    private boolean pendingMinimalPlay = false;

    public SmartBotPlayer(int ownPileIndex, Random random) {
        this.ownPileIndex = ownPileIndex;
        this.random = random;
        this.attackingMode = new AttackingMode(ownPileIndex, calc);
        this.defendingMode = new DefendingMode(ownPileIndex, calc);
        this.minimalPlayMode = new MinimalPlayMode(ownPileIndex, calc);
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

        // Collect all hearts that match the winning frequency and rank
        List<Card> winners = new ArrayList<>();
        for (Card h : hearts) {
            int value = ((Rank) h.getRank()).getScoreValue();
            int freq = rankFrequency.getOrDefault(value, 0);
            if (freq == bestFreq && value == bestRank) {
                winners.add(h);
            }
        }

        // Random choice for tiebreaks among equally ranked hearts
        return winners.get(random.nextInt(winners.size()));
    }

    /**
     * Chooses a card and pile for a non-heart turn, or returns a pass.
     * Dispatches to mode based on current pile values.
     */
    public Move selectMove(Hand hand, Hand[] piles) {
        if (pendingMinimalPlay) {
            pendingMinimalPlay = false;
            return minimalPlayMode.selectBestMove(hand, piles);
        }

        int[] ownRanks = calc.calculateRanks(piles[ownPileIndex]);
        int[] oppRanks = calc.calculateRanks(piles[1 - ownPileIndex]);

        // Attacking when attack cannot beat opponent defence
        SmartBotMode mode = (ownRanks[ATTACK] <= oppRanks[DEFENCE]) ? attackingMode : defendingMode;
        Move move = mode.selectBestMove(hand, piles);

        if (move.isPass()) {
            pendingMinimalPlay = true;
        }
        return move;
    }
}
