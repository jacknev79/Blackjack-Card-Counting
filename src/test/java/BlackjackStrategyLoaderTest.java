import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class BlackjackStrategyLoaderTest {

    @BeforeEach
    public void resetSingletonBeforeEachTest() throws Exception {
        Field instanceField = BlackjackStrategyLoader.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    // ===================================================================================
    // EXISTING TESTS (Migrated to JUnit 5)
    // ===================================================================================

    @Test
    public void testGetInstanceThrowsExceptionWhenUninitialized() {
        // JUnit 5 style exception tracking using assertThrows
        assertThrows(IllegalStateException.class, () -> {
            BlackjackStrategyLoader.getInstance();
        });
    }

    @Test
    public void testSingletonIdentity() {
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);

        BlackjackStrategyLoader instance1 = BlackjackStrategyLoader.getInstance();
        BlackjackStrategyLoader instance2 = BlackjackStrategyLoader.getInstance();

        // In JUnit 5, the message string goes LAST
        assertSame(instance1, instance2, "getInstance() must always point to the exact same memory reference");
    }

    @Test
    public void testHardcodedDeviationsLoading() {
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        assertNotNull(loader.getDeviationStrategies()[4], "True Count 4 strategy map should be initialized");

        String move = loader.getDeviationStrategies()[4].get("10").get("15");
        assertEquals("S", move, "At True Count 4 against a 10 upcard, a player total of 15 should Stand");
    }

    @Test
    public void testParseMove_StandardS17vsH17Splits() throws Exception {
        GameConfig h17Config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(h17Config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        Method parseMoveMethod = BlackjackStrategyLoader.class.getDeclaredMethod("parseMove", String.class);
        parseMoveMethod.setAccessible(true);

        Object resultH17 = parseMoveMethod.invoke(loader, "S | H");
        String standardMoveH17 = getStandardMoveFromRecord(resultH17);
        assertEquals("H", standardMoveH17, "Should choose the second move (index 1) when hitSoft17 is TRUE");

        resetSingletonBeforeEachTest();
        GameConfig s17Config = new GameConfig(1, 75, 6, 1000000, true, false,false);
        BlackjackStrategyLoader.initialize(s17Config);
        loader = BlackjackStrategyLoader.getInstance();

        Object resultS17 = parseMoveMethod.invoke(loader, "S | H");
        String standardMoveS17 = getStandardMoveFromRecord(resultS17);
        assertEquals("S", standardMoveS17, "Should choose the first move (index 0) when hitSoft17 is FALSE");
    }

    @Test
    public void testParseMove_SurrenderEdgeCases() throws Exception {
        GameConfig surrenderDisabled = new GameConfig(1, 75, 6, 1000000, false, false,true);
        BlackjackStrategyLoader.initialize(surrenderDisabled);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        Method parseMoveMethod = BlackjackStrategyLoader.class.getDeclaredMethod("parseMove", String.class);
        parseMoveMethod.setAccessible(true);

        Object res1 = parseMoveMethod.invoke(loader, "H | SURR");
        assertFalse(getShouldSurrenderFromRecord(res1), "Surrender flag must be false if lateSurrender is turned off in config");
        assertEquals("H", getStandardMoveFromRecord(res1), "The fallback move should still extract cleanly");

        resetSingletonBeforeEachTest();
        GameConfig surrenderEnabled = new GameConfig(1, 75, 6, 1000000, true, false,false);
        BlackjackStrategyLoader.initialize(surrenderEnabled);
        loader = BlackjackStrategyLoader.getInstance();

        Object res2 = parseMoveMethod.invoke(loader, "H | SURR");
        assertTrue(getShouldSurrenderFromRecord(res2), "Universal SURR token should trigger true under standard S17");
        assertEquals("H", getStandardMoveFromRecord(res2), "Fallback play path must be mapped");

        Object res3 = parseMoveMethod.invoke(loader, "H | SURR_H17");
        assertFalse(getShouldSurrenderFromRecord(res3), "SURR_H17 should resolve to false if game rules run on S17");

        resetSingletonBeforeEachTest();
        GameConfig h17Surrender = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(h17Surrender);
        loader = BlackjackStrategyLoader.getInstance();

        Object res4 = parseMoveMethod.invoke(loader, "H | SURR_H17");
        assertTrue(getShouldSurrenderFromRecord(res4), "SURR_H17 should resolve to true if game rules run on H17");
    }

    @Test
    public void testParseMove_ComplexThreeTierSplit() throws Exception {
        GameConfig s17Config = new GameConfig(1, 75, 6, 1000000, true, false,false);
        BlackjackStrategyLoader.initialize(s17Config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        Method parseMoveMethod = BlackjackStrategyLoader.class.getDeclaredMethod("parseMove", String.class);
        parseMoveMethod.setAccessible(true);

        Object s17Result = parseMoveMethod.invoke(loader, "S | H | SURR_H17");
        assertEquals("S", getStandardMoveFromRecord(s17Result), "S17 configuration must extract the first index option");
        assertFalse(getShouldSurrenderFromRecord(s17Result), "S17 configuration must skip H17 conditional surrender rules");

        resetSingletonBeforeEachTest();
        GameConfig h17Config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(h17Config);
        loader = BlackjackStrategyLoader.getInstance();

        Object h17Result = parseMoveMethod.invoke(loader, "S | H | SURR_H17");
        assertEquals("H", getStandardMoveFromRecord(h17Result), "H17 configuration must extract the second index option");
        assertTrue(getShouldSurrenderFromRecord(h17Result), "H17 configuration must execute active conditional surrender rules");
    }

    // ===================================================================================
    // NEW TESTS: CLASSPATH LOADING & STRATEGY LOOKUP
    // ===================================================================================

    @Test
    public void testStrategyMapsAreLoadedFromClasspath() {
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        assertFalse(loader.getHardStrategy().isEmpty(), "Hard strategy map should not be empty");
        assertFalse(loader.getSoftStrategy().isEmpty(), "Soft strategy map should not be empty");
        assertFalse(loader.getSplitStrategy().isEmpty(), "Split strategy map should not be empty");

        assertTrue(loader.getHardStrategy().containsKey("10"), "Should contain dealer upcard 10");
        assertTrue(loader.getSoftStrategy().containsKey("1"), "Should contain dealer upcard A");
        assertTrue(loader.getSplitStrategy().containsKey("2"), "Should contain dealer upcard 2");
    }

    @Test
    public void testLoadPropertiesFromClasspath_MissingResourceHandlesGracefully() throws Exception {
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        Method loadPropertiesMethod = BlackjackStrategyLoader.class.getDeclaredMethod("loadPropertiesFromClasspath", String.class, String.class);
        loadPropertiesMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        HashMap<String, String> result = (HashMap<String, String>) loadPropertiesMethod.invoke(loader, "/hard/non_existent_upcard.properties", "99");

        assertNotNull(result, "Method should return an empty map, not null, when stream is null");
        assertTrue(result.isEmpty(), "Returned map should be empty when resource is not found");
    }

    @Test
    public void testStrategyLookup_HardHand() {
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        // Print loaded keys if test fails
        System.out.println("DEBUG - Loaded keys for upcard 6: " + loader.getHardStrategy().get("6").keySet());

        String playerHand = "11";
        String dealerUpcard = "6";
        String expectedMove = "D";

        String actualMove = loader.getHardStrategy().get(dealerUpcard).get(playerHand);

        assertNotNull(actualMove, "Move should exist for Hard " + playerHand + " vs " + dealerUpcard);
        assertEquals(expectedMove, actualMove, "Incorrect move loaded for Hard " + playerHand + " vs " + dealerUpcard);
    }

    @Test
    public void testStrategyLookup_SoftHand() {
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        String playerHand = "6";
        String dealerUpcard = "2";
        String expectedMove = "H";

        String actualMove = loader.getSoftStrategy().get(dealerUpcard).get(playerHand);

        assertNotNull(actualMove, "Move should exist for Soft " + playerHand + " vs " + dealerUpcard);
        assertEquals(expectedMove, actualMove, "Incorrect move loaded for Soft " + playerHand + " vs " + dealerUpcard);
    }

    @Test
    public void testStrategyLookup_SplitHand() {
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        String playerHand = "8";
        String dealerUpcard = "10";
        String expectedMove = "split";

        String actualMove = loader.getSplitStrategy().get(dealerUpcard).get(playerHand);

        assertNotNull(actualMove, "Move should exist for Split " + playerHand + " vs " + dealerUpcard);
        assertEquals(expectedMove, actualMove, "Incorrect move loaded for Split " + playerHand + " vs " + dealerUpcard);
    }

    @Test
    public void testSurrenderLookup_SafeHandlingOfMissingKeys() {
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        boolean shouldSurrenderBadDealer = loader.shouldSurrender("INVALID_DEALER", "16");
        assertFalse(shouldSurrenderBadDealer, "Should return false for non-existent dealer upcard");

        boolean shouldSurrenderBadPlayer = loader.shouldSurrender("10", "INVALID_PLAYER");
        assertFalse(shouldSurrenderBadPlayer, "Should return false for non-existent player hand");
    }

    @Test
    public void testSurrenderLookup_ValidSurrenderScenario() {
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        String playerHand = "16";
        String dealerUpcard = "10";

        boolean surrenders = loader.shouldSurrender(dealerUpcard, playerHand);
        assertTrue(surrenders, "Hard " + playerHand + " vs " + dealerUpcard + " should trigger a surrender");
    }

    private String getStandardMoveFromRecord(Object recordInstance) throws Exception {
        Method method = recordInstance.getClass().getMethod("standardMove");
        return (String) method.invoke(recordInstance);
    }

    private boolean getShouldSurrenderFromRecord(Object recordInstance) throws Exception {
        Method method = recordInstance.getClass().getMethod("shouldSurrender");
        return (boolean) method.invoke(recordInstance);
    }
}