package thrones.game.bot;

import thrones.game.PileCalculator;
import thrones.game.Suit;

/**
 * Attacking mode, maximises attack or reduces opponent defence.
 */
public class AttackingMode extends SmartBotMode {

    public AttackingMode(int ownPileIndex, PileCalculator calc) {
        super(ownPileIndex, calc);
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
        return suit.isAttack();
    }
}
