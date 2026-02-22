import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    ArrayList<Card> deck = new ArrayList<>();

    @Override
    public String toString() {
        return deck.toString();
    }

    public Card takeCard(){
        return deck.removeLast();
    }

    private void shuffle(){
        Collections.shuffle(deck);
    }

    public Deck(int shoeSize) {
        for (int i = 0; i != shoeSize; i++){
            for (int j = 0; j != 4; j++){
                char suit = switch (j) {
                    case 0 -> 'C';
                    case 1 -> 'D';
                    case 2 -> 'S';
                    case 3 -> 'H';
                    default -> 0;
                };
                for (int n = 1; n < 14; n++){
                    String rank;
                    if (n == 11) rank = "J";
                    else if (n == 12) rank = "Q";
                    else if (n == 13) rank = "K";
                    else if (n == 1) rank = "A";
                    else {
                        rank = String.valueOf(n);
                    }
                    deck.add(new Card(suit, rank));
                }
            }
        }
        shuffle();
    }
    public ArrayList<Card> getDeck() {
        return deck;
    }
}
