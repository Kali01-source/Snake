package Misc;

public record PlayerScore(String playerName, int score) {
    public PlayerScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }
}
