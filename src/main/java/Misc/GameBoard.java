package Misc;

import Entities.*;
import Entities.Snake.*;

import java.util.*;

/**
 * A játéktábla osztály. Kezeli az entitásokat (Snake, Food, Rock), és
 * biztosítja a spawnolási, elérhetőségi, és frissítési logikát.
 */
public class GameBoard {

    private final int size;
    private final Cell[][] cells;
    private final Rock[] rocks;
    private Snake currentSnake; // utoljára frissített kígyó referenciája

    private final int[] DX = {1, -1, 0, 0};
    private final int[] DY = {0, 0, 1, -1};

    public GameBoard(int size, int rockCount) {
        this.size = size;
        this.cells = new Cell[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                cells[x][y] = new Cell(new Position(x,y));
            }
        }
        this.rocks = new Rock[rockCount];
    }

    public int getSize() {
        return size;
    }

    public Rock[] getRocks() {
        return rocks;
    }

    public Cell getCell(int x, int y) {
        if (x < 0 || y < 0 || x >= size || y >= size) return null;
        return cells[y][x];
    }

    /**
     * A tábla frissítése a kígyó aktuális állapota alapján.
     */
    public void refreshSnake(Snake snake) {
        // előbb töröljük az előző snake segmenteket
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (cells[y][x].getEntity() instanceof SnakeSegment) {
                    cells[y][x].setEntity(null);
                }
            }
        }

        // majd felrajzoljuk az új kígyót
        for (SnakeSegment segment : snake.getSegments()) {
            Position pos = segment.getPosition();
            if (pos != null && inBounds(pos)) {
                cells[pos.getY()][pos.getX()].setEntity(segment);
            }
        }

        // elmentjük referenciának
        this.currentSnake = snake;
    }

    /* ---------------------------------------------------- */
    /* ----------------- HELPER METÓDUSOK ----------------- */
    /* ---------------------------------------------------- */

    private boolean inBounds(Position p) {
        return p != null && p.getX() >= 0 && p.getY() >= 0 && p.getX() < size && p.getY() < size;
    }

    private boolean isRockAt(Position p) {
        if (!inBounds(p) || rocks == null) return false;
        for (Rock r : rocks) {
            if (r != null && r.getPosition() != null && r.getPosition().equals(p)) return true;
        }
        return false;
    }

    private boolean isSnakeAt(Position p) {
        if (!inBounds(p) || currentSnake == null || currentSnake.getSegments() == null) return false;
        for (SnakeSegment seg : currentSnake.getSegments()) {
            if (seg.getPosition() != null && seg.getPosition().equals(p)) return true;
        }
        return false;
    }

    private boolean isFreeConsidering(Position p, Set<Position> extraRocks) {
        if (!inBounds(p)) return false;
        if (isSnakeAt(p)) return false;
        if (isRockAt(p)) return false;
        if (extraRocks != null && extraRocks.contains(p)) return false;
        if (Food.getInstance().getPosition() != null && Food.getInstance().getPosition().equals(p)) return false;
        return true;
    }

    private int blockedNeighborsIf(Position center, Set<Position> extraRocks) {
        int blocked = 0;
        for (int i = 0; i < 4; i++) {
            Position n = new Position(center.getX() + DX[i], center.getY() + DY[i]);
            boolean out = !inBounds(n);
            boolean rockNow = isRockAt(n);
            boolean rockExtra = extraRocks != null && extraRocks.contains(n);
            if (out || rockNow || rockExtra) blocked++;
        }
        return blocked;
    }

    private int reachableFreeCount(Position start, Set<Position> extraRocks) {
        if (start == null || !inBounds(start)) return 0;

        boolean[][] vis = new boolean[size][size];
        ArrayDeque<Position> dq = new ArrayDeque<>();

        if (isFreeConsidering(start, extraRocks)) {
            dq.add(start);
            vis[start.getY()][start.getX()] = true;
        }

        int count = 0;
        while (!dq.isEmpty()) {
            Position p = dq.poll();
            count++;
            for (int i = 0; i < 4; i++) {
                Position n = new Position(p.getX() + DX[i], p.getY() + DY[i]);
                if (!inBounds(n)) continue;
                if (vis[n.getY()][n.getX()]) continue;
                if (!isFreeConsidering(n, extraRocks)) continue;
                vis[n.getY()][n.getX()] = true;
                dq.add(n);
            }
        }
        return count;
    }

    private int totalFreeCells(Set<Position> extraRocks) {
        int total = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Position p = new Position(x, y);
                if (isFreeConsidering(p, extraRocks)) total++;
            }
        }
        return total;
    }

    /* ---------------------------------------------------- */
    /* ------------------- FOOD SPAWN --------------------- */
    /* ---------------------------------------------------- */

    public void spawnFood() {
        ArrayList<Position> free = new ArrayList<>();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Position p = new Position(x, y);
                if (!isRockAt(p) && !isSnakeAt(p)) {
                    free.add(p);
                }
            }
        }
        if (free.isEmpty()) return;
        Collections.shuffle(free);
        Food.getInstance().setPosition(free.get(0));
    }

    /* ---------------------------------------------------- */
    /* ------------------- ROCK SPAWN --------------------- */
    /* ---------------------------------------------------- */

    public void spawnRocks() {
        if (rocks == null || rocks.length == 0) return;

        Random rnd = new Random();
        int placed = 0;
        int attempts = 0;
        int maxAttempts = size * size * 10;

        Position start;
        if (currentSnake != null && !currentSnake.getSegments().isEmpty()) {
            start = currentSnake.getSegments().getFirst().getPosition();
        } else {
            int mid = size / 2;
            start = new Position(mid, mid);
        }

        HashSet<Position> tempRocks = new HashSet<>();

        while (placed < rocks.length && attempts++ < maxAttempts) {
            Position cand = new Position(rnd.nextInt(size), rnd.nextInt(size));

            if (isSnakeAt(cand) || isRockAt(cand) || tempRocks.contains(cand)) continue;
            if (Food.getInstance().getPosition() != null && Food.getInstance().getPosition().equals(cand)) continue;

            // ideiglenesen hozzáadjuk, hogy a BFS és dead-end ellenőrzés így lássa
            tempRocks.add(cand);

            boolean createsDeadEnd = false;

            // ZSÁKUTCA-VÉDELEM
            for (int i = 0; i < 4 && !createsDeadEnd; i++) {
                Position neighbor = new Position(cand.getX() + DX[i], cand.getY() + DY[i]);
                if (!inBounds(neighbor)) continue;
                int blocked = blockedNeighborsIf(neighbor, tempRocks);
                if (blocked >= 3) {
                    createsDeadEnd = true;
                }
            }

            // PÁLYASZÉL-VÉDELEM: ne legyen két egymás melletti kő a szélén
            if (!createsDeadEnd) {
                if (cand.getX() == 0 || cand.getX() == size - 1) {
                    for (int dy : DY) {
                        Position neighbor = new Position(cand.getX(), cand.getY() + dy);
                        if (inBounds(neighbor) && (isRockAt(neighbor) || tempRocks.contains(neighbor))) {
                            createsDeadEnd = true;
                        }
                    }
                }
                if (cand.getY() == 0 || cand.getY() == size - 1) {
                    for (int dx : DX) {
                        Position neighbor = new Position(cand.getX() + dx, cand.getY());
                        if (inBounds(neighbor) && (isRockAt(neighbor) || tempRocks.contains(neighbor))) {
                            createsDeadEnd = true;
                        }
                    }
                }
            }

            // ELÉRHETŐSÉG-VÉDELEM (ne szigetesedjen)
            boolean breaksConnectivity = false;
            if (!createsDeadEnd) {
                int reachable = reachableFreeCount(start, tempRocks);
                int total = totalFreeCells(tempRocks);
                breaksConnectivity = (reachable != total);
            }

            if (createsDeadEnd || breaksConnectivity) {
                tempRocks.remove(cand);
                continue;
            }

            // ha idáig eljutott, a hely jó
            rocks[placed] = new Rock(cand);
            placed++;
        }
    }

    /* ---------------------------------------------------- */
    /* ------------------- EGYÉB -------------------------- */
    /* ---------------------------------------------------- */

    public boolean isOutOfBounds(Position pos) {
        return pos.getX() < 0 || pos.getY() < 0 || pos.getX() >= size || pos.getY() >= size;
    }
}
