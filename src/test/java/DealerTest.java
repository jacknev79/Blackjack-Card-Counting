import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class DealerTest {
    private Dealer dealer;
    private ArrayList<Player> players;
    private GameConfig config;

    @BeforeEach
    void setUp() {
        config = new GameConfig(1, 0, 1, 1, false,true, true);
        BlackjackStrategyLoader.initialize(config);
        players = new ArrayList<>();
        players.add(new BlackjackBot(1));
        // Config: 1 player, 0 pen, 1 shoe, 1 game, surrender true, H17 true
        dealer = new Dealer(players, config);
    }

    private void stackDeckBackwards(Card... cards) {
        dealer.deck.getDeck().clear();
        // Deck uses removeLast(), so first card to be drawn must be at the end
        for (int i = cards.length - 1; i >= 0; i--) {
            dealer.deck.getDeck().add(cards[i]);
        }
    }

    @Test
    void testDealDistributesCardsCorrectly() {
        // Dealer up, dealer hole, player card, player hole
        stackDeckBackwards(
                new Card('H', "10"), new Card('C', "9"), // Dealer: 19
                new Card('S', "8"), new Card('D', "8")   // Player: 16
        );

        dealer.deal();

        assertEquals(19, dealer.hand.getScore(), "Dealer should have 19");
        assertEquals("10", dealer.hand.getCard().rank, "Dealer upcard should be 10");
        assertEquals(16, players.get(0).getHands().get(0).getScore(), "Player should have 16");
    }

    @Test
    void testCardCountingLogic() {
        dealer.deck.getDeck().clear();

        // High cards (-1)
        dealer.deck.getDeck().add(new Card('H', "A"));
        dealer.deck.getDeck().add(new Card('S', "K"));
        // Neutral cards (0)
        dealer.deck.getDeck().add(new Card('C', "8"));
        // Low cards (+1)
        dealer.deck.getDeck().add(new Card('D', "2"));
        dealer.deck.getDeck().add(new Card('H', "6"));

        // Simulate drawing them one by one
        while (!dealer.deck.getDeck().isEmpty()) {
            dealer.takeCard();
        }

        // Calculation: A(-1) + K(-1) + 8(0) + 2(+1) + 6(+1) = 0
        assertEquals(0, dealer.runningCount, "Running count should equal 0");
    }

    @Test
    void testIsSoft() {
        dealer.hand = new Hand(new Card('H', "A"), new Card('S', "6"), 0);
        assertTrue(dealer.isSoft(), "A + 6 is a soft 17");

        dealer.hand.hit(new Card('D', "10")); // Score becomes 17 (Hard)
        assertFalse(dealer.isSoft(), "A + 6 + 10 is a hard 17");
    }

    @Test
    void testDealerHitsSoft17() {
        // H17 is true in config
        dealer.hand = new Hand(new Card('H', "A"), new Card('S', "6"), 0); // Soft 17

        stackDeckBackwards(new Card('C', "2")); // Next card is a 2

        int finalScore = dealer.play();

        assertEquals(19, finalScore, "Dealer should hit soft 17 and get 19");
    }

    @Test
    void testDealerStandsHard17() {
        dealer.hand = new Hand(new Card('H', "10"), new Card('S', "7"), 0); // Hard 17
        stackDeckBackwards(new Card('C', "2")); // Shouldn't be drawn

        int finalScore = dealer.play();

        assertEquals(17, finalScore, "Dealer should stand on hard 17");
        assertFalse(dealer.deck.getDeck().isEmpty(), "Deck should not be empty, dealer stood");
    }

    @Test
    void testHasBlackjack() {
        dealer.hand = new Hand(new Card('H', "A"), new Card('S', "K"), 0);
        assertTrue(dealer.hasBlackjack(), "A + K is Blackjack");

        dealer.hand = new Hand(new Card('H', "10"), new Card('S', "10"), 0);
        assertFalse(dealer.hasBlackjack(), "10 + 10 is 20, not Blackjack");
    }
}