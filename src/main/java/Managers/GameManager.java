package Managers;

import Misc.*;
import Entities.*;
import Entities.Snake.*;

import javax.swing.Timer;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;

public class GameManager {

    private GameState state = GameState.PAUSED;
    private GameBoard board;
    private Snake snake;
    private Difficulty difficulty = Difficulty.EASY;
    private int score = 0;

    private final DatabaseManager db;
    private final UIManager ui;
    private Timer timer;

    private int tickMillis = 150;

    public GameManager() throws SQLException {

        this.db = new DatabaseManager(100);

        this.ui = new UIManager(this);
        ui.showMenu();
    }

    public void setDifficulty(Difficulty d) {
        this.difficulty = d;
    }

    public void startNewGame() {
        this.board = new GameBoard(12, difficulty.rockCount());
        int mid = board.getSize() / 2;
        this.snake = new Snake(mid, mid);

        board.spawnFood();
        board.spawnRocks();

        if (snake.getSegments() != null && !snake.getSegments().isEmpty()) {
            board.refreshSnake(snake);
        }

        score = 0;
        state = GameState.RUNNING;
        ui.showGameScreen();
        startLoop();
    }

    public void pauseGame() {
        if (state != GameState.RUNNING) return;
        state = GameState.PAUSED;
        stopLoop();
        ui.showMenu();
    }

    public void resumeGameFromPause() {
        if (state != GameState.PAUSED) return;
        state = GameState.RUNNING;
        ui.showGameScreen();
        startLoop();
    }

    public void gameOver() {
        state = GameState.OVER;
        stopLoop();
        ui.showGameOverScreen(score);
    }

    public void saveScore(String playerName) {
        try {
            db.saveScore(new PlayerScore(playerName, score));
        } catch (SQLException e) {
            System.err.println("Error while reading database");
            System.exit(0);
        }
    }

    public ArrayList<PlayerScore> getTopScores(int limit) {
        try {
            return db.getTopScores(limit);
        } catch (SQLException e) {
            System.err.println("Error while reading database");
            System.exit(0);
        }
        return null;
    }


    private void startLoop() {
        stopLoop();
        timer = new Timer(tickMillis, e -> {
            if (state == GameState.RUNNING) {
                tick();
            }
        });
        timer.start();
    }

    private void stopLoop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void tick() {
        SnakeRattle rattle = (SnakeRattle) snake.getSegments().getLast();
        Position oldTailPosCopy = new Position(rattle.getPosition().getX(), rattle.getPosition().getY());

        snake.move();

        Position headPos = snake.getSegments().getFirst().getPosition();
        if (board.isOutOfBounds(headPos)) { //ütközés fallal
            gameOver();
            return;
        }

        if (snake.checkSelfCollision()) { //ütközés önmagával
            gameOver();
            return;
        }

        Rock[] rocks = board.getRocks();
        if (rocks != null) { //ütközés kövekkel
            for (Rock r : rocks) {
                if (r != null && headPos.equals(r.getPosition())) {
                    gameOver();
                    return;
                }
            }
        }

        if (Food.getInstance().getPosition() != null &&
                headPos.equals(Food.getInstance().getPosition())) { //elemózsia felszedése
            score++;
            snake.getSegments().add(new SnakeRattle(oldTailPosCopy));
            board.spawnFood();
        }

        board.refreshSnake(snake);
        ui.updateBoard(score);
    }

    public void handleKeyPress(KeyEvent e) {
        if (snake == null) return;

        if (state != GameState.RUNNING && e.getKeyCode() != KeyEvent.VK_P && e.getKeyCode() != KeyEvent.VK_ESCAPE) {
            return;
        }

        Direction newDir = null;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> newDir = Direction.UP;
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> newDir = Direction.DOWN;
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> newDir = Direction.LEFT;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> newDir = Direction.RIGHT;
            case KeyEvent.VK_P -> {
                if (state == GameState.RUNNING) pauseGame();
                else if (state == GameState.PAUSED) resumeGameFromPause();
                return;
            }
            case KeyEvent.VK_ESCAPE -> { pauseGame(); return; }
        }

        Direction currentDir = snake.getDirection();
        if (newDir != null && !isOpposite(currentDir, newDir)) {
            snake.setDirection(newDir);
        }
    }

    private boolean isOpposite(Direction d1, Direction d2) {
        return (d1 == Direction.UP && d2 == Direction.DOWN)
                || (d1 == Direction.DOWN && d2 == Direction.UP)
                || (d1 == Direction.LEFT && d2 == Direction.RIGHT)
                || (d1 == Direction.RIGHT && d2 == Direction.LEFT);
    }

    public GameBoard getBoard() { return board; }
    public Snake getSnake() { return snake; }
    public Difficulty getDifficulty() { return difficulty; }
}
