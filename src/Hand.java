import java.util.ArrayList;
public class Hand {
    ArrayList<Card> hand = new ArrayList<>();
    Card card;
    Card hole;
    int bet;
    int score;

    @Override
    public String toString() {
        return "Hand{" +
                "hand=" + hand +
                '}' + "Bet:" + bet;
    }

    public Hand(Card card, Card hole, int bet) {
        this.card = card;
        this.hole = hole;
        this.bet = bet;
        hand.add(card);
        hand.add(hole);
        score = this.card.getRank() + this.hole.getRank();
        if (this.card.getRank() == 1 || this.hole.getRank() == 1) score += 10;
    }

    void hit(Card card) {
        hand.add(card);
        int sum = 0;
        boolean ace = false;
        for (int i = 0; i != hand.size(); i++){
            int num = hand.get(i).getRank();
            if (num == 1) {
                ace = true;
            }
            sum += num;
        }
        if (ace && sum < 11) sum += 10;
        score = sum;
    }

    public int getScore() {
        return score;
    }

    Card getHole() {
        return hole;
    }

    void replaceHoleCard(Card newCard){
        this.hole = newCard;
    }

    public Card getCard() {
        return card;
    }

    public int getBet() {
        return bet;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }
}
