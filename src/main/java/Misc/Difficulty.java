package Misc;

public enum Difficulty {
    EASY(1), NORMAL(2), HARD(4);
    private final int rockCount;
    Difficulty(int rockCount) { this.rockCount = rockCount; }
    public int rockCount() { return rockCount; }
}
