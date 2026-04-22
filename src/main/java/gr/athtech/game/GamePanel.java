package gr.athtech.game;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class GamePanel extends JPanel implements ActionListener {

    // Rithmiseis plegmatos kai parathyrou
    final int TILE_SIZE = 40;
    final int MAX_SCREEN_COL = 22;
    final int MAX_SCREEN_ROW = 22;
    final int SCREEN_WIDTH = TILE_SIZE * MAX_SCREEN_COL;
    final int SCREEN_HEIGHT = TILE_SIZE * MAX_SCREEN_ROW;

    Timer gameTimer;
    final int BASE_DELAY = 150;
    int currentDelay = BASE_DELAY;

    // Voithitikes domes gia ta plakidia kai ta frouta
    record Tile(int x, int y, boolean isJump) {
        public Tile(int x, int y) {
            this(x, y, false);
        }
    }
    record Fruit(int x, int y, int seqNum, int imageId) {}

    // Lises antikeimenwn pou yparxoun stin othoni
    ArrayList<Tile> snake;
    ArrayList<Fruit> fruits;
    ArrayList<Tile> rocks;
    ArrayList<Tile> mushrooms;

    SoundManager sound = new SoundManager();
    ImageManager images = new ImageManager();

    char direction = 'R';
    char lastDirection = 'R';

    // State variables tou paixnidiou
    boolean isMoving = false;
    boolean gameOver = false;
    boolean gameWon = false;
    boolean levelComplete = false;
    boolean wantsToJump = false;

    boolean showingMainMenu = true;
    boolean showingControls = false;
    boolean isPaused = false;
    int menuOption = 0;
    boolean showingTutorial = false;

    boolean isCountingDown = false;
    long countdownStart;
    int countdownNum = 3;

    // Metavlites logikhs (Gameplay)
    int expectedFruit = 1;
    int triosCompleted = 0;
    int currentLevel = 1;
    Random random;

    long startTime;
    long pausedTimeObj;
    int secondsElapsed = 0;
    int finalTimeSeconds = 0;
    int mistakes = 0;
    int starsEarned = 0;

    int bestTimeL1 = 9999;
    int bestTimeL2 = 9999;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        random = new Random();
        loadHighScores();
        playMusic(6);

        if (gameTimer == null) {
            gameTimer = new Timer(currentDelay, this);
            gameTimer.start();
        }
    }

    // Helper methods gia thaxristi diaxeirisi ixou
    public void playSound(int i) { sound.playSFX(i); }
    public void playMusic(int i) { sound.playBGM(i); }
    public void stopMusic() { sound.stopBGM(); }

    // Fortwnei ta highscores apo arxeio keimenou (an yparxei)
    private void loadHighScores() {
        try {
            File scoreFile = new File("highscores.txt");
            if (scoreFile.exists()) {
                Scanner scanner = new Scanner(scoreFile);
                if (scanner.hasNextInt()) bestTimeL1 = scanner.nextInt();
                if (scanner.hasNextInt()) bestTimeL2 = scanner.nextInt();
                scanner.close();
            }
        } catch (Exception e) {
            System.out.println("De vrethike to arxeio apothikeusis.");
        }
    }

    // Apothikeuei ta nea highscores sto arxeio
    private void saveHighScores() {
        try {
            FileWriter writer = new FileWriter("highscores.txt");
            writer.write(bestTimeL1 + "\n" + bestTimeL2);
            writer.close();
        } catch (IOException e) {
            System.out.println("Adunamia apothikeushs twn scores.");
        }
    }

    // Arxikopoihsh twn dedomenwn gia na ksekinisei to Level 1
    public void startGame() {
        stopMusic();
        playMusic(7);
        sound.setVolume(-15.0f);

        snake = new ArrayList<>();
        fruits = new ArrayList<>();
        rocks = new ArrayList<>();
        mushrooms = new ArrayList<>();

        expectedFruit = 1;
        triosCompleted = 0;
        currentLevel = 1;

        direction = 'R';
        lastDirection = 'R';

        gameOver = false;
        gameWon = false;
        levelComplete = false;
        isMoving = false;
        wantsToJump = false;
        isPaused = false;

        showingMainMenu = false;
        showingControls = false;
        showingTutorial = true;
        isCountingDown = false;

        mistakes = 0;
        secondsElapsed = 0;

        currentDelay = BASE_DELAY;
        gameTimer.setDelay(currentDelay);

        resetSnake();
        spawnFruits();
    }

    // Epanaferei to fidi stin kentriki thesi tou plegmatos
    public void resetSnake() {
        snake.clear();
        snake.add(new Tile(11, 11));
        snake.add(new Tile(10, 11));
        snake.add(new Tile(9, 11));

        direction = 'R';
        lastDirection = 'R';
        isMoving = false;
    }

    // Arxikopoihsh logikis kai perivallontos gia to Level 2
    public void loadLevel2() {
        stopMusic();
        playMusic(7);
        sound.setVolume(-15.0f);

        currentLevel = 2;
        triosCompleted = 0;
        rocks.clear();
        mushrooms.clear();
        levelComplete = false;

        showingTutorial = true;
        isCountingDown = false;
        isMoving = false;

        mistakes = 0;
        secondsElapsed = 0;

        currentDelay = BASE_DELAY;
        gameTimer.setDelay(currentDelay);

        // Vazei ta empodia sta pio "krisima" simeia tou xarti
        rocks.add(new Tile(6, 6));
        rocks.add(new Tile(6, 7));
        rocks.add(new Tile(15, 15));
        rocks.add(new Tile(16, 15));
        rocks.add(new Tile(11, 5));
        rocks.add(new Tile(11, 16));

        resetSnake();
        spawnFruits();
    }

    // Ypologismos tou score (asteria) vasi tou xronou kai twn lathwn poy eginan
    public void evaluateLevel() {
        finalTimeSeconds = (int)((System.currentTimeMillis() - startTime) / 1000);

        if (currentLevel == 1) {
            if (finalTimeSeconds < 60 && mistakes <= 1) starsEarned = 3;
            else if (finalTimeSeconds < 90 && mistakes <= 3) starsEarned = 2;
            else starsEarned = 1;

            if (starsEarned >= 2 && finalTimeSeconds < bestTimeL1) {
                bestTimeL1 = finalTimeSeconds;
                saveHighScores();
            }
        } else if (currentLevel == 2) {
            if (finalTimeSeconds < 50 && mistakes == 0) starsEarned = 3;
            else if (finalTimeSeconds < 80 && mistakes <= 3) starsEarned = 2;
            else starsEarned = 1;

            if (finalTimeSeconds < bestTimeL2) {
                bestTimeL2 = finalTimeSeconds;
                saveHighScores();
            }
        }
    }

    // Fortwnei ena neo manitari se tuxaia "asfalh" thesi (menei panta stathero)
    public void spawnMushroom() {
        int randomX, randomY;
        boolean safeToSpawn;
        do {
            safeToSpawn = true;
            randomX = random.nextInt(MAX_SCREEN_COL - 4) + 2;
            randomY = random.nextInt(MAX_SCREEN_ROW - 4) + 2;

            for (Tile t : snake) if (t.x() == randomX && t.y() == randomY) safeToSpawn = false;
            for (Fruit f : fruits) if (f.x() == randomX && f.y() == randomY) safeToSpawn = false;
            for (Tile r : rocks) if (r.x() == randomX && r.y() == randomY) safeToSpawn = false;
            for (Tile m : mushrooms) if (m.x() == randomX && m.y() == randomY) safeToSpawn = false;

        } while (!safeToSpawn);
        mushrooms.add(new Tile(randomX, randomY));
    }

    // Fortwnei ta 3 frouta tou epomenou gameloop, tuxaia kanontas mix stis eikones
    public void spawnFruits() {
        fruits.clear();
        expectedFruit = 1;
        Tile currentHead = snake.get(0);
        Tile dangerZone = getNextHeadPosition(currentHead, direction);

        ArrayList<Integer> availableImageIds = new ArrayList<>();
        for (int i = 1; i <= 5; i++) availableImageIds.add(i);
        Collections.shuffle(availableImageIds, random);

        for (int i = 1; i <= 3; i++) {
            int randomX, randomY;
            boolean safeToSpawn;
            do {
                safeToSpawn = true;
                randomX = random.nextInt(MAX_SCREEN_COL - 4) + 2;
                randomY = random.nextInt(MAX_SCREEN_ROW - 4) + 2;

                for (Tile t : snake) if (t.x() == randomX && t.y() == randomY) safeToSpawn = false;
                if (dangerZone.x() == randomX && dangerZone.y() == randomY) safeToSpawn = false;
                for (Fruit f : fruits) if (f.x() == randomX && f.y() == randomY) safeToSpawn = false;
                for (Tile r : rocks) if (r.x() == randomX && r.y() == randomY) safeToSpawn = false;
                for (Tile m : mushrooms) if (m.x() == randomX && m.y() == randomY) safeToSpawn = false;
            } while (!safeToSpawn);

            fruits.add(new Fruit(randomX, randomY, i, availableImageIds.get(i - 1)));
        }
    }

    // Ypologizei to epomeno keli sto opoio prokeitai na paei to kefali tou fidiou
    private Tile getNextHeadPosition(Tile currentHead, char dir) {
        return switch (dir) {
            case 'U' -> new Tile(currentHead.x(), currentHead.y() - 1);
            case 'D' -> new Tile(currentHead.x(), currentHead.y() + 1);
            case 'L' -> new Tile(currentHead.x() - 1, currentHead.y());
            case 'R' -> new Tile(currentHead.x() + 1, currentHead.y());
            default -> currentHead;
        };
    }

    // To kurio game loop pou kaleitai apo to gameTimer
    @Override
    public void actionPerformed(ActionEvent e) {
        if (showingMainMenu || showingControls || isPaused) {
            repaint();
            return;
        }

        if (isCountingDown) {
            long elapsed = (System.currentTimeMillis() - countdownStart) / 1000;
            countdownNum = 3 - (int)elapsed;

            if (countdownNum < 0) {
                isCountingDown = false;
                isMoving = true;
                startTime = System.currentTimeMillis();
            }
            repaint();
            return;
        }

        if (isMoving && !gameOver && !gameWon && !levelComplete && !showingTutorial) {
            lastDirection = direction;

            move();
            checkCollisions();
            secondsElapsed = (int)((System.currentTimeMillis() - startTime) / 1000);
        }
        repaint();
    }

    // Kinisi tou fidiou (kavontas xrisi tis pliroforias an prokeitai na pidiksei to kefali)
    public void move() {
        Tile currentHead = snake.get(0);
        Tile newHead;
        Tile bridgedTile = null;

        if (wantsToJump) {
            bridgedTile = getNextHeadPosition(currentHead, direction);
            newHead = getNextHeadPosition(bridgedTile, direction);
            wantsToJump = false;
            playSound(2);
        } else {
            newHead = getNextHeadPosition(currentHead, direction);
        }

        boolean ateFruit = false;
        for (Fruit f : fruits) {
            if (newHead.x() == f.x() && newHead.y() == f.y()) {
                ateFruit = true;
                break;
            }
        }

        if (bridgedTile != null) {
            snake.add(0, new Tile(bridgedTile.x(), bridgedTile.y(), true));
            snake.add(0, newHead);

            if (!ateFruit) {
                snake.remove(snake.size() - 1);
                snake.remove(snake.size() - 1);
            } else {
                snake.remove(snake.size() - 1);
            }
        } else {
            snake.add(0, newHead);
            if (!ateFruit) {
                snake.remove(snake.size() - 1);
            }
        }
    }

    // Elenxos gia oles tis pithanes sigkrouseis (tixous, pema fidiou, perivallon)
    public void checkCollisions() {
        Tile head = snake.get(0);

        if (head.x() <= 1 || head.x() >= MAX_SCREEN_COL - 2 || head.y() <= 1 || head.y() >= MAX_SCREEN_ROW - 2) gameOver = true;
        for (int i = 1; i < snake.size(); i++) if (head.x() == snake.get(i).x() && head.y() == snake.get(i).y()) gameOver = true;
        for (Tile r : rocks) if (head.x() == r.x() && head.y() == r.y()) gameOver = true;

        if (gameOver) {
            stopMusic();
            playSound(3);
            return;
        }

        for (Tile m : mushrooms) {
            if (head.x() == m.x() && head.y() == m.y()) {
                gameOver = true;
                stopMusic();
                playSound(4);
                return;
            }
        }

        Fruit eatenFruit = null;
        for (Fruit f : fruits) {
            if (head.x() == f.x() && head.y() == f.y()) {
                eatenFruit = f;
                break;
            }
        }

        if (eatenFruit != null) {
            fruits.remove(eatenFruit);

            // Elenxei an to frouto pou fagate einai to epomeno sth seira
            if (eatenFruit.seqNum() == expectedFruit) {
                expectedFruit++;
                playSound(0);

                if (fruits.isEmpty()) {
                    triosCompleted++;

                    if (currentLevel == 1 && triosCompleted >= 6) {
                        evaluateLevel();
                        levelComplete = true;
                        isMoving = false;
                        stopMusic();
                        playSound(5);
                    }
                    else if (currentLevel == 2 && triosCompleted >= 6) {
                        evaluateLevel();
                        gameWon = true;
                        isMoving = false;
                        stopMusic();
                        playSound(5);
                    }
                    else {
                        // Afkshsh ths taxythitas kathe fora pou o paixtis teliwnei ena trio (mono sto lvl 2)
                        if (currentLevel == 2) {
                            currentDelay = Math.max(50, currentDelay - 5);
                            gameTimer.setDelay(currentDelay);
                            if (triosCompleted % 2 == 0) spawnMushroom();
                        }
                        spawnFruits();
                    }
                }
            } else {
                mistakes++;
                playSound(1);
                spawnFruits();
            }
        }
    }

    // Boithitiki methodos pou voithaei na zografisoume ta plakidia periballontos stis sostes gwnies.
    private void drawRotatedImage(Graphics g, BufferedImage img, int x, int y, int angle) {
        if (img == null) return;
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.rotate(Math.toRadians(angle), x + (TILE_SIZE / 2.0), y + (TILE_SIZE / 2.0));
        g2d.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
        g2d.dispose();
    }

    // Methodos zwgrafikhs olwn twn grafikwn stoicheiwn
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        FontMetrics metrics;

        // --- ARXIKI OTHONI / MENU ---
        if (showingMainMenu) {
            if (images.menuBg != null) {
                g.drawImage(images.menuBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
            } else {
                g.setColor(new Color(15, 15, 25));
                g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            }

            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(SCREEN_WIDTH / 2 - 250, SCREEN_HEIGHT / 2 - 190, 500, 450, 40, 40);

            g.setFont(new Font("Impact", Font.ITALIC, 85));
            metrics = getFontMetrics(g.getFont());
            g.setColor(new Color(0, 100, 0));
            g.drawString("SNAKE", (SCREEN_WIDTH - metrics.stringWidth("SNAKE")) / 2 + 5, SCREEN_HEIGHT / 2 - 85);
            g.setColor(new Color(50, 255, 50));
            g.drawString("SNAKE", (SCREEN_WIDTH - metrics.stringWidth("SNAKE")) / 2, SCREEN_HEIGHT / 2 - 90);

            g.setFont(new Font("Impact", Font.PLAIN, 45));
            metrics = getFontMetrics(g.getFont());
            g.setColor(new Color(150, 0, 0));
            g.drawString("SURVIVOR", (SCREEN_WIDTH - metrics.stringWidth("SURVIVOR")) / 2 + 3, SCREEN_HEIGHT / 2 - 37);
            g.setColor(Color.WHITE);
            g.drawString("SURVIVOR", (SCREEN_WIDTH - metrics.stringWidth("SURVIVOR")) / 2, SCREEN_HEIGHT / 2 - 40);

            g.drawImage(images.head, SCREEN_WIDTH / 2 - 160, SCREEN_HEIGHT / 2 - 160, 60, 60, null);
            g.drawImage(images.fruits[2], SCREEN_WIDTH / 2 + 100, SCREEN_HEIGHT / 2 - 160, 60, 60, null);

            g.setFont(new Font("Monospaced", Font.BOLD, 30));
            metrics = getFontMetrics(g.getFont());

            String[] options = {"START GAME", "CONTROLS", "QUIT"};
            for (int i = 0; i < options.length; i++) {
                int yPos = SCREEN_HEIGHT / 2 + 70 + (i * 60);

                if (menuOption == i) {
                    g.setColor(Color.YELLOW);
                    g.drawImage(images.head, (SCREEN_WIDTH - metrics.stringWidth(options[i])) / 2 - 50, yPos - 30, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                }
                g.drawString(options[i], (SCREEN_WIDTH - metrics.stringWidth(options[i])) / 2, yPos);
            }

            // --- PINAQIDA ME TA CREDITS ---
            g.setFont(new Font("Monospaced", Font.PLAIN, 15));
            String creditsLine1 = "Developed by:";
            String creditsLine2 = "Evangelos Dimovits, Sofia Loukissa and Anastasia Kouridaki";

            metrics = getFontMetrics(g.getFont());
            int boxWidth = Math.max(metrics.stringWidth(creditsLine1), metrics.stringWidth(creditsLine2)) + 30;
            int boxHeight = 55;
            int boxX = SCREEN_WIDTH - boxWidth - 15;
            int boxY = SCREEN_HEIGHT - boxHeight - 15;

            // Fonto koutiou
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

            // Perigramma
            g.setColor(new Color(50, 255, 50));
            g.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

            // Keimena
            g.setColor(Color.WHITE);
            g.drawString(creditsLine1, boxX + 15, boxY + 22);
            g.setColor(Color.YELLOW);
            g.drawString(creditsLine2, boxX + 15, boxY + 42);

            // --- EKDOSH GAME ---
            g.setColor(new Color(255, 255, 255, 150));
            g.setFont(new Font("Monospaced", Font.BOLD, 14));
            g.drawString("v1.0", 15, SCREEN_HEIGHT - 15);

            return;
        }

        // --- OTHONI ODIGIWN ---
        if (showingControls) {
            if (images.menuBg != null) {
                g.drawImage(images.menuBg, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            } else {
                g.setColor(new Color(20, 20, 40));
                g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
            }

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Impact", Font.PLAIN, 45));
            metrics = getFontMetrics(g.getFont());
            g.drawString("HOW TO PLAY", (SCREEN_WIDTH - metrics.stringWidth("HOW TO PLAY")) / 2, 100);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.PLAIN, 20));
            g.drawString("> Use ARROW KEYS to navigate.", 100, 200);
            g.drawString("> Press SPACEBAR to jump over hazards.", 100, 250);
            g.drawString("> Press 'P' or 'ESC' to pause the game.", 100, 300);
            g.drawString("> Press 'M' to mute audio.", 100, 350);
            g.drawString("> Eat fruits in numerical order (1, 2, 3).", 100, 400);

            g.setColor(Color.CYAN);
            g.setFont(new Font("Monospaced", Font.BOLD, 25));
            metrics = getFontMetrics(g.getFont());
            g.drawString("[ PRESS ENTER TO RETURN ]", (SCREEN_WIDTH - metrics.stringWidth("[ PRESS ENTER TO RETURN ]")) / 2, 600);

            return;
        }

        // --- SCHEDIASMOS GAMEPLAY OTHONIS ---
        for (int i = 0; i < MAX_SCREEN_COL; i++) {
            for (int j = 0; j < MAX_SCREEN_ROW; j++) {

                // Eswterikos kai eksoterikos perigrammos (Nero kai Dentra)
                if (i == 0 || i == MAX_SCREEN_COL - 1 || j == 0 || j == MAX_SCREEN_ROW - 1) {

                    if (i == 0 && j == 0) {
                        drawRotatedImage(g, images.waterCorner, i * TILE_SIZE, j * TILE_SIZE, 270);
                    } else if (i == MAX_SCREEN_COL - 1 && j == 0) {
                        drawRotatedImage(g, images.waterCorner, i * TILE_SIZE, j * TILE_SIZE, 0);
                    } else if (i == MAX_SCREEN_COL - 1 && j == MAX_SCREEN_ROW - 1) {
                        drawRotatedImage(g, images.waterCorner, i * TILE_SIZE, j * TILE_SIZE, 90);
                    } else if (i == 0 && j == MAX_SCREEN_ROW - 1) {
                        drawRotatedImage(g, images.waterCorner, i * TILE_SIZE, j * TILE_SIZE, 180);
                    }
                    else if (j == 0) {
                        drawRotatedImage(g, images.waterSide, i * TILE_SIZE, j * TILE_SIZE, 0);
                    } else if (i == MAX_SCREEN_COL - 1) {
                        drawRotatedImage(g, images.waterSide, i * TILE_SIZE, j * TILE_SIZE, 90);
                    } else if (j == MAX_SCREEN_ROW - 1) {
                        drawRotatedImage(g, images.waterSide, i * TILE_SIZE, j * TILE_SIZE, 180);
                    } else if (i == 0) {
                        drawRotatedImage(g, images.waterSide, i * TILE_SIZE, j * TILE_SIZE, 270);
                    }
                }
                else if (i == 1 || i == MAX_SCREEN_COL - 2 || j == 1 || j == MAX_SCREEN_ROW - 2) {
                    g.drawImage(images.wall, i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
                else {
                    if ((i + j) % 2 == 0) g.setColor(new Color(25, 35, 25));
                    else g.setColor(new Color(20, 30, 20));
                    g.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Zwgrafizei ta empodia tou perivallontos
        for (Tile r : rocks) g.drawImage(images.rock, r.x() * TILE_SIZE, r.y() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        for (Tile m : mushrooms) g.drawImage(images.mushroom, m.x() * TILE_SIZE, m.y() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);

        // Zwgrafizei ta frouta me ton antistoixo arithmo stin aristerh panw gwnia
        for (Fruit f : fruits) {
            g.drawImage(images.fruits[f.imageId()], f.x() * TILE_SIZE, f.y() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);

            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(f.x() * TILE_SIZE + 2, f.y() * TILE_SIZE + 2, 14, 16);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(String.valueOf(f.seqNum()), f.x() * TILE_SIZE + 5, f.y() * TILE_SIZE + 14);
        }

        // Zwgrafizei to swma tou fidiou, lamvanontas ypopsin tin apoxrwsh sto alma
        for (int i = 0; i < snake.size(); i++) {
            Tile t = snake.get(i);
            if (i == 0) g.drawImage(images.head, t.x() * TILE_SIZE, t.y() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            else if (t.isJump()) g.drawImage(images.bodyShadow, t.x() * TILE_SIZE, t.y() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            else g.drawImage(images.body, t.x() * TILE_SIZE, t.y() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        }

        // --- STATISTIKA PAIXNIDIOU (TOP BAR) ---
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, SCREEN_WIDTH, 30);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 15));

        int currentBest = (currentLevel == 1) ? bestTimeL1 : bestTimeL2;
        String bestScoreText = (currentBest == 9999) ? "--" : currentBest + "s";
        String muteText = sound.isMuted ? "[Muted] " : "";
        g.drawString(muteText + "Lvl: " + currentLevel + " | Trios: " + triosCompleted + "/6 | Time: " + secondsElapsed + "s | Best: " + bestScoreText + " | Mistakes: " + mistakes, 10, 20);

        // --- OTHONH TUTORIAL PRIN APO TO LEVEL ---
        if (showingTutorial) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Impact", Font.PLAIN, 40));
            metrics = getFontMetrics(g.getFont());
            String title = currentLevel == 1 ? "LEVEL 1: LEARNING PHASE" : "LEVEL 2: SKILL PHASE";
            g.drawString(title, (SCREEN_WIDTH - metrics.stringWidth(title)) / 2, SCREEN_HEIGHT / 2 - 120);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.PLAIN, 18));
            metrics = getFontMetrics(g.getFont());

            if (currentLevel == 1) {
                g.drawString("Use ARROW KEYS to move. Spacebar to JUMP.", (SCREEN_WIDTH - metrics.stringWidth("Use ARROW KEYS to move. Spacebar to JUMP.")) / 2, SCREEN_HEIGHT / 2 - 40);
                g.drawString("Eat fruits in numerical order: 1, then 2, then 3.", (SCREEN_WIDTH - metrics.stringWidth("Eat fruits in numerical order: 1, then 2, then 3.")) / 2, SCREEN_HEIGHT / 2);
                g.drawString("Avoid hitting the trees or your own tail!", (SCREEN_WIDTH - metrics.stringWidth("Avoid hitting the trees or your own tail!")) / 2, SCREEN_HEIGHT / 2 + 40);
            } else {
                g.drawString("The snake will now speed up over time!", (SCREEN_WIDTH - metrics.stringWidth("The snake will now speed up over time!")) / 2, SCREEN_HEIGHT / 2 - 40);
                g.drawString("Avoid Gray Rocks. DO NOT eat Purple Mushrooms.", (SCREEN_WIDTH - metrics.stringWidth("Avoid Gray Rocks. DO NOT eat Purple Mushrooms.")) / 2, SCREEN_HEIGHT / 2);
                g.drawString("Use the Jump mechanic to escape tight spaces.", (SCREEN_WIDTH - metrics.stringWidth("Use the Jump mechanic to escape tight spaces.")) / 2, SCREEN_HEIGHT / 2 + 40);
            }

            g.setColor(Color.CYAN);
            g.setFont(new Font("Monospaced", Font.BOLD, 25));
            metrics = getFontMetrics(g.getFont());
            g.drawString("[ PRESS ENTER TO BEGIN ]", (SCREEN_WIDTH - metrics.stringWidth("[ PRESS ENTER TO BEGIN ]")) / 2, SCREEN_HEIGHT / 2 + 120);
        }
        else if (isCountingDown) { // --- OTHONH COUNTDOWN ---
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Impact", Font.PLAIN, 100));
            metrics = getFontMetrics(g.getFont());
            String countText = countdownNum > 0 ? String.valueOf(countdownNum) : "GO!";
            g.drawString(countText, (SCREEN_WIDTH - metrics.stringWidth(countText)) / 2, SCREEN_HEIGHT / 2 + 30);
        }
        else if (isPaused) { // --- OTHONH PAYSIS ---
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Impact", Font.PLAIN, 60));
            metrics = getFontMetrics(g.getFont());
            g.drawString("PAUSED", (SCREEN_WIDTH - metrics.stringWidth("PAUSED")) / 2, SCREEN_HEIGHT / 2 - 20);

            g.setFont(new Font("Monospaced", Font.PLAIN, 20));
            metrics = getFontMetrics(g.getFont());
            g.drawString("Press 'P' to Resume", (SCREEN_WIDTH - metrics.stringWidth("Press 'P' to Resume")) / 2, SCREEN_HEIGHT / 2 + 30);
        }
        else if (gameOver || gameWon || levelComplete) { // --- TELIKI OTHONH APOTELESMATWN ---
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

            if (gameOver) {
                g.setColor(Color.RED);
                g.setFont(new Font("Impact", Font.PLAIN, 70));
                metrics = getFontMetrics(g.getFont());
                g.drawString("GAME OVER", (SCREEN_WIDTH - metrics.stringWidth("GAME OVER")) / 2, SCREEN_HEIGHT / 2 - 50);
            } else {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Impact", Font.PLAIN, 60));
                metrics = getFontMetrics(g.getFont());
                String title = gameWon ? "VICTORY!" : "LEVEL 1 COMPLETE";
                g.drawString(title, (SCREEN_WIDTH - metrics.stringWidth(title)) / 2, SCREEN_HEIGHT / 2 - 80);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 30));
                metrics = getFontMetrics(g.getFont());

                String starText = "Stars Earned: ";
                int textWidth = metrics.stringWidth(starText);
                int totalWidth = textWidth + (starsEarned * 40);
                int startX = (SCREEN_WIDTH - totalWidth) / 2;

                g.drawString(starText, startX, SCREEN_HEIGHT / 2 - 15);

                // Emfanizei tis eikones tou asteriou analogws thn vathmologia
                for(int i = 0; i < starsEarned; i++) {
                    g.drawImage(images.star, startX + textWidth + (i * 45), SCREEN_HEIGHT / 2 - 45, 40, 40, null);
                }

                g.setFont(new Font("Monospaced", Font.PLAIN, 20));
                metrics = getFontMetrics(g.getFont());
                String stats = "Time: " + finalTimeSeconds + "s  |  Mistakes: " + mistakes;
                g.drawString(stats, (SCREEN_WIDTH - metrics.stringWidth(stats)) / 2, SCREEN_HEIGHT / 2 + 30);

                if (finalTimeSeconds == currentBest) {
                    g.setColor(Color.CYAN);
                    g.drawString("NEW HIGH SCORE!", (SCREEN_WIDTH - metrics.stringWidth("NEW HIGH SCORE!")) / 2, SCREEN_HEIGHT / 2 + 60);
                }
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 22));
            metrics = getFontMetrics(g.getFont());

            if (levelComplete && !gameWon) {
                if (starsEarned >= 2) {
                    g.drawString("Press 'ENTER' for Level 2", (SCREEN_WIDTH - metrics.stringWidth("Press 'ENTER' for Level 2")) / 2, SCREEN_HEIGHT / 2 + 110);
                } else {
                    g.setColor(Color.RED);
                    g.drawString("You need 2 Stars to advance!", (SCREEN_WIDTH - metrics.stringWidth("You need 2 Stars to advance!")) / 2, SCREEN_HEIGHT / 2 + 110);
                    g.setColor(Color.WHITE);
                    g.drawString("Press 'R' to Retry Level 1", (SCREEN_WIDTH - metrics.stringWidth("Press 'R' to Retry Level 1")) / 2, SCREEN_HEIGHT / 2 + 150);
                }
            } else {
                g.drawString("Press 'R' to Play Again", (SCREEN_WIDTH - metrics.stringWidth("Press 'R' to Play Again")) / 2, SCREEN_HEIGHT / 2 + 110);
                g.drawString("Press 'M' for Main Menu", (SCREEN_WIDTH - metrics.stringWidth("Press 'M' for Main Menu")) / 2, SCREEN_HEIGHT / 2 + 150);
            }
        }
    }

    // Klasisi pou akouei kai epeksergazetai tis energeies tou keyboard
    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_M && !gameOver && !gameWon && !levelComplete) {
                sound.toggleMute();
                repaint();
                return;
            }

            if (showingMainMenu) {
                if (key == KeyEvent.VK_UP) {
                    menuOption--;
                    if (menuOption < 0) menuOption = 2;
                    playSound(2);
                    repaint();
                } else if (key == KeyEvent.VK_DOWN) {
                    menuOption++;
                    if (menuOption > 2) menuOption = 0;
                    playSound(2);
                    repaint();
                } else if (key == KeyEvent.VK_ENTER) {
                    if (menuOption == 0) {
                        startGame();
                    } else if (menuOption == 1) {
                        showingMainMenu = false;
                        showingControls = true;
                        repaint();
                    } else if (menuOption == 2) {
                        System.exit(0);
                    }
                }
                return;
            }

            if (showingControls) {
                if (key == KeyEvent.VK_ENTER) {
                    showingControls = false;
                    showingMainMenu = true;
                    repaint();
                }
                return;
            }

            if (showingTutorial) {
                if (key == KeyEvent.VK_ENTER) {
                    showingTutorial = false;
                    isCountingDown = true;
                    countdownStart = System.currentTimeMillis();
                }
                return;
            }

            if (isCountingDown) return;

            if (gameOver || gameWon) {
                if (key == KeyEvent.VK_R) startGame();
                else if (key == KeyEvent.VK_M) {
                    showingMainMenu = true;
                    gameOver = false;
                    gameWon = false;
                    stopMusic();
                    playMusic(6);
                    sound.setVolume(0.0f);
                    repaint();
                }
                return;
            }

            if (levelComplete) {
                if (starsEarned >= 2 && key == KeyEvent.VK_ENTER) {
                    loadLevel2();
                } else if (key == KeyEvent.VK_R) {
                    startGame();
                } else if (key == KeyEvent.VK_M) {
                    showingMainMenu = true;
                    levelComplete = false;
                    stopMusic();
                    playMusic(6);
                    sound.setVolume(0.0f);
                    repaint();
                }
                return;
            }

            if (key == KeyEvent.VK_P || key == KeyEvent.VK_ESCAPE) {
                isPaused = !isPaused;
                if (isPaused) {
                    pausedTimeObj = System.currentTimeMillis();
                } else {
                    startTime += (System.currentTimeMillis() - pausedTimeObj);
                }
                repaint();
                return;
            }

            if (isPaused) return;

            if (key == KeyEvent.VK_SPACE) wantsToJump = true;

            switch (key) {
                case KeyEvent.VK_LEFT -> { if (lastDirection != 'R') direction = 'L'; }
                case KeyEvent.VK_RIGHT -> { if (lastDirection != 'L') direction = 'R'; }
                case KeyEvent.VK_UP -> { if (lastDirection != 'D') direction = 'U'; }
                case KeyEvent.VK_DOWN -> { if (lastDirection != 'U') direction = 'D'; }
            }
        }
    }
}