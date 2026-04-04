/* NB card counting implemented by tracking running/ true
counts. Then will have multiple hashmaps of hashmaps of Strings,
one for each truecount level bot will be deviating at.
Each hashmap has as key dealer upcard and value = hashmap of strings.
The key of second hashmap = score of own card, while the value is the move
to pass into super.play() method.
NB will need a separate hashmap for splitting, should create an isSplittable()
method to determine if so, then use that hashmap for lookup.
Also may need hashmap if having aces? For hard/ soft values.
If ace in hand, check if hand.score + 10 <= 21, if true then hand is soft?
NB each hand can be in one of three states:
Hard
Soft (ace exists and has value of 11)
Doubles (Splitting is allowed)
so should have separate funcs to determine 1 if spittable, 2 if hard/soft
 */

import java.util.ArrayList;

public class BlackjackBot extends Player implements Runnable {
    private int botId;
    private final BlackjackStrategyLoader strategy;

    public BlackjackBot(int id) {
        super(";");
        this.botId = id;
        this.strategy = BlackjackStrategyLoader.getInstance();
    }

    @Override
    public void run() {
        // Access the shared global instance


        // Use the strategy
    }

    @Override
    public void play(Dealer dealer) {
        String upcardKey = String.valueOf(dealer.upCard());
        // Standard for-loop allows hands.size() to grow dynamically if we split
        for (int i = 0; i < hands.size(); i++) {
            Hand hand = hands.get(i);

            // 1. Get the Dealer's upcard rank as a String (e.g., "10")
            // Ensure getRank() returns the numeric value (1-11)


            while (hand.getScore() < 21) {
                String move = null;

                // 2. CHECK SPLIT STRATEGY
                if (isSplittable(hand)) {
                    String pairKey = String.valueOf(hand.getCard().getRank());

                    // Directly query the Singleton, checking for null on the fly
                    if (strategy.getSplitStrategy().get(upcardKey) != null) {
                        String decision = strategy.getSplitStrategy().get(upcardKey).get(pairKey);

                        if ("split".equalsIgnoreCase(decision)) {
                            split(hand, dealer.deck.takeCard(), dealer.deck.takeCard());
                            continue; // (or continue, depending on your loop structure)
                        }
                    }
                }

// 3. CHECK HARD/SOFT STRATEGY
                String scoreKey = String.valueOf(hand.getScore());

// 4. EXTRACT MOVE WITH DIRECT NULL SAFETY
                if (isSoft(hand) && (hand.getScore() - 11) > 9) {
                    if (strategy.getSoftStrategy().get(upcardKey) != null) {
                        move = strategy.getSoftStrategy().get(upcardKey).get(String.valueOf(getScoresExceptFirstAce(hand)));
                    }
                }
                else {
                    if (strategy.getHardStrategy().get(upcardKey) != null) {
                        move = strategy.getHardStrategy().get(upcardKey).get(scoreKey);
                    }
                }

// 5. FAILSAFE
                if (move == null) {
                    System.out.println("Move not found");
                    System.out.println(String.valueOf(hand) + ' ' + hand.getScore());
                    move = (hand.getScore() >= 17) ? "S" : "H";
                }

                // 5. FAILSAFE: If the score isn't in our .properties files (e.g., score < 8)
                if (move == null) {
                    move = (hand.getScore() >= 17) ? "S" : "H";
                }

                // 6. EXECUTE MOVE
                if (move.equals("S")) {
                    break;
                } else if (move.equals("H")) {
                    hand.hit(dealer.deck.takeCard());
                    // if gotten null card, deck is empty!
                    if (hand.getHand().getLast() == null) {
                        hand.getHand().removeLast();
                        break;
                    }
                } else if (move.equals("D")) {
                    // Standard Double Down: Hit once and force stand
                    if (hand.getHand().size() == 2) {
                        hand.hit(dealer.deck.takeCard());
                        hand.bet *= 2;
                    } else {
                        // Cannot double after hitting; default to Hit
                        hand.hit(dealer.deck.takeCard());
                        continue;
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean takeInsurance() {
        // Nb will be changed when true/ running count is implemented
        return true;
    }

    @Override
    public int enterBet() {
        return 100;
    }

    private boolean isSplittable(Hand hand) {
        return hand.getCard() == hand.getHole();
    }

    private boolean isSoft(Hand hand) {
        boolean hasAce = false;

        // Iterate over all cards in the hand list, not just hole and upcard
        for (int i = 0; i < hand.getHand().size(); i++) {
            // Assuming your Card object uses rank 1 for an Ace
            if (hand.getHand().get(i).getRank() == 1) {
                hasAce = true;
                break;
            }
        }

        return hasAce && hand.getScore() <= 21;
    }

    public int getScoresExceptFirstAce(Hand hand) {
        boolean aceSkipped = false;
        int score = 0;
        ArrayList<Card> cards = hand.getHand();

        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getRank() == 1 && !aceSkipped) {
                aceSkipped = true;
                continue;
            }
            score += cards.get(i).getRank();
        }

        return score;
    }
}



