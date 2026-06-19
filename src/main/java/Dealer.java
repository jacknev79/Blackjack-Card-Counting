import java.util.ArrayList;

public class Dealer {
    Deck deck;
    Hand hand;
    ArrayList<Player> players;
    int shoeSize;
    int runningCount;
    int trueCount;
    boolean h17;

    // Surrender Flags
    boolean earlySurrender;
    boolean lateSurrender;

    public Dealer(ArrayList<Player> players, GameConfig config) {
        this.players = players;
        this.shoeSize = config.shoeSize();
        this.runningCount = 0;
        this.trueCount = 0;
        this.h17 = config.hitSoft17();

        // Initialize flags from config
        this.earlySurrender = config.earlySurrender();
        this.lateSurrender = config.lateSurrender();

        this.deck = new Deck(this.shoeSize);
    }

    void deal() {
        Card dcard = this.deck.takeCard();
        Card dhole = this.deck.takeCard();
        this.hand = new Hand(dcard, dhole, 0);
        this.runningCount += cardCount(dcard) + cardCount(dhole);

        for (Player player : this.players) {
            int bet = player.enterBet(getTrueCount());
            Card card = this.deck.takeCard();
            Card hole = this.deck.takeCard();
            Hand phand = new Hand(card, hole, bet);
            this.runningCount += cardCount(card) + cardCount(hole);
            player.addHand(phand);
            player.setBet(bet);
        }

        // 1. EARLY SURRENDER PHASE
        // Happens immediately after cards are dealt, BEFORE insurance or Blackjack checks.
        if (this.earlySurrender) {
            processSurrenderPhase();
        }

        // 2. INSURANCE PHASE
        if (this.hand.card.getRank() == 1) {
            ArrayList<Player> insured = new ArrayList<>();
            for (Player player : this.players) {
                boolean check = player.takeInsurance(getTrueCount());
                if (check) insured.add(player);
            }
            if (!hasBlackjack() && !insured.isEmpty()) {
                for (Player player : insured) {
                    player.winnings -= 0.5 * player.bet;
                }
            } else if (hasBlackjack() && !insured.isEmpty()) {
                for (Player player : insured) {
                    player.winnings += player.getBet();
                }
            }
        }
    }

    /**
     * LATE SURRENDER PHASE
     * This is separate from deal() because standard Late Surrender can ONLY occur
     * after the dealer checks the hole card and confirms they DO NOT have Blackjack.
     */
    public void processLateSurrenderPhase() {
        if (this.lateSurrender && !hasBlackjack()) {
            processSurrenderPhase();
        }
    }

    /**
     * Helper method to trigger the surrender check for players/bots.
     */
    private void processSurrenderPhase() {
        for (Player player : this.players) {
            // Ideally, add checkSurrender(Dealer dealer) to the base Player class.
            // For now, we cast to BlackjackBot to hit the strategy logic.
            if (player instanceof BlackjackBot bot) {
                bot.checkSurrender(this);
            }
        }
    }

    int play() {
        // Keep hitting if total is under 17, OR if it's a soft 17 and H17 rule is active
        while (this.hand.getScore() < 17 || (this.hand.getScore() == 17 && this.h17 && isSoft())) {
            Card card = this.deck.takeCard();
            this.hand.hit(card);
            this.runningCount += cardCount(card);
        }
        return this.hand.getScore();
    }

    boolean hasBlackjack() {
        // FIXED: Universal Blackjack check. Hand size must be exactly 2 and score exactly 21.
        return this.hand.getScore() == 21 && this.hand.getHand().size() == 2;
    }

    public Card takeCard() {
        if (this.deck.getDeck().isEmpty()) return null;
        Card card = this.deck.takeCard();
        this.runningCount += cardCount(card);
        return card;
    }

    private int cardCount(Card c) {
        if (c.getRank() == 1 || c.getRank() == 10) return -1;
        if (c.getRank() < 7) return 1;
        return 0;
    }

    public int getTrueCount() {
        if (this.deck.getDeck().size() < 52) return runningCount;
        return runningCount / (this.deck.getDeck().size() / 52);
    }

    public boolean isSoft() {
        int hardScore = 0;
        boolean hasAce = false;

        for (Card card : this.hand.getHand()) {
            if (card.getRank() == 1) {
                hasAce = true;
                hardScore += 1;
            } else {
                hardScore += card.getRank();
            }
        }
        return hasAce && (hardScore + 10 <= 21);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int upCard() {
        return this.hand.getCard().getRank();
    }
}