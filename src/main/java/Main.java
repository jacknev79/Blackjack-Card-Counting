/*
flags/ optional features split into 2 kinds:
game variables ie deckpen, numplayers, etc
strategy changers ie h/s17, surrender, resplit aces, etc
*/

public static void main(String[] args) {
    // 1. Set default values
    int numPlayers = 4;
    int deckPenetration = 40; // Default: e.g., 4 players * 10
    int shoeSize = 6;
    int numGames = 1000000;
    // allowing surrender seems to be reducing bot winnings?
    boolean earlySurrender = false;
    boolean lateSurrender = false;
    boolean hitSoft17 = false;

    // 2. Parse arguments
    for (int i = 0; i < args.length; i++) {
        switch (args[i]) {
            case "--players":
            case "-p":
                numPlayers = Integer.parseInt(args[++i]);
                break;
            case "--penetration":
            case "-pen":
                deckPenetration = Integer.parseInt(args[++i]);
                break;
            case "--shoe":
            case "-s":
                shoeSize = Integer.parseInt(args[++i]);
                break;
            case "--surrender":
            case "-surr":
                // need to handle to accept as next arg early or late
                //earlySurrender = true;
                break;
            case "--hit-soft-17":
                hitSoft17 = true;
                break;
            default:
                System.out.println("Unknown argument: " + args[i]);
                System.exit(1);
        }
    }

    // 3. Create the immutable config ONCE
    GameConfig config = new GameConfig(numPlayers, deckPenetration, shoeSize, numGames, earlySurrender, lateSurrender, hitSoft17);

    BlackjackStrategyLoader.initialize(config);
    // 4. Start your ExecutorService and pass the config to your threads
    // executor.submit(new Game(1, players, config));
    /*String[] options = new String[4];
    options[0] = numPlayers;
    // NB is located in Game.java class
    options[1] = deckPenetration;
    options[2] = shoeSize;
    options[3] = numGames;*/
    LaunchSimulation.main(config);
}