public record GameConfig(
        int numPlayers,
        int numDummy,
        int deckPenetration,
        int shoeSize,
        int numGames,
        boolean earlySurrender,
        boolean lateSurrender,
        boolean hitSoft17
) {
}