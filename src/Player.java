import java.util.ArrayList;
import java.util.Scanner;

public class Player {
    Scanner inp = new Scanner(System.in);
    ArrayList<Hand> hands;
    int winnings;
    String name;
    int bet;

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", winnings=" + winnings +
                ", hand=" + hands +
                '}';
    }

    public Player(String name) {
        this.winnings = 0;
        this.name = name;
        this.hands = new ArrayList<>();
    }

    void play(Dealer dealer) {
        if (hasBlackjack(hands.getFirst())){
            System.out.println("BlackJack! Hand wins!");
            this.winnings += (int) (1.5 * hands.getFirst().bet);
        }
        for (Hand hand : hands) {
            System.out.println("Your hand is: " + hand);
            while (hand.getScore() < 21) {
                System.out.println(this.name + " Please enter your next move:");
                String move = inp.next();
                move = move.toLowerCase();
                //NB may have to change to if to take adv. of break statement

                if (move.equals("stand")) {
                    break;
                } else if (move.equals("hit")) {
                    hand.hit(dealer.deck.takeCard());
                } else if (move.equals("double") && hand.getHand().size() == 2){
                    hand.hit(dealer.deck.takeCard());
                    hand.bet *= 2;
                    break;
                } else if (move.equals("split") && hand.getCard() == hand.getHole()) {//creates a new hand with card1 = this.holecard and
                    //card2 = arg2. arg3 = replacement card for this.holecard.
                    //new card is then appended to hands list.
                    split(hand, dealer.deck.takeCard(), dealer.deck.takeCard());
                }
                else{
                    System.out.println("Please enter a valid move!");
                }
                System.out.println("Your hand is: " + hand);
            }
        }
    }

    public boolean hasBlackjack(Hand hand){
        if (hand.getScore() == 21) return true;
        return false;
    }
    public void split(Hand hand, Card card, Card holeCard){
        Hand newHand = new Hand(hand.getHole(), card, hand.getBet());
        this.hands.add(newHand);
        hand.replaceHoleCard(holeCard);
    }

    public void addHand(Hand hand){
        this.hands.add(hand);
    }

    public int getWinnings() {
        return winnings;
    }

    public ArrayList<Hand> getHands() {
        return hands;
    }

    public String getName() {
        return name;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }
}
