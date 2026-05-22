package thrones.game.bot;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import thrones.game.PileCalculator;
import thrones.game.Rank;
import thrones.game.Suit;

import java.util.ArrayList;
import java.util.List;

/**
 * Selects the lowest-rank card following a pass.
 */
public class MinimalPlayMode extends SmartBotMode {

    public MinimalPlayMode(int ownPileIndex, PileCalculator calc) {
        super(ownPileIndex, calc);
    }

    @Override
    public Move selectBestMove(Hand hand, Hand[] piles) {
        List<Move> candidates = new ArrayList<>();
        List<Integer> effects = new ArrayList<>();
        List<Integer> priorities = new ArrayList<>();

        for (Card c : hand.getCardList()) {
            Suit s = (Suit) c.getSuit();
            if (s.isCharacter())
                continue;
            int rank = ((Rank) c.getRank()).getScoreValue();

            // Diamond that reduces opponent's total
            if (s.isMagic() && canPlayDiamondOn(piles[oppPileIndex()])) {
                int before = calc.calculateRanks(piles[oppPileIndex()])[ATTACK]
                        + calc.calculateRanks(piles[oppPileIndex()])[DEFENCE];
                int after = simulateNewRanks(piles[oppPileIndex()], c)[ATTACK]
                        + simulateNewRanks(piles[oppPileIndex()], c)[DEFENCE];
                if (after < before) {
                    candidates.add(Move.play(c, oppPileIndex()));
                    effects.add(rank);
                    priorities.add(1);
                }
            }
            // Club on own pile
            if (s.isAttack()) {
                candidates.add(Move.play(c, ownPileIndex));
                effects.add(rank);
                priorities.add(2);
            }
            // Spade on own pile
            if (s.isDefence()) {
                candidates.add(Move.play(c, ownPileIndex));
                effects.add(rank);
                priorities.add(3);
            }
        }

        if (candidates.isEmpty())
            return Move.pass();

        // Pick smallest rank
        int bestIdx = 0;
        for (int i = 1; i < candidates.size(); i++) {
            if (effects.get(i) < effects.get(bestIdx)
                    || (effects.get(i).equals(effects.get(bestIdx))
                            && priorities.get(i) < priorities.get(bestIdx))) {
                bestIdx = i;
            }
        }
        return candidates.get(bestIdx);
    }

    @Override
    protected int ownAxis() {
        return ATTACK;
    }

    @Override
    protected int oppAxis() {
        return DEFENCE;
    }

    @Override
    protected boolean prefersOppOnTie() {
        return true;
    }

    @Override
    protected boolean isOwnBenefit(Suit suit) {
        return false;
    }
}
