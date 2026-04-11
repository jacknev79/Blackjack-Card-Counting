import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LaunchSimulation {

    public static void main(String[] args) {
        int NUM_GAMES = 10;
        int NUM_PLAYERS = 2; // Set the number of bots per game
        int SHOE_SIZE = 6;

        // Use a thread pool sized to your CPU cores for maximum efficiency
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);

        System.out.println("Starting " + NUM_GAMES + " games across " + cores + " threads...");

        for (int i = 1; i <= NUM_GAMES; i++) {

            // 1. Create a fresh list of bots for THIS specific game
            ArrayList<Player> players = new ArrayList<>();
            for (int j = 1; j <= NUM_PLAYERS; j++) {
                // Adjust this line to match how you instantiate your specific Bot/Player class
                players.add(new BlackjackBot(j));
            }

            // 2. Instantiate the game
            Game game = new Game(i, players, SHOE_SIZE);

            // 3. Hand the game off to the thread pool to execute
            executor.submit(game);
        }

        // 4. Tell the executor we are done sending tasks
        executor.shutdown();

        try {
            // 5. Wait for all 1000 games to finish executing
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Simulation interrupted!");
            e.printStackTrace();
        }

        System.out.println("All simulations have successfully completed.");
    }
}