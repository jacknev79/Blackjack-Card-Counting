import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LaunchSimulation {

    public static void main(GameConfig config) {

        int NUM_PLAYERS = config.numPlayers();
        int NUM_GAMES = config.numGames();
        int NUM_DUMMY = 3;

        // 1. Thread-safe storage: Every index is accessed by exactly one game ID
        double[] allAverageWinnings = new double[NUM_GAMES];
        double[] allHoursSpent = new double[NUM_GAMES];

        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        System.out.println("Starting " + NUM_GAMES + " games across " + cores + " threads...");

        for (int i = 0; i < NUM_GAMES; i++) {
            ArrayList<BlackjackBot> players = new ArrayList<>();
            for (int j = 1; j <= NUM_PLAYERS; j++) {
                players.add(new BlackjackBot(j));
            }

            Game game = new Game(i, players, config);

            // 3. Wrap the execution in a lambda to handle the result storage
            executor.submit(() -> {
                game.run();

                // After run() completes, extract the data.
                int gameId = game.getId();
                double result = game.getWinnings();
                double hours = game.getHoursSpentCounting();
                // Write to the specific index. No sync needed because gameId is unique.
                allAverageWinnings[gameId] = result;
                allHoursSpent[gameId] = hours;
            });
        }

        executor.shutdown();

        try {
            // 4. Wait for all threads to finish.
            // This creates a 'happens-before' memory barrier.
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Simulation interrupted!");
            e.printStackTrace();
        }

        System.out.println("All simulations have successfully completed.");
        // Main thread can now safely read allAverageWinnings
        double avgWinnings;
        double expectedHourlyValue;
        double sumWinnings = 0;

        double sumExpectedHourlyValue = 0;

        double highest = 0;
        double lowest = 0;
        for (int i = 0; i < NUM_GAMES; i++) {
            sumWinnings += allAverageWinnings[i];
            if (allAverageWinnings[i] > highest) {
                highest = allAverageWinnings[i];
            }
            else if (allAverageWinnings[i] < lowest) {
                lowest = allAverageWinnings[i];
            }
            if (allHoursSpent[i] > 0 && allAverageWinnings[i] > 0) {
                sumExpectedHourlyValue += allAverageWinnings[i] / allHoursSpent[i];
        }
        }
        avgWinnings = sumWinnings / NUM_GAMES;
        expectedHourlyValue = sumExpectedHourlyValue / NUM_GAMES;
        System.out.println("Average winnings per game: " + avgWinnings);
        System.out.println("Lowest bot winnings: " + lowest);
        System.out.println("Highest bot winnings: " + highest);
        System.out.println("Your Expected Earnings per hour is: " + expectedHourlyValue);
    }
}