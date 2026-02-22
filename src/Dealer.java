import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Dealer {
    Deck deck;
    Hand hand;
    ArrayList<Player> players;
    int shoeSize;
    //NB should not generally close Scanner with stream = System.in
    //As will be needed for each hand to take in bet amounts, Scanner
    //is opened on Dealer initialisation.
    Scanner inp = new Scanner(System.in);

    public Dealer(ArrayList<Player> players, int shoeSize) {
        this.players = players;
        this.shoeSize = shoeSize;

        this.deck = new Deck(this.shoeSize);
    }

    void deal(){
            this.hand = new Hand(this.deck.takeCard(), this.deck.takeCard(), 0);

            for (Player player : this.players ){
                System.out.println(player.name + " How much will you bet this hand?");
                int bet = inp.nextInt();
                Hand phand = new Hand(this.deck.takeCard(), this.deck.takeCard(), bet);
                player.addHand(phand);
                player.setBet(bet);
            }
            if (this.hand.card.getRank() == 1) {
                ArrayList<Player> insured = new ArrayList<>();
                System.out.println("Dealer's upcard is an Ace. Please enter 'y' to take Insurance");
                for (Player player : this.players){
                    System.out.println(player.name + "Would you like Insurance?");
                    String check = inp.next();
                    if (Objects.equals(check, "y")) insured.add(player);
                    }
                if (!hasBlackjack() && !insured.isEmpty()) {
                    System.out.println("Dealer did not have Blackjack");
                    //dealer did not have blackjack, bet is lost
                    for (Player player : insured) {
                        player.winnings -= player.bet;
                    }
                }
                else if (hasBlackjack() && !insured.isEmpty()){
                    //dealer has blackjack, so pay out winnings
                    System.out.println("Dealer had BlackJack!");
                    for (Player player : insured) {
                        player.winnings += 2 * player.getBet();
                    }
                }
                if (this.hand.getScore() == 21){
                    System.out.println("Dealer has BlackJack!");
                    for (Player p : players){
                        if (p.hasBlackjack(p.hands.getFirst())){
                            System.out.println(p.name + " has BlackJack! Push.");
                        }
                        else{
                            p.winnings -= p.getBet();
                            System.out.println(p.name + " lost!");
                        }
                    }

                }
            }
        System.out.println("Dealers upcard is: " + this.hand.getCard());
    }

    int play(){
        while (this.hand.getScore() < 17){
            System.out.println("Dealers hand is: " + this.hand);
            this.hand.hit(this.deck.takeCard());
        }
        System.out.println("Dealers hand is: " + this.hand);
        return this.hand.getScore();
    }
    boolean hasBlackjack(){
        if (this.hand.hole.getRank() == 10) return true;
        return false;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
