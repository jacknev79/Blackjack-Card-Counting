/*
Aces are dealt with by treating as value = 1, and if an ace exists
in a hand and hand.score < 11, 10 points are added to hand.score.
*/

import java.util.Objects;

public class Card {
    char suit;
    String rank;

    @Override
    public String toString() {
        return rank + suit;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Card card)) return false;
        return Objects.equals(rank, card.rank);
    }

    public Card(char suit, String rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public int getRank() {
        switch (this.rank){
            case "10", "J", "Q", "K":
                return 10;
            case "2":
                return 2;
            case "3":
                return 3;
            case "4":
                return 4;
            case "5":
                return 5;
            case "6":
                return 6;
            case "7":
                return 7;
            case "8":
                return 8;
            case "9":
                return 9;
            case "A":
                return 1;

        }
        return 0;
    }

    public char getSuit() {
        return suit;
    }
}
