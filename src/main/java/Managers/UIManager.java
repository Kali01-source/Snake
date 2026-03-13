package Managers;

import Misc.*;
import Entities.*;
import Entities.Snake.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.function.Consumer;

public class UIManager extends JFrame {

    private final GameManager gm;

    private BoardPanel boardPanel;
    private JLabel scoreLabel;

    private static final int PREFERRED_BOARD_PX = 600;

    public UIManager(GameManager gm) {
        this.gm = gm;
        setTitle("Snake");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    public void showMenu() {
        getContentPane().removeAll();

        JPanel root = new JPanel(new BorderLayout());
        JLabel title = new JLabel("SNAKE", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        root.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(5, 1, 8, 8));

        JButton easy   = new JButton("Easy (1 rock)");
        JButton normal = new JButton("Normal (2 rocks)");
        JButton hard   = new JButton("Hard (4 rocks)");
        JButton start  = new JButton("Start");
        JButton exit   = new JButton("Exit");

        buttons.add(easy);
        buttons.add(normal);
        buttons.add(hard);
        buttons.add(start);
        buttons.add(exit);
        root.add(buttons, BorderLayout.CENTER);

        Font btnFont = new Font("Arial", Font.BOLD, 16);
        easy.setFont(btnFont);
        normal.setFont(btnFont);
        hard.setFont(btnFont);
        start.setFont(btnFont);
        exit.setFont(btnFont);

        Runnable updateButtons = getRunnable(easy, normal, hard);

        easy.addActionListener(e -> { gm.setDifficulty(Difficulty.EASY); updateButtons.run(); });
        normal.addActionListener(e -> { gm.setDifficulty(Difficulty.NORMAL); updateButtons.run(); });
        hard.addActionListener(e -> { gm.setDifficulty(Difficulty.HARD); updateButtons.run(); });
        start.addActionListener(e -> gm.startNewGame());
        exit.addActionListener(e -> System.exit(0));

        updateButtons.run();
        add(root, BorderLayout.CENTER);
        packAndShow();
    }

    private Runnable getRunnable(JButton easy, JButton normal, JButton hard) {
        Color selectedColor = new Color(100, 200, 100);
        Color defaultColor  = new JButton().getBackground();

        return () -> {
            Difficulty d = gm.getDifficulty();
            easy.setBackground(d == Difficulty.EASY ? selectedColor : defaultColor);
            normal.setBackground(d == Difficulty.NORMAL ? selectedColor : defaultColor);
            hard.setBackground(d == Difficulty.HARD ? selectedColor : defaultColor);

            easy.setEnabled(d != Difficulty.EASY);
            normal.setEnabled(d != Difficulty.NORMAL);
            hard.setEnabled(d != Difficulty.HARD);
        };
    }

    public void showGameScreen() {
        getContentPane().removeAll();

        scoreLabel = new JLabel("Score: 0", JLabel.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(scoreLabel, BorderLayout.NORTH);

        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        installKeyBindings(); //fókuszfüggetlenítés
        packAndShow();
        boardPanel.setFocusable(true);
        boardPanel.requestFocusInWindow();
    }

    public void showGameOverScreen(int score) {
        getContentPane().removeAll();

        JPanel root = new JPanel(new BorderLayout());
        JLabel over = new JLabel("GAME OVER — Score: " + score, JLabel.CENTER);
        over.setFont(new Font("Arial", Font.BOLD, 20));
        root.add(over, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton viewTopBtn = new JButton("View Top 10");
        JButton saveBtn    = new JButton("Save score");
        JButton backBtn    = new JButton("Back to menu");

        viewTopBtn.addActionListener(e -> {
            ArrayList<PlayerScore> top = gm.getTopScores(10);
            ArrayList<String> lines = new ArrayList<>();
            int i = 1;
            for (PlayerScore ps : top) {
                if (ps.score() > 0) {
                    lines.add((i++) + ". " + ps.playerName() + " — " + ps.score());
                }
            }
            if (lines.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No scores yet.", "Top 10",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JList<String> list = new JList<>(lines.toArray(new String[0]));
                list.setVisibleRowCount(Math.min(10, lines.size()));
                JScrollPane sp = new JScrollPane(list);
                sp.setPreferredSize(new Dimension(280, Math.min(10, lines.size()) * 22 + 8));
                JOptionPane.showMessageDialog(this, sp, "Top 10", JOptionPane.PLAIN_MESSAGE);
            }
        });

        saveBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter your name:", "Save score",
                    JOptionPane.PLAIN_MESSAGE);
            if (name != null) {
                name = name.trim();
                if (!name.isEmpty()) {
                    gm.saveScore(name);
                    JOptionPane.showMessageDialog(this, "Score saved!", "Saved",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        backBtn.addActionListener(e -> showMenu());

        bottom.add(viewTopBtn);
        bottom.add(saveBtn);
        bottom.add(backBtn);

        root.add(bottom, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
        packAndShow();
    }

    public void updateBoard(int score) {
        if (scoreLabel != null) scoreLabel.setText("Score: " + score);
        if (boardPanel != null) boardPanel.repaint();
    }

    private void packAndShow() {
        int boardPx = PREFERRED_BOARD_PX;
        setPreferredSize(new Dimension(boardPx, boardPx + 80));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class BoardPanel extends JPanel {
        BoardPanel() {
            setBackground(Color.BLACK);
            int px = PREFERRED_BOARD_PX;
            setPreferredSize(new Dimension(px, px));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            GameBoard board = gm.getBoard();
            if (board == null) return;

            int n = board.getSize();
            int cell = Math.min(getWidth(), getHeight()) / n;

            // GRID
            g.setColor(new Color(40, 40, 40));
            for (int i = 0; i <= n; i++) {
                g.drawLine(i * cell, 0, i * cell, n * cell);
                g.drawLine(0, i * cell, n * cell, i * cell);
            }

            // FOOD
            if (Food.getInstance().getPosition() != null) {
                int fx = Food.getInstance().getPosition().getX();
                int fy = Food.getInstance().getPosition().getY();
                g.setColor(Color.RED);
                g.fillOval(fx * cell, fy * cell, cell, cell);
            }

            // ROCKS
            Rock[] rocks = board.getRocks();
            if (rocks != null) {
                g.setColor(Color.GRAY);
                for (Rock r : rocks) {
                    if (r == null || r.getPosition() == null) continue;
                    int rx = r.getPosition().getX();
                    int ry = r.getPosition().getY();
                    g.fillRect(rx * cell, ry * cell, cell, cell);
                }
            }

            Snake snake = gm.getSnake();
            if (snake != null && !snake.getSegments().isEmpty()) {
                // fej
                SnakeSegment head = snake.getSegments().getFirst();
                int hx = head.getPosition().getX();
                int hy = head.getPosition().getY();
                g.setColor(new Color(80, 200, 120));
                g.fillRect(hx * cell, hy * cell, cell, cell);

                // test
                g.setColor(new Color(60, 160, 90));
                for (int i = 1; i < snake.getSegments().size()-1; i++) {
                    SnakeSegment seg = snake.getSegments().get(i);
                    int sx = seg.getPosition().getX();
                    int sy = seg.getPosition().getY();
                    g.fillRect(sx * cell, sy * cell, cell, cell);
                }

                SnakeRattle r = (SnakeRattle) snake.getSegments().getLast();
                int rx = r.getPosition().getX();
                int ry = r.getPosition().getY();
                g.setColor(new Color(129, 191, 29));
                g.fillRect(rx * cell, ry * cell, cell, cell);
            }
        }
    }

    private void installKeyBindings() {
        JPanel target = boardPanel;

        Consumer<Integer> dispatch = (keyCode) -> { //lambda a keyevent átadására
            KeyEvent ev = new KeyEvent(
                    target,
                    KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(),
                    0,
                    keyCode,
                    KeyEvent.CHAR_UNDEFINED
            );
            gm.handleKeyPress(ev);
        };

        bind(target, "UP", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), () -> dispatch.accept(KeyEvent.VK_UP));
        bind(target, "DOWN", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), () -> dispatch.accept(KeyEvent.VK_DOWN));
        bind(target, "LEFT", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), () -> dispatch.accept(KeyEvent.VK_LEFT));
        bind(target, "RIGHT", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), () -> dispatch.accept(KeyEvent.VK_RIGHT));

        bind(target, "W", KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), () -> dispatch.accept(KeyEvent.VK_W));
        bind(target, "S", KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), () -> dispatch.accept(KeyEvent.VK_S));
        bind(target, "A", KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), () -> dispatch.accept(KeyEvent.VK_A));
        bind(target, "D", KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), () -> dispatch.accept(KeyEvent.VK_D));

        bind(target, "P", KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), () -> dispatch.accept(KeyEvent.VK_P));
        bind(target, "ESC", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), () -> dispatch.accept(KeyEvent.VK_ESCAPE));
    }

    private static void bind(JPanel component, String actionName, KeyStroke ks, Runnable r) {
        component.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(ks, actionName);
        component.getActionMap().put(actionName, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { r.run(); }
        });
    }
}
