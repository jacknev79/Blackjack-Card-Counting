import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class GameTest {
    private Game game;
    private BlackjackBot bot;
    private Dealer dealer;
    private Method resolveRoundMethod;

    @BeforeEach
    void setUp() throws Exception {
        GameConfig config = new GameConfig(1, 10, 1, 1, false,true, true);
        BlackjackStrategyLoader.initialize(config);

        bot = new BlackjackBot(1);
        ArrayList<Player> players = new ArrayList<>();
        players.add(bot);

        game = new Game(1, players, config);

        // Use reflection to access the private resolveRound method
        resolveRoundMethod = Game.class.getDeclaredMethod("resolveRound", int.class);
        resolveRoundMethod.setAccessible(true);

        // Setup a dummy dealer hand to prevent null pointers
        java.lang.reflect.Field dealerField = Game.class.getDeclaredField("dealer");
        dealerField.setAccessible(true);
        dealer = (Dealer) dealerField.get(game);
        dealer.hand = new Hand(new Card('H', "10"), new Card('C', "10"), 0); // Default 20
    }

    private void executeResolveRound(int dealerScore) throws Exception {
        resolveRoundMethod.invoke(game, dealerScore);
    }

    @Test
    void testPlayerBlackjackPayout() throws Exception {
        Hand botHand = new Hand(new Card('S', "A"), new Card('H', "K"), 100);
        bot.addHand(botHand);

        executeResolveRound(20);

        // 1.5 * 100 bet = 150 winnings
        assertEquals(150, bot.getWinnings(), "Player should win 1.5x on Blackjack");
    }

    @Test
    void testPlayerBustLosesBet() throws Exception {
        Hand botHand = new Hand(new Card('S', "10"), new Card('H', "10"), 100);
        botHand.hit(new Card('C', "5")); // Busts (25)
        bot.addHand(botHand);

        executeResolveRound(20);

        assertEquals(-100, bot.getWinnings(), "Player should lose bet on bust");
    }

    @Test
    void testDealerBustPlayerWins() throws Exception {
        Hand botHand = new Hand(new Card('S', "10"), new Card('H', "5"), 100); // 15
        bot.addHand(botHand);

        // Modify dealer to bust
        dealer.hand = new Hand(new Card('D', "10"), new Card('D', "6"), 0);
        dealer.hand.hit(new Card('D', "10")); // Dealer 26 (Bust)

        executeResolveRound(26);

        assertEquals(100, bot.getWinnings(), "Player wins normal bet when dealer busts");
    }

    @Test
    void testPushLeavesWinningsUnchanged() throws Exception {
        Hand botHand = new Hand(new Card('S', "10"), new Card('H', "8"), 100); // 18
        bot.addHand(botHand);

        executeResolveRound(18);

        assertEquals(0, bot.getWinnings(), "Winnings should remain 0 on a push");
    }

    @Test
    void testDealerBlackjackBeatsPlayer21() throws Exception {
        // Player has 21 (3 cards)
        Hand botHand = new Hand(new Card('S', "10"), new Card('H', "6"), 100);
        botHand.hit(new Card('D', "5"));
        bot.addHand(botHand);

        // Dealer has Blackjack
        dealer.hand = new Hand(new Card('C', "A"), new Card('S', "K"), 0);

        executeResolveRound(21);

        assertEquals(-100, bot.getWinnings(), "Player multi-card 21 loses to Dealer Blackjack");
    }

    @Test
    void testPlayerBlackjackPushesDealerBlackjack() throws Exception {
        // Player Blackjack
        Hand botHand = new Hand(new Card('S', "A"), new Card('H', "K"), 100);
        bot.addHand(botHand);

        // Dealer Blackjack
        dealer.hand = new Hand(new Card('C', "A"), new Card('S', "K"), 0);

        executeResolveRound(21);

        assertEquals(0, bot.getWinnings(), "Player BJ and Dealer BJ should Push");
    }
}