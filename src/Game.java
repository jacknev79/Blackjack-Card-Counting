import java.util.ArrayList;

public class Game implements Runnable {
    private Dealer dealer;
    private ArrayList<Player> players;
    private int gameId; // Added to identify which game thread is printing
    private double totalWinnings;

    public Game(int gameId, ArrayList<Player> players, int shoeSize) {
        this.gameId = gameId;
        this.players = players;
        // Connect the players to the dealer
        this.dealer = new Dealer(players, shoeSize);
        // runSimulation() removed from here. It will be called by run()
    }

    // This method is required by the Runnable interface
    @Override
    public void run() {
        runSimulation();
    }

    public boolean isBust(Hand hand) {
        return hand.getScore() > 21;
    }

    /**
     * Runs the simulation until the deck reaches the penetration limit.
     */
    public void runSimulation() {
        int DECK_PENETRATION = players.size() * 10;

        while (dealer.deck.getDeck().size() > DECK_PENETRATION) {

            // 1. Dealer deals the initial 2 cards to everyone
            this.dealer.deal();
            if (dealer.hand.getScore() == 21) {
                resolveRound(21);
                for (Player p : players) {
                    p.finishRound();
                }
                continue;
            }

            // 2. Each player/bot plays their turn
            for (Player p : players) {
                p.play(this.dealer);
            }

            // 3. Dealer plays their hand and gets final score
            int dealerScore = this.dealer.play();
            // System.out.println("Game " + gameId + " | Dealer score: " + dealerScore);

            // 4. Resolve winnings
            resolveRound(dealerScore);

            // 5. Cleanup hands for the next round
            for (Player p : players) {
                p.finishRound();
            }
        }

        // --- END OF SIMULATION FOR THIS GAME ---
        printFinalStats();
    }

    private void printFinalStats() {
        //System.out.println("--- Game " + gameId + " Ended: Deck low ---");
        double totalWinnings = 0;

        for (Player p : players) {
            //System.out.println("Game " + gameId + " | " + p.getName() + " Final Winnings: " + p.winnings);
            totalWinnings += p.winnings;
        }
        this.totalWinnings = totalWinnings;

        double averageWinnings = totalWinnings / players.size();
      /*  System.out.println("Game " + gameId + " | Average Winnings per bot: " + averageWinnings);
        System.out.println("----------------------------------------");*/
    }

    private void resolveRound(int dealerScore) {
        boolean dealerBust = isBust(this.dealer.hand);
        boolean dealerBlackJack = false;
        if (dealerScore == 21 && dealer.hand.getHand().size() == 2) dealerBlackJack = true;

        for (Player p : players) {
            for (Hand h : p.getHands()) {
                if (isBust(h)) {
                    // System.out.println(p.getName() + " Bust! Hand: " + h.getScore());
                    p.winnings -= h.getBet();
                }
                else if (p.hasBlackjack(h) && h.getHand().size() == 2){
                    if (dealerBlackJack) {
                        break;
                    }
                    p.winnings += (int) (1.5 * h.getBet());
                }
                else if (dealerBlackJack){
                    p.winnings -= h.getBet();
                }
                else if (dealerBust) {
                    // System.out.println(p.getName() + " Won (Dealer Bust)! Hand: " + h.getScore());
                    p.winnings += h.getBet();
                }
                else if (h.getScore() > dealerScore) {
                    // System.out.println(p.getName() + " Won! Hand: " + h.getScore());
                    p.winnings += h.getBet();
                }
                else if (h.getScore() < dealerScore) {
                    // System.out.println(p.getName() + " Lost! Hand: " + h.getScore());
                    p.winnings -= h.getBet();
                }
                else {
                    // System.out.println(p.getName() + " Push! Hand: " + h.getScore());
                    // Winnings remain unchanged for push
                }
            }
        }
    }

    public int getId() {
        return gameId;
    }

    public double getWinnings() {
        return totalWinnings;
    }
}