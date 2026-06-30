/*
NB will need to update directory paths/ add searching to make cli interactions work
should create a non-counting bot that plays random moves?
then have estimated time to play each card, stop a game at e.g. 1hr
to get hourly rate?

flags left to do:
resplit, double after split, split aces
deck penetration
handle late/ early surr

should re-add user-player back into new branch. can then have card counting training sim created???
*/

public static void main(String[] args) {
    // 1. Set default values
    int numPlayers = 4;
    int deckPenetration = 40; // Default: e.g., 4 players * 10
    int shoeSize = 2;
    int numGames = 1000000;
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
            case "--late-surrender":
            case "-lsurr":
                // need to handle to accept as next arg early or late
                lateSurrender = true;
                break;
            case "--hit-soft-17":
            case "-h17":
                hitSoft17 = true;
                break;
            case "--early-surrender":
            case "-esurr":
                // need to handle to accept as next arg early or late
                earlySurrender = true;
                break;
            default:
                System.out.println("Unknown argument: " + args[i]);
                System.exit(1);
        }
    }

    // 3. Create the immutable config ONCE
    GameConfig config = new GameConfig(numPlayers, deckPenetration, shoeSize, numGames, earlySurrender, lateSurrender, hitSoft17);

    BlackjackStrategyLoader.initialize(config);

    System.out.println("Starting Simulation of " + numGames +  " Games, with " + numPlayers + " players and " + shoeSize + " Decks per shoe");
    if (hitSoft17) System.out.println("Hit17 is on");
    else System.out.println("Hit17 is off");
    if (earlySurrender) System.out.println("EarlySurrender is on");
    else System.out.println("EarlySurrender is off");
    if (lateSurrender) System.out.println("Late Surrender is on");
    else System.out.println("Late Surrender is off");
    LaunchSimulation.main(config);
}