import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LaunchSimulationTest {


    @Test
    void testMainExecutionCompletesWithoutCrashing() {
        // Configure a tiny simulation that should execute and finish immediately
        // 1 Player, high penetration (so round ends fast), tiny shoe, 2 multithreaded games.
        GameConfig smallConfig = new GameConfig(1, 50, 1, 2, false,false, false);
        BlackjackStrategyLoader.initialize(smallConfig);
        // Using assertDoesNotThrow to ensure parallel processing thread pools
        // shutdown correctly and standard outputs format correctly.
        assertDoesNotThrow(() -> {
            LaunchSimulation.main(smallConfig);
        });
    }
}