package thrones.game.bot;

import thrones.game.PileCalculator;
import thrones.game.Suit;

/**
 * Defending mode, maximises defence or reduces opponent attack.
 */
public class DefendingMode extends SmartBotMode {

    public DefendingMode(int ownPileIndex, PileCalculator calc) {
        super(ownPileIndex, calc);
    }

    @Override
    protected int ownAxis() {
        return DEFENCE;
    }

    @Override
    protected int oppAxis() {
        return ATTACK;
    }

    @Override
    protected boolean prefersOppOnTie() {
        return false;
    }

    @Override
    protected boolean isOwnBenefit(Suit suit) {
        return suit.isDefence();
    }
}
