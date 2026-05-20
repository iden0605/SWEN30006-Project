package thrones.game;

// Oh_Heaven.java

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;
import thrones.game.bot.ConsiderationFactory;
import thrones.game.bot.Decision;
import thrones.game.bot.LegalBotPlayer;
import thrones.game.bot.Move;
import thrones.game.bot.SmartBotPlayer;
import thrones.game.utility.Logger;

import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class GameOfThrones extends CardGame {

    /*
     * Canonical String representations of Suit, Rank, Card, and Hand
     */
    String canonical(Suit s) {
        return s.toString().substring(0, 1);
    }

    String canonical(Rank r) {
        switch (r) {
            case ACE:
            case KING:
            case QUEEN:
            case JACK:
            case TEN:
                return r.toString().substring(0, 1);
            default:
                return String.valueOf(r.getShortHandValue());
        }
    }

    String canonical(Card c) {
        return canonical((Rank) c.getRank()) + canonical((Suit) c.getSuit());
    }

    String canonical(Hand h) {
        return "[" + h.getCardList().stream().map(this::canonical).collect(Collectors.joining(",")) + "]";
    }

    static Random random = new Random(30006);

    private boolean isAuto = false;
    public final int nbPlayers = 4;
    private final int nbHeartCardsPerPlayer = 3;
    private final int nbEffectCardsPerPlayer = 9;
    public final int nbPlays = 2;
    public final int nbRounds = 3;
    private Deck deck = new Deck(Suit.values(), Rank.values(), "cover");
    private final Location[] handLocations = {
            new Location(350, 625),
            new Location(75, 350),
            new Location(350, 75),
            new Location(625, 350)
    };

    private final Location[] scoreLocations = {
            new Location(575, 675),
            new Location(25, 575),
            new Location(25, 25),
            new Location(575, 125)
    };
    private final Location[] pileLocations = {
            new Location(350, 280),
            new Location(350, 430)
    };
    private final Location[] pileStatusLocations = {
            new Location(300, 200),
            new Location(300, 520)
    };

    private enum PlayerType {
        HUMAN,
        RANDOM,
        LEGAL,
        SMART;

        public static PlayerType fromStringToPlayerType(String playerTypeStr) {
            if (playerTypeStr == null || playerTypeStr.trim().isEmpty()) {
                return null; // Invalid input
            }

            try {
                // Case-insensitive conversion
                return PlayerType.valueOf(playerTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // No matching enum constant
                return null;
            }
        }
    }

    private final Actor[] pileTextActors = { null, null };
    private final Actor[] scoreActors = { null, null, null, null };
    private final int watchingTime = 5000;
    private Hand[] hands;
    private Hand[] piles;

    private int nextStartingPlayer = random.nextInt(nbPlayers);

    private int[] scores = new int[nbPlayers];
    private Logger logger = new Logger();

    Font bigFont = new Font("Arial", Font.BOLD, 36);
    Font smallFont = new Font("Arial", Font.PLAIN, 10);
    PlayerType[] playerTypes = { PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN, PlayerType.HUMAN };
    private int currentPlay = 0;
    private List<Integer> firstPlayers = new ArrayList<>();
    private int NUMBER_OF_PLAYS = 2;
    private List<List<List<String>>> playerAutoMovements = new ArrayList<>();
    private List<Integer> playerMovementIndexes = new ArrayList<>();
    private List<List<List<String>>> initialCardStrings = new ArrayList<>();
    private List<List<List<String>>> initialHeartStrings = new ArrayList<>();

    // Bot for each player slot, null if not LEGAL
    private LegalBotPlayer[] legalBots = new LegalBotPlayer[nbPlayers];

    // Smart bot per slot, null if not SMART
    private SmartBotPlayer[] smartBots = new SmartBotPlayer[nbPlayers];

    private List<String> cardListFromKey(Properties properties, String key) {
        String initialCardString = properties.getProperty(key);
        if (initialCardString != null && !initialCardString.isEmpty()) {
            return Arrays.asList(initialCardString.split(","));
        }
        return null;
    }

    /**
     * Initialise object
     */
    private void initWithProperties(Properties properties) {
        isAuto = Boolean.parseBoolean(properties.getProperty("isAuto"));
        for (int i = 0; i < nbPlayers; i++) {
            playerTypes[i] = PlayerType.fromStringToPlayerType(properties.getProperty("players." + i));
            playerMovementIndexes.add(0);

            // Build Legal and Smart bots in slots
            if (playerTypes[i] == PlayerType.LEGAL) {
                String csv = properties.getProperty("players." + i + ".considerations");
                legalBots[i] = new LegalBotPlayer(ConsiderationFactory.fromCodes(csv));
            }
            if (playerTypes[i] == PlayerType.SMART) {
                smartBots[i] = new SmartBotPlayer(i % 2, random);
            }
        }

        for (int i = 0; i < NUMBER_OF_PLAYS; i++) {
            String firstPlayerString = properties.getProperty("plays." + i + ".firstPlayer");
            if (firstPlayerString != null && !firstPlayerString.isEmpty()) {
                firstPlayers.add(Integer.parseInt(firstPlayerString));
            } else {
                firstPlayers.add(-1);
            }
        }

        for (int i = 0; i < NUMBER_OF_PLAYS; i++) {
            initialCardStrings.add(new ArrayList<>());
            playerAutoMovements.add(new ArrayList<>());
            initialHeartStrings.add(new ArrayList<>());
            for (int j = 0; j < nbPlayers; j++) {
                String initialCardKey = "plays." + i + ".players." + j + ".initialCards";
                List<String> initialStrings = cardListFromKey(properties, initialCardKey);
                initialCardStrings.get(i).add(new ArrayList<>());
                if (initialStrings != null) {
                    initialCardStrings.get(i).get(j).addAll(initialStrings);
                }

                String movementCardKey = "plays." + i + ".players." + j + ".cardsPlayed";
                List<String> movementStrings = cardListFromKey(properties, movementCardKey);
                playerAutoMovements.get(i).add(new ArrayList<>());
                if (movementStrings != null) {
                    playerAutoMovements.get(i).get(j).addAll(movementStrings);
                }

                String heartCardKey = "plays." + i + ".players." + j + ".initialHearts";
                List<String> heartStrings = cardListFromKey(properties, heartCardKey);
                initialHeartStrings.get(i).add(new ArrayList<>());
                if (heartStrings != null) {
                    initialHeartStrings.get(i).get(j).addAll(heartStrings);
                }
            }
        }
    }

    public GameOfThrones(Properties properties) {
        super(700, 700, 30);
        initWithProperties(properties);
        setSimulationPeriod(100);
        int version = 1;
        setTitle("Game of Thrones (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
        setStatusText("Initializing...");
        initScore();

        setupGame();
    }

    public String runApp() {
        while (currentPlay < nbPlays) {
            executeAPlay();
            updateScores();

        }

        String text;
        if (scores[0] > scores[1]) {
            text = "Players 0 and 2 won.";
        } else if (scores[0] == scores[1]) {
            text = "All players drew.";
        } else {
            text = "Players 1 and 3 won.";
        }
        System.out.println("Result: " + text);
        setStatusText(text);

        refresh();
        return logger.getAllLog();
    }

    private void sortHand(Hand hand) {
        List<Card> cards = hand.getCardList();
        Comparator<Card> cardComparator = (o1, o2) -> {
            Suit suit1 = (Suit) o1.getSuit();
            Suit suit2 = (Suit) o2.getSuit();
            Rank rank1 = (Rank) o1.getRank();
            Rank rank2 = (Rank) o2.getRank();

            if (suit1.ordinal() - suit2.ordinal() != 0) {
                return suit1.ordinal() - suit2.ordinal();
            }

            return rank1.getShortHandValue() - rank2.getShortHandValue();
        };

        cards.sort(cardComparator);
    }

    // return random Card from Hand
    public static Card randomCard(Hand hand) {
        assert !hand.isEmpty() : " random card from empty hand.";
        int x = random.nextInt(hand.getNumberOfCards());
        return hand.get(x);
    }

    private Rank getRankFromString(String cardName) {
        String rankString = cardName.substring(0, cardName.length() - 1);
        Integer rankValue = Integer.parseInt(rankString);

        for (Rank rank : Rank.values()) {
            if (rank.getShortHandValue() == rankValue) {
                return rank;
            }
        }

        return Rank.ACE;
    }

    private Suit getSuitFromString(String cardName) {
        String suitString = cardName.substring(cardName.length() - 1, cardName.length());

        for (Suit suit : Suit.values()) {
            if (suit.getSuitShortHand().equals(suitString)) {
                return suit;
            }
        }
        return Suit.CLUBS;
    }

    private Card getCardFromList(List<Card> cards, String cardName) {
        Rank existingRank = getRankFromString(cardName);
        Suit existingSuit = getSuitFromString(cardName);
        for (Card card : cards) {
            Suit suit = (Suit) card.getSuit();
            Rank rank = (Rank) card.getRank();
            if (suit.getSuitShortHand().equals(existingSuit.getSuitShortHand())
                    && rank.getShortHandValue() == existingRank.getShortHandValue()) {
                return card;
            }
        }

        return null;
    }

    private void dealingOutHeartCards(Hand[] hands, Hand pack) {
        List<Card> heartCards = pack.getCardsWithSuit(Suit.HEARTS);
        for (int i = 0; i < nbPlayers; i++) {
            int remainingHeartCards = nbHeartCardsPerPlayer;
            if (isAuto) {
                if (!initialHeartStrings.get(currentPlay).get(i).isEmpty()) {
                    List<String> playerHeartStrings = initialHeartStrings.get(currentPlay).get(i);
                    for (String heartString : playerHeartStrings) {
                        Card card = getCardFromList(heartCards, heartString);
                        assert card != null;
                        card.removeFromHand(false);
                        hands[i].insert(card, false);
                        remainingHeartCards--;
                    }
                }
            }

            for (int j = 0; j < remainingHeartCards; j++) {
                int x = random.nextInt(heartCards.size());
                Card randomCard = heartCards.get(x);
                randomCard.removeFromHand(false);
                hands[i].insert(randomCard, false);
            }
        }
    }

    private void dealingOutEffectCards(Hand[] hands, Hand pack) {
        for (int i = 0; i < nbPlayers; i++) {
            int remainingEffectCards = nbEffectCardsPerPlayer;
            if (isAuto) {
                if (!initialCardStrings.get(currentPlay).get(i).isEmpty()) {
                    List<String> playerEffectStrings = initialCardStrings.get(currentPlay).get(i);
                    for (String effectString : playerEffectStrings) {
                        Card card = getCardFromList(pack.getCardList(), effectString);
                        assert card != null;
                        card.removeFromHand(false);
                        hands[i].insert(card, false);
                        remainingEffectCards--;
                    }
                }
            }
            for (int j = 0; j < remainingEffectCards; j++) {
                assert !pack.isEmpty() : " Pack has prematurely run out of cards.";
                Card dealt = randomCard(pack);
                dealt.removeFromHand(false);
                hands[i].insert(dealt, false);
            }
        }
    }

    private void dealingOut(Hand[] hands) {
        Hand pack = deck.toHand(false);
        assert pack.getNumberOfCards() == 52 : " Starting pack is not 52 cards.";
        // Remove 4 Aces
        List<Card> aceCards = pack.getCardsWithRank(Rank.ACE);
        for (Card card : aceCards) {
            card.removeFromHand(false);
        }
        assert pack.getNumberOfCards() == 48 : " Pack without aces is not 48 cards.";
        // Give each player 3 heart cards
        dealingOutHeartCards(hands, pack);

        assert pack.getNumberOfCards() == 36 : " Pack without aces and hearts is not 36 cards.";
        // Give each player 9 of the remaining cards
        dealingOutEffectCards(hands, pack);

        for (int j = 0; j < nbPlayers; j++) {
            sortHand(hands[j]);
            logger.logPlayerCards(hands[j], j);
            assert hands[j].getNumberOfCards() == 12 : " Hand does not have twelve cards.";
        }
    }

    private void initScore() {
        for (int i = 0; i < nbPlayers; i++) {
            scores[i] = 0;
            String text = "P" + i + "-0";
            scoreActors[i] = new TextActor(text, Color.WHITE, bgColor, bigFont);
            addActor(scoreActors[i], scoreLocations[i]);
        }

        String text = "Attack: 0 - Defence: 0";
        for (int i = 0; i < pileTextActors.length; i++) {
            pileTextActors[i] = new TextActor(text, Color.WHITE, bgColor, smallFont);
            addActor(pileTextActors[i], pileStatusLocations[i]);
        }
    }

    private void updateScore(int player) {
        removeActor(scoreActors[player]);
        String text = "P" + player + "-" + scores[player];
        scoreActors[player] = new TextActor(text, Color.WHITE, bgColor, bigFont);
        addActor(scoreActors[player], scoreLocations[player]);
    }

    private void updateScores() {
        for (int i = 0; i < nbPlayers; i++) {
            updateScore(i);
        }
    }

    private Optional<Card> selected;
    private final int NON_SELECTION_VALUE = -1;
    private int selectedPileIndex = NON_SELECTION_VALUE;
    private final int UNDEFINED_INDEX = -1;
    public static final int ATTACK_RANK_INDEX = 0;
    public static final int DEFENCE_RANK_INDEX = 1;

    private void setupGame() {
        hands = new Hand[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            hands[i] = new Hand(deck);
        }
        dealingOut(hands);

        for (int i = 0; i < nbPlayers; i++) {
            hands[i].sort(Hand.SortType.SUITPRIORITY, true);
            System.out.println("hands[" + i + "]: " + canonical(hands[i]));
        }

        for (final Hand currentHand : hands) {
            // Set up human player for interaction
            currentHand.addCardListener(new CardAdapter() {
                public void leftDoubleClicked(Card card) {
                    selected = Optional.of(card);
                    currentHand.setTouchEnabled(false);
                }

                public void rightClicked(Card card) {
                    selected = Optional.empty(); // Don't care which card we right-clicked for player to pass
                    currentHand.setTouchEnabled(false);
                }
            });
        }
        // graphics
        RowLayout[] layouts = new RowLayout[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            int handWidth = 400;
            layouts[i] = new RowLayout(handLocations[i], handWidth);
            layouts[i].setRotationAngle(90 * i);
            hands[i].setView(this, layouts[i]);
            hands[i].draw();
        }
        // End graphics
    }

    private void resetIndexes() {
        for (int i = 0; i < playerMovementIndexes.size(); i++) {
            playerMovementIndexes.set(i, 0);
        }
    }

    private void resetPile() {
        if (piles != null) {
            for (Hand pile : piles) {
                pile.removeAll(true);
            }
        }
        piles = new Hand[2];
        for (int i = 0; i < 2; i++) {
            piles[i] = new Hand(deck);
            int pileWidth = 40;
            piles[i].setView(this, new RowLayout(pileLocations[i], 8 * pileWidth));
            piles[i].draw();
            final Hand currentPile = piles[i];
            final int pileIndex = i;
            piles[i].addCardListener(new CardAdapter() {
                public void leftClicked(Card card) {
                    selectedPileIndex = pileIndex;
                    currentPile.setTouchEnabled(false);
                }
            });
        }

        updatePileRanks();
    }

    private void pickACorrectSuit(int playerIndex, boolean isCharacter) {
        Hand currentHand = hands[playerIndex];
        List<Card> shortListCards = new ArrayList<>();
        for (int i = 0; i < currentHand.getCardList().size(); i++) {
            Card card = currentHand.getCardList().get(i);
            Suit suit = (Suit) card.getSuit();
            if (suit.isCharacter() == isCharacter) {
                shortListCards.add(card);
            }
        }
        if (shortListCards.isEmpty() || !isCharacter && random.nextInt(3) == 0) {
            selected = Optional.empty();
        } else {
            selected = Optional.of(shortListCards.get(random.nextInt(shortListCards.size())));
        }
    }

    private void selectRandomPile() {
        selectedPileIndex = random.nextInt(2);
    }

    private void waitForCorrectSuit(int playerIndex, boolean isCharacter) {
        if (hands[playerIndex].isEmpty()) {
            selected = Optional.empty();
        } else {
            selected = null;
            hands[playerIndex].setTouchEnabled(true);
            do {
                if (selected == null) {
                    delay(100);
                    continue;
                }
                Suit suit = selected.isPresent() ? (Suit) selected.get().getSuit() : null;
                if (isCharacter && suit != null && suit.isCharacter() || // If we want character, can't pass and suit
                                                                         // must be right
                        !isCharacter && (suit == null || !suit.isCharacter())) { // If we don't want character, can pass
                                                                                 // or suit must not be character
                    // if (suit != null && suit.isCharacter() == isCharacter) {
                    break;
                } else {
                    selected = null;
                    hands[playerIndex].setTouchEnabled(true);
                }
                delay(100);
            } while (true);
        }
    }

    private void waitForPileSelection() {
        selectedPileIndex = NON_SELECTION_VALUE;
        for (Hand pile : piles) {
            pile.setTouchEnabled(true);
        }
        while (selectedPileIndex == NON_SELECTION_VALUE) {
            delay(100);
        }
        for (Hand pile : piles) {
            pile.setTouchEnabled(false);
        }
    }

    private PileCalculationStrategy pileCalculator = new PileCalculator();

    private int[] calculatePileRanks(int pileIndex) {
        return pileCalculator.calculateRanks(piles[pileIndex]);
    }

    private void updatePileRankState(int pileIndex, int attackRank, int defenceRank) {
        TextActor currentPile = (TextActor) pileTextActors[pileIndex];
        removeActor(currentPile);
        String text = "Attack: " + attackRank + " - Defence: " + defenceRank;
        pileTextActors[pileIndex] = new TextActor(text, Color.WHITE, bgColor, smallFont);
        addActor(pileTextActors[pileIndex], pileStatusLocations[pileIndex]);
    }

    private void updatePileRanks() {
        for (int j = 0; j < piles.length; j++) {
            int[] ranks = calculatePileRanks(j);
            updatePileRankState(j, ranks[ATTACK_RANK_INDEX], ranks[DEFENCE_RANK_INDEX]);
        }
    }

    private int getPlayerIndex(int index) {
        return index % nbPlayers;
    }

    private void playHeartForCharacters() {
        // 1: play the first 2 hearts
        nextStartingPlayer = -1;
        if (isAuto) {
            if (firstPlayers.size() >= currentPlay) {
                nextStartingPlayer = firstPlayers.get(currentPlay);
            }
        }

        if (nextStartingPlayer < 0) {
            nextStartingPlayer = getPlayerIndex(nextStartingPlayer);
            if (hands[nextStartingPlayer].getNumberOfCardsWithSuit(Suit.HEARTS) == 0)
                nextStartingPlayer = getPlayerIndex(nextStartingPlayer + 1);
        }

        assert hands[nextStartingPlayer].getNumberOfCardsWithSuit(Suit.HEARTS) != 0 : " Starting player has no hearts.";

        for (int i = 0; i < 2; i++) {
            int pileIndex = 0;
            selected = Optional.empty();

            int playerIndex = getPlayerIndex(nextStartingPlayer + i);
            setStatusText("Player " + playerIndex + " select a Heart card to play");
            if (isAuto) {
                if (playerAutoMovements.size() > currentPlay) {
                    List<List<String>> playersCards = playerAutoMovements.get(currentPlay);
                    if (playersCards.size() > playerIndex) {
                        List<String> movementStrings = playersCards.get(playerIndex);
                        Hand currentHand = hands[playerIndex];
                        int playerMovementIndex = playerMovementIndexes.get(playerIndex);
                        if (movementStrings.size() > playerMovementIndex) {
                            String movementString = movementStrings.get(playerMovementIndex);
                            String[] components = movementString.split("-");
                            String cardString = components[0];
                            pileIndex = Integer.parseInt(components[1]);
                            selected = Optional.ofNullable(getCardFromList(currentHand.getCardList(), cardString));
                            playerMovementIndexes.set(playerIndex, playerMovementIndex + 1);
                        }
                    }
                }
            }

            if (selected.isEmpty()) {
                pileIndex = i % 2;
                if (playerTypes[playerIndex] == PlayerType.HUMAN) {
                    waitForCorrectSuit(playerIndex, true);
                } else if (playerTypes[playerIndex] == PlayerType.SMART && smartBots[playerIndex] != null) {
                    selected = Optional.of(smartBots[playerIndex].selectCharacter(hands[playerIndex]));
                } else {
                    pickACorrectSuit(playerIndex, true);
                }
            }

            assert selected.isPresent() : " Pass returned on selection of character.";
            selected.get().setVerso(false);
            selected.get().transfer(piles[pileIndex], true); // transfer to pile (includes graphic effect)
            logger.logPlayerMovement(nextStartingPlayer + i, selected.get(), pileIndex);
            updatePileRanks();
        }
    }

    private void playTurns() {
        // 2: play the remaining nbPlayers * nbRounds - 2
        int remainingTurns = nbPlayers * nbRounds - 2;
        int nextPlayer = (nextStartingPlayer + 2) % nbPlayers;

        while (remainingTurns > 0) {
            boolean hasSelectedCard = false;
            if (isAuto) {
                List<List<String>> playersCards = playerAutoMovements.get(currentPlay);
                if (playersCards.size() > nextPlayer) {
                    List<String> movementStrings = playersCards.get(nextPlayer);
                    Hand currentHand = hands[nextPlayer];
                    int playerMovementIndex = playerMovementIndexes.get(nextPlayer);
                    if (movementStrings.size() > playerMovementIndex) {
                        String movementString = movementStrings.get(playerMovementIndex);
                        String[] components = movementString.split("-");
                        String cardString = components[0];
                        selectedPileIndex = Integer.parseInt(components[1]);
                        selected = Optional.ofNullable(getCardFromList(currentHand.getCardList(), cardString));
                        setStatusText("Selected: " + canonical(selected.get()) + ". Player" + nextPlayer +
                                " select a pile " + selectedPileIndex + " to play the card.");
                        playerMovementIndexes.set(nextPlayer, playerMovementIndex + 1);
                        hasSelectedCard = true;
                    }
                }
            }

            if (!hasSelectedCard || selected.isEmpty()) {
                nextPlayer = getPlayerIndex(nextPlayer);
                setStatusText("Player" + nextPlayer + " select a non-Heart card to play.");

                // Smart bot picks both card and pile
                if (playerTypes[nextPlayer] == PlayerType.SMART && smartBots[nextPlayer] != null) {
                    Move m = smartBots[nextPlayer].selectMove(hands[nextPlayer], piles);
                    if (m.isPass()) {
                        selected = Optional.empty();
                        System.out.println(". Player" + nextPlayer + " Pass.");
                        setStatusText("Pass.");
                    } else {
                        selected = Optional.of(m.getCard());
                        selectedPileIndex = m.getPileIndex();
                        setStatusText("Selected: " + canonical(selected.get()) + ". Player" + nextPlayer
                                + " plays on pile " + selectedPileIndex + ".");
                    }

                    // Legal bot reviews the random card + pile and may override
                    if (playerTypes[nextPlayer] == PlayerType.LEGAL && legalBots[nextPlayer] != null) {
                        int ownPileIndex = nextPlayer % 2;
                        Decision d = legalBots[nextPlayer].decideMove(
                                selected.get(), selectedPileIndex, ownPileIndex, piles);
                        if (d == Decision.PASS) {
                            selected = Optional.empty();
                            System.out.println(". Player" + nextPlayer + " Pass.");
                            setStatusText("Pass.");
                        }
                    }
                } else {
                    if (playerTypes[nextPlayer] == PlayerType.HUMAN) {
                        waitForCorrectSuit(nextPlayer, false);
                    } else {
                        pickACorrectSuit(nextPlayer, false);
                    }

                    if (selected.isPresent()) {
                        setStatusText("Selected: " + canonical(selected.get()) + ". Player" + nextPlayer
                                + " select a pile to play the card.");
                        if (playerTypes[nextPlayer] == PlayerType.HUMAN) {
                            waitForPileSelection();
                        } else {
                            selectRandomPile();
                        }

                        // Legal bot reviews the random card + pile and may override
                        if (playerTypes[nextPlayer] == PlayerType.LEGAL && legalBots[nextPlayer] != null) {
                            int ownPileIndex = nextPlayer % 2;
                            Decision d = legalBots[nextPlayer].decideMove(
                                    selected.get(), selectedPileIndex, ownPileIndex, piles);
                            if (d == Decision.PASS) {
                                selected = Optional.empty();
                                System.out.println(". Player" + nextPlayer + " Pass.");
                                setStatusText("Pass.");
                            }
                        }
                    } else {
                        System.out.println(". Player" + nextPlayer + "Pass.");
                        setStatusText("Pass.");
                    }
                }
            }

            if (selected.isPresent()) {
                selected.get().setVerso(false);
                selected.get().transfer(piles[selectedPileIndex], true); // transfer to pile (includes graphic effect)
                updatePileRanks();
                logger.logPlayerMovement(nextPlayer, selected.get(), selectedPileIndex);
            }

            nextPlayer = (nextPlayer + 1) % nbPlayers;
            remainingTurns--;
        }
    }

    private void updateScoreForPlayers(int[] pileNorthRanks, int[] pileSouthRanks) {
        System.out.println("pile north: " + canonical(piles[Pile.NORTH.ordinal()]));
        System.out.println("pile north is " + "Attack: " + pileNorthRanks[ATTACK_RANK_INDEX] + " - Defence: "
                + pileNorthRanks[DEFENCE_RANK_INDEX]);
        System.out.println("pile south: " + canonical(piles[Pile.SOUTH.ordinal()]));
        System.out.println("pile south is " + "Attack: " + pileSouthRanks[ATTACK_RANK_INDEX] + " - Defence: "
                + pileSouthRanks[DEFENCE_RANK_INDEX]);
        Rank pileNorthCharacterRank = (Rank) piles[Pile.NORTH.ordinal()].getCardList().get(0).getRank();
        Rank pileSouthCharacterRank = (Rank) piles[Pile.SOUTH.ordinal()].getCardList().get(0).getRank();
        String character0Result;
        String character1Result;

        if (pileNorthRanks[ATTACK_RANK_INDEX] > pileSouthRanks[DEFENCE_RANK_INDEX]) {
            scores[getPlayerIndex(nextStartingPlayer)] += pileSouthCharacterRank.getScoreValue();
            scores[getPlayerIndex(nextStartingPlayer + 2)] += pileSouthCharacterRank.getScoreValue();
            character0Result = "Character 0 attack on character 1 succeeded.";
        } else {
            scores[getPlayerIndex(nextStartingPlayer + 1)] += pileSouthCharacterRank.getScoreValue();
            scores[getPlayerIndex(nextStartingPlayer + 3)] += pileSouthCharacterRank.getScoreValue();
            character0Result = "Character 0 attack on character 1 failed.";
        }

        if (pileSouthRanks[ATTACK_RANK_INDEX] > pileNorthRanks[DEFENCE_RANK_INDEX]) {
            scores[getPlayerIndex(nextStartingPlayer + 1)] += pileNorthCharacterRank.getScoreValue();
            scores[getPlayerIndex(nextStartingPlayer + 3)] += pileNorthCharacterRank.getScoreValue();
            character1Result = "Character 1 attack on character 0 succeeded.";
        } else {
            scores[getPlayerIndex(nextStartingPlayer)] += pileNorthCharacterRank.getScoreValue();
            scores[getPlayerIndex(nextStartingPlayer + 2)] += pileNorthCharacterRank.getScoreValue();
            character1Result = "Character 1 attack character 0 failed.";
        }
        updateScores();
        System.out.println(character0Result);
        System.out.println(character1Result);
        setStatusText(character0Result + " " + character1Result);
    }

    private void executeAPlay() {
        resetPile();
        resetIndexes();

        // Clear smart bot flags at the start of each play
        for (SmartBotPlayer s : smartBots) {
            if (s != null)
                s.resetForNewPlay();
        }

        playHeartForCharacters();
        playTurns();

        // 3: calculate winning & update scores for players
        updatePileRanks();
        for (int i = 0; i < piles.length; i++) {
            logger.logPileCards(piles[i], Pile.values()[i]);
        }

        int[] pileNorthRanks = calculatePileRanks(Pile.NORTH.ordinal());
        int[] pileSouthRanks = calculatePileRanks(Pile.SOUTH.ordinal());
        updateScoreForPlayers(pileNorthRanks, pileSouthRanks);
        logger.logScores(pileNorthRanks, pileSouthRanks, scores);

        nextStartingPlayer += 1;
        currentPlay++;
        delay(watchingTime);
    }

    private int getWatchingTime() {
        if (isAuto) {
            return 100;
        } else {
            return 500;
        }
    }
}
