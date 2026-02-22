import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Game {
    Dealer dealer;
    ArrayList<Player> players = new ArrayList<>();
    Scanner inp = new Scanner(System.in);

    public Game() {
        System.out.println("Enter the number of decks per shoe:");
        int shoeSize = inp.nextInt();
        while (true) {
            System.out.println("Please enter 'y' to add another player.");
            String check = inp.next();
            if (!Objects.equals(check, "y")) break;

            System.out.println("Please enter the name of the next player.");
            String name = inp.next();
            Player p = new Player(name);
            players.add(p);
        }
        this.dealer = new Dealer(players, shoeSize);
        play();
    }

    public boolean isBust(Hand hand){
        if (hand.getScore() > 21) return true;
        return false;
    }

    public void play() {
        while (dealer.deck.getDeck().size() > 20) {
            this.dealer.deal();
            for (Player p : dealer.getPlayers()) {
                p.play(this.dealer);
            }
            int dealerScore = this.dealer.play();
            System.out.println("Dealer has: " + dealerScore);
            if (isBust(this.dealer.hand)) {
                for (Player p : this.dealer.getPlayers()) {
                    for (Hand h : p.getHands()) {
                        if (!isBust(h)) {
                            System.out.println(h + " Won!");
                            p.winnings += h.getBet();
                        } else {
                            System.out.println(h + " Lost!");
                            p.winnings -= h.getBet();
                        }
                    }
                }
            } else {
                for (Player p : this.dealer.getPlayers()) {
                    for (Hand h : p.getHands()) {
                        System.out.println(p.getName() + " Has " + h.getScore());
                        if (isBust(h)) {
                            System.out.println("Bust!");
                            p.winnings -= h.bet;
                            continue;
                        }
                        if (h.getScore() < dealerScore) {
                            System.out.println(h + "Lost!");
                            p.winnings -= h.bet;
                        } else if (h.getScore() == dealerScore) {
                            System.out.println(h + "Push!");
                        } else {
                            System.out.println(h + "Wins!");
                            p.winnings += h.bet;
                        }
                    }
                    p.getHands().clear();
                }
            }
        }
        play();
    }
}
