import java.util.ArrayList;
import java.util.Objects;


public class Dealer {
    Deck deck;
    Hand hand;
    ArrayList<Player> players;
    int shoeSize;
    int runningCount;
    int trueCount;
    //NB should not generally close Scanner with stream = System.in
    //As will be needed for each hand to take in bet amounts, Scanner
    //is opened on Dealer initialisation.

    public Dealer(ArrayList<Player> players, int shoeSize) {
        this.players = players;
        this.shoeSize = shoeSize;
        this.runningCount = 0;
        this.trueCount = 0;

        this.deck = new Deck(this.shoeSize);
    }

    void deal(){
        Card dcard = this.deck.takeCard();
        Card dhole = this.deck.takeCard();
        this.hand = new Hand(dcard, dhole, 0);
        this.runningCount += cardCount(dcard) + cardCount(dhole);

        for (Player player : this.players ){
            // System.out.println(player.name + " How much will you bet this hand?");
            int bet = player.enterBet(getTrueCount());
            Card card = this.deck.takeCard();
            Card hole = this.deck.takeCard();
            Hand phand = new Hand(card, hole, bet);
            this.runningCount += cardCount(card) + cardCount(hole);
            player.addHand(phand);
            player.setBet(bet);
        }
        if (this.hand.card.getRank() == 1) {
            ArrayList<Player> insured = new ArrayList<>();
            // System.out.println("Dealer's upcard is an Ace. Please enter 'y' to take Insurance");
            for (Player player : this.players){
                boolean check = player.takeInsurance(getTrueCount());
                if (check) insured.add(player);
            }
            if (!hasBlackjack() && !insured.isEmpty()) {
                // System.out.println("Dealer did not have Blackjack");
                //dealer did not have blackjack, bet is lost
                for (Player player : insured) {
                    player.winnings -= player.bet;
                }
            }
            else if (hasBlackjack() && !insured.isEmpty()){
                //dealer has blackjack, so pay out winnings
                //System.out.println("Dealer had BlackJack!");
                for (Player player : insured) {
                    player.winnings += 2 * player.getBet();
                }
            }
            if (this.hand.getScore() == 21){
                //System.out.println("Dealer has BlackJack!");
                for (Player p : players){
                    if (p.hasBlackjack(p.hands.getFirst())){
                        //NB not sure if insured players can push!
                        //System.out.println(p.name + " has BlackJack! Push.");
                    }
                    else{
                        p.winnings -= p.getBet();
                        // System.out.println(p.name + " lost!");
                    }
                }

            }
        }
        // System.out.println("Dealers upcard is: " + this.hand.getCard());

    }

    int play(){
        while (this.hand.getScore() < 17){
            // System.out.println("Dealers hand is: " + this.hand);
            Card card =  this.deck.takeCard();
            this.hand.hit(card);
            this.runningCount += cardCount(card);
        }
        // System.out.println("Dealers hand is: " + this.hand);
        return this.hand.getScore();
    }
    boolean hasBlackjack(){
        if (this.hand.hole.getRank() == 10) return true;
        return false;
    }

    public Card takeCard(){
        if (this.deck.getDeck().size() == 0) return null;
        Card card =  this.deck.takeCard();
        this.runningCount += cardCount(card);
        return card;
    }

    private int cardCount(Card c) {
        if (c.getRank() == 1 || c.getRank() == 10) return -1;
        if (c.getRank() < 7) return 1;
        return 0;
    }

    public int getTrueCount() {
        if (this.deck.getDeck().size() < 52) return runningCount;
        return runningCount / (this.deck.getDeck().size()/52);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int upCard() {
        return this.hand.getCard().getRank();
    }
}