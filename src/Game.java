/*
Make runnable? Each thread runs an instance of the Game class.
Maybe have constructor create X bots to enter into game?
Or a factory to do this.
*/
import java.util.ArrayList;

public class Game {
    private Dealer dealer;
    private ArrayList<Player> players;

    /**
     * Constructor now accepts the bots/players directly.
     * No Scanner/System.in used here.
     */
    public Game(ArrayList<Player> players, int shoeSize) {
        this.players = players;
        // Connect the players to the dealer
        this.dealer = new Dealer(players, shoeSize);
        runSimulation();
    }

    public boolean isBust(Hand hand) {
        return hand.getScore() > 21;
    }

    /**
     * Runs the simulation until the deck reaches the penetration limit.
     */
    public void runSimulation() {
        int deckPen = players.size() * 10;
        // Assume each player might take up to 5 cards

        while (dealer.deck.getDeck().size() > deckPen) {

            // 1. Dealer deals the initial 2 cards to everyone
            this.dealer.deal();
            if (dealer.hand.getScore() == 21) {
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
            System.out.println("Dealer score: " + dealerScore);

            // 4. Resolve winnings
            resolveRound(dealerScore);

            // 5. Cleanup hands for the next round
            for (Player p : players) {
                p.finishRound();
            }
        }

        System.out.println("--- Simulation Ended: Deck low ---");
    }

    private void resolveRound(int dealerScore) {
        boolean dealerBust = isBust(this.dealer.hand);

        for (Player p : players) {
            for (Hand h : p.getHands()) {
                if (isBust(h)) {
                    System.out.println(p.getName() + " Bust! Hand: " + h.getScore());
                    p.winnings -= h.getBet();
                }
                else if (dealerBust) {
                    System.out.println(p.getName() + " Won (Dealer Bust)! Hand: " + h.getScore());
                    p.winnings += h.getBet();
                }
                else if (h.getScore() > dealerScore) {
                    System.out.println(p.getName() + " Won! Hand: " + h.getScore());
                    p.winnings += h.getBet();
                }
                else if (h.getScore() < dealerScore) {
                    System.out.println(p.getName() + " Lost! Hand: " + h.getScore());
                    p.winnings -= h.getBet();
                }
                else {
                    System.out.println(p.getName() + " Push! Hand: " + h.getScore());
                    // Winnings remain unchanged for push
                }
            }
        }
    }
}