import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

public class BlackjackStrategyLoaderTest {

    @Before
    public void resetSingletonBeforeEachTest() throws Exception {
        // Uses reflection to reset the private static instance to null.
        // This allows us to test multiple GameConfig setups in a single run.
        Field instanceField = BlackjackStrategyLoader.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetInstanceThrowsExceptionWhenUninitialized() {
        // JUnit 4 style exception tracking
        BlackjackStrategyLoader.getInstance();
    }

    @Test
    public void testSingletonIdentity() {
        // Added numGames (1000000) after shoeSize
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);

        BlackjackStrategyLoader instance1 = BlackjackStrategyLoader.getInstance();
        BlackjackStrategyLoader instance2 = BlackjackStrategyLoader.getInstance();

        // In JUnit 4, the message string goes FIRST
        assertSame("getInstance() must always point to the exact same memory reference", instance1, instance2);
    }

    @Test
    public void testHardcodedDeviationsLoading() {
        // Added numGames (1000000) after shoeSize
        GameConfig config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        // Verify that standard structural deviations are preserved at the correct indices
        assertNotNull("True Count 4 strategy map should be initialized", loader.getDeviationStrategies()[4]);

        String move = loader.getDeviationStrategies()[4].get("10").get("15");
        assertEquals("At True Count 4 against a 10 upcard, a player total of 15 should Stand", "S", move);
    }

    @Test
    public void testParseMove_StandardS17vsH17Splits() throws Exception {
        // Setup H17 Environment - Added numGames (1000000) after shoeSize
        GameConfig h17Config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(h17Config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        // Access private parseMove method for advanced edge-case verification
        Method parseMoveMethod = BlackjackStrategyLoader.class.getDeclaredMethod("parseMove", String.class);
        parseMoveMethod.setAccessible(true);

        // Test split options: S17 action | H17 action
        Object resultH17 = parseMoveMethod.invoke(loader, "S | H");
        String standardMoveH17 = getStandardMoveFromRecord(resultH17);
        assertEquals("Should choose the second move (index 1) when hitSoft17 is TRUE", "H", standardMoveH17);

        // Reset and test S17 Environment - Added numGames (1000000) after shoeSize
        resetSingletonBeforeEachTest();
        GameConfig s17Config = new GameConfig(1, 75, 6, 1000000, true, false,false);
        BlackjackStrategyLoader.initialize(s17Config);
        loader = BlackjackStrategyLoader.getInstance();

        Object resultS17 = parseMoveMethod.invoke(loader, "S | H");
        String standardMoveS17 = getStandardMoveFromRecord(resultS17);
        assertEquals("Should choose the first move (index 0) when hitSoft17 is FALSE", "S", standardMoveS17);
    }

    @Test
    public void testParseMove_SurrenderEdgeCases() throws Exception {
        // Case A: Late Surrender completely disabled in config - Added numGames (1000000) after shoeSize
        GameConfig surrenderDisabled = new GameConfig(1, 75, 6, 1000000, false, false,true);
        BlackjackStrategyLoader.initialize(surrenderDisabled);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        Method parseMoveMethod = BlackjackStrategyLoader.class.getDeclaredMethod("parseMove", String.class);
        parseMoveMethod.setAccessible(true);

        Object res1 = parseMoveMethod.invoke(loader, "H | SURR");
        assertFalse("Surrender flag must be false if lateSurrender is turned off in config", getShouldSurrenderFromRecord(res1));
        assertEquals("The fallback move should still extract cleanly", "H", getStandardMoveFromRecord(res1));

        // Case B: Universal Surrender Enabled (Applies to both S17 and H17) - Added numGames (1000000) after shoeSize
        resetSingletonBeforeEachTest();
        GameConfig surrenderEnabled = new GameConfig(1, 75, 6, 1000000, true, false,false); // S17 engine
        BlackjackStrategyLoader.initialize(surrenderEnabled);
        loader = BlackjackStrategyLoader.getInstance();

        Object res2 = parseMoveMethod.invoke(loader, "H | SURR");
        assertTrue("Universal SURR token should trigger true under standard S17", getShouldSurrenderFromRecord(res2));
        assertEquals("Fallback play path must be mapped", "H", getStandardMoveFromRecord(res2));

        // Case C: H17-Only Surrender token ("SURR_H17") under an S17 config
        Object res3 = parseMoveMethod.invoke(loader, "H | SURR_H17");
        assertFalse("SURR_H17 should resolve to false if game rules run on S17", getShouldSurrenderFromRecord(res3));

        // Case D: H17-Only Surrender token ("SURR_H17") under an H17 config - Added numGames (1000000) after shoeSize
        resetSingletonBeforeEachTest();
        GameConfig h17Surrender = new GameConfig(1, 75, 6, 1000000, true, false,true); // H17 engine
        BlackjackStrategyLoader.initialize(h17Surrender);
        loader = BlackjackStrategyLoader.getInstance();

        Object res4 = parseMoveMethod.invoke(loader, "H | SURR_H17");
        assertTrue("SURR_H17 should resolve to true if game rules run on H17", getShouldSurrenderFromRecord(res4));
    }

    @Test
    public void testParseMove_ComplexThreeTierSplit() throws Exception {
        // Tests the combination string: "S | H | SURR_H17"
        // Target outcome: S17 system -> Stand, No Surrender. H17 system -> Hit, Surrender.

        // Scenario 1: S17 Game Mode - Added numGames (1000000) after shoeSize
        GameConfig s17Config = new GameConfig(1, 75, 6, 1000000, true, false,false);
        BlackjackStrategyLoader.initialize(s17Config);
        BlackjackStrategyLoader loader = BlackjackStrategyLoader.getInstance();

        Method parseMoveMethod = BlackjackStrategyLoader.class.getDeclaredMethod("parseMove", String.class);
        parseMoveMethod.setAccessible(true);

        Object s17Result = parseMoveMethod.invoke(loader, "S | H | SURR_H17");
        assertEquals("S17 configuration must extract the first index option", "S", getStandardMoveFromRecord(s17Result));
        assertFalse("S17 configuration must skip H17 conditional surrender rules", getShouldSurrenderFromRecord(s17Result));

        // Scenario 2: H17 Game Mode - Added numGames (1000000) after shoeSize
        resetSingletonBeforeEachTest();
        GameConfig h17Config = new GameConfig(1, 75, 6, 1000000, true, false,true);
        BlackjackStrategyLoader.initialize(h17Config);
        loader = BlackjackStrategyLoader.getInstance();

        Object h17Result = parseMoveMethod.invoke(loader, "S | H | SURR_H17");
        assertEquals("H17 configuration must extract the second index option", "H", getStandardMoveFromRecord(h17Result));
        assertTrue("H17 configuration must execute active conditional surrender rules", getShouldSurrenderFromRecord(h17Result));
    }

    // --- Reflection Helper Methods to extract details out of the private ParsedMoveResult Record ---

    private String getStandardMoveFromRecord(Object recordInstance) throws Exception {
        Method method = recordInstance.getClass().getMethod("standardMove");
        return (String) method.invoke(recordInstance);
    }

    private boolean getShouldSurrenderFromRecord(Object recordInstance) throws Exception {
        Method method = recordInstance.getClass().getMethod("shouldSurrender");
        return (boolean) method.invoke(recordInstance);
    }
}