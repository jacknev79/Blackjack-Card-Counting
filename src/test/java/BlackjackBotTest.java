import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class BlackjackBotTest {
    private BlackjackBot bot;
    private Dealer dealer;

    @BeforeEach
    void setUp() {
        GameConfig config = new GameConfig(1, 10, 1, 1, false,true, true);
        BlackjackStrategyLoader.initialize(config);

        bot = new BlackjackBot(1);
        ArrayList<Player> players = new ArrayList<>();
        players.add(bot);
        dealer = new Dealer(players, config);
    }

    @Test
    void testEnterBetScalesWithTrueCount() {
        assertEquals(25, bot.enterBet(0), "Bet should be minimum if TC <= 0");
        assertEquals(25, bot.enterBet(-2), "Bet should be minimum if TC <= 0");
        assertEquals(300, bot.enterBet(3), "Bet should scale 100 * TC");
        assertEquals(1600, bot.enterBet(6), "Bet should cap logic at TC > 5 (1000 + 100 * TC)");
    }

    @Test
    void testTakeInsuranceDependsOnTrueCount() {
        assertTrue(bot.takeInsurance(3), "Bot should take insurance if TC >= 3");
        assertFalse(bot.takeInsurance(2), "Bot should NOT take insurance if TC < 3");
    }

    @Test
    void testGetScoresExceptFirstAce() {
        Hand hand = new Hand(new Card('H', "A"), new Card('C', "5"), 0); // A + 5
        hand.hit(new Card('D', "A")); // A + 5 + A

        int scoreWithoutFirstAce = bot.getScoresExceptFirstAce(hand);
        assertEquals(6, scoreWithoutFirstAce, "Should sum 5 + 1 (the second Ace), skipping first Ace");
    }

    @Test
    void testFallbackPlayStrategy_HitsUnder17() {
        // Without mocked files, the strategy loader falls back to move = (score >= 17) ? "S" : "H"
        Hand botHand = new Hand(new Card('H', "10"), new Card('C', "6"), 100); // 16
        bot.addHand(botHand);

        dealer.hand = new Hand(new Card('H', "10"), new Card('D', "2"), 0);

        // Stack deck with a card that won't bust it over 21
        dealer.deck.getDeck().clear();
        dealer.deck.getDeck().add(new Card('S', "5")); // Drawn during hit
        dealer.deck.getDeck().add(new Card('S', "10")); // Failsafe

        bot.play(dealer);

        assertEquals(2, bot.getHands().size(), "Should still be one hand (no split)");
        assertEquals(3, bot.getHands().get(0).getHand().size(), "Bot should have hit on 16 to get 3 cards");
        assertEquals(21, bot.getHands().get(0).getScore(), "16 + 5 = 21");
    }

    @Test
    void testFallbackPlayStrategy_StandsOn17AndAbove() {
        Hand botHand = new Hand(new Card('H', "10"), new Card('C', "7"), 100); // 17
        bot.addHand(botHand);

        dealer.hand = new Hand(new Card('H', "10"), new Card('D', "2"), 0);

        int initialDeckSize = dealer.deck.getDeck().size();
        bot.play(dealer);

        assertEquals(17, bot.getHands().get(0).getScore(), "Bot should stand on 17");
        assertEquals(initialDeckSize, dealer.deck.getDeck().size(), "No cards should be drawn");
    }
}