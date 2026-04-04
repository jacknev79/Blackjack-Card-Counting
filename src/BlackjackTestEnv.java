import java.util.ArrayList;

public class BlackjackTestEnv {

    public static void main(String[] args) {
        System.out.println("--- Initializing Blackjack Simulation ---");

        // 1. Create the list to hold our bots
        ArrayList<Player> botList = new ArrayList<>();

        // 2. Initialize 4 BlackjackBot instances
        // We add them to the list as 'Player' objects so the Game class can handle them
        for (int i = 1; i <= 6; i++) {
            // Passing the ID as the name for the Player constructor
            BlackjackBot bot = new BlackjackBot(i);
            botList.add(bot);
        }

        // 3. Initialize the Game engine
        // We pass the bot list and the number of decks (shoe size)
        int shoeSize = 40;
        Game blackjackGame = new Game(botList, shoeSize);

        System.out.println("Starting simulation with " + botList.size() + " bots and " + shoeSize + " decks...");

        // 4. Run the simulation
        // This will loop internally until the deck penetration limit is reached
        //blackjackGame.runSimulation();

        // 5. Simulation finished - Print final stats
        System.out.println("\n========================================");
        System.out.println("          SIMULATION COMPLETE           ");
        System.out.println("========================================");

        for (Player p : botList) {
            // Cast back to BlackjackBot if you need to access bot-specific methods like getBotId()
            if (p instanceof BlackjackBot) {
                BlackjackBot bot = (BlackjackBot) p;
                System.out.println("Bot ID:" + bot.getName() + " Final Winnings:" + bot.winnings);
            }
        }
        System.out.println("========================================");
    }
}