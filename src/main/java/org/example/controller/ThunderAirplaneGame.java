import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class ThunderAirplaneGame extends JFrame {

    private GamePanel gamePanel;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SPEED = 5;
    private static final int ENEMY_SPAWN_RATE = 30; // 敌机生成频率（帧数）
    private static final int BULLET_SPEED = 7;
    private static final int POWER_UP_CHANCE = 100; // 道具生成概率（1/100）

    public ThunderAirplaneGame() {
        setTitle("雷霆战机");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        setVisible(true);
    }

    public static void main(String[] args) {
        // 重定向错误输出以隐藏警告
        System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
            @Override
            public void write(int b) {
                // 什么都不做，忽略所有错误输出
            }
        }));

        SwingUtilities.invokeLater(ThunderAirplaneGame::new);
    }

    class GamePanel extends JPanel implements Runnable, KeyListener {
        private Player player;
        private ArrayList<Enemy> enemies;
        private ArrayList<Bullet> bullets;
        private ArrayList<PowerUp> powerUps;
        private Thread gameThread;
        private boolean isRunning;
        private boolean gameOver;
        private int score;
        private int level;
        private int enemySpawnCounter;
        private Random random;
        private BufferedImage background; // 使用BufferedImage避免警告

        public GamePanel() {
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setBackground(Color.BLACK);
            setFocusable(true);
            addKeyListener(this);

            player = new Player(WIDTH / 2 - 25, HEIGHT - 100);
            enemies = new ArrayList<>();
            bullets = new ArrayList<>();
            powerUps = new ArrayList<>();
            random = new Random();
            score = 0;
            level = 1;
            enemySpawnCounter = 0;
            gameOver = false;

            // 使用程序生成的背景
            background = createBackground();

            startGame();
        }

        private BufferedImage createBackground() {
            // 创建简单的星空背景
            BufferedImage bg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bg.createGraphics();

            // 渐变背景
            GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 0, 50), 0, HEIGHT, Color.BLACK);
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            // 添加星星
            g2d.setColor(Color.WHITE);
            for (int i = 0; i < 200; i++) {
                int x = random.nextInt(WIDTH);
                int y = random.nextInt(HEIGHT);
                int size = random.nextInt(3) + 1;
                int alpha = 100 + random.nextInt(155); // 随机透明度
                g2d.setColor(new Color(255, 255, 255, alpha));
                g2d.fillOval(x, y, size, size);
            }

            // 添加一些星云效果
            g2d.setColor(new Color(100, 100, 255, 50));
            g2d.fillOval(100, 100, 300, 300);
            g2d.setColor(new Color(255, 100, 100, 50));
            g2d.fillOval(500, 300, 200, 200);

            g2d.dispose();
            return bg;
        }

        public void startGame() {
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public void run() {
            long lastTime = System.nanoTime();
            double nsPerFrame = 1000000000.0 / 60.0; // 60 FPS
            double delta = 0;

            while (isRunning) {
                long now = System.nanoTime();
                delta += (now - lastTime) / nsPerFrame;
                lastTime = now;

                while (delta >= 1) {
                    update();
                    delta--;
                }

                repaint();

                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void update() {
            if (gameOver) return;

            // 更新玩家位置
            player.update();

            // 更新子弹位置
            for (int i = bullets.size() - 1; i >= 0; i--) {
                Bullet bullet = bullets.get(i);
                bullet.update();

                // 移除超出屏幕的子弹
                if (bullet.getY() < 0 || bullet.getY() > HEIGHT) {
                    bullets.remove(i);
                }
            }

            // 更新敌机位置
            for (int i = enemies.size() - 1; i >= 0; i--) {
                Enemy enemy = enemies.get(i);
                enemy.update();

                // 检测敌机与玩家碰撞
                if (enemy.getBounds().intersects(player.getBounds())) {
                    player.takeDamage(enemy.getDamage());
                    enemies.remove(i);
                    continue;
                }

                // 检测敌机与子弹碰撞
                for (int j = bullets.size() - 1; j >= 0; j--) {
                    Bullet bullet = bullets.get(j);
                    if (enemy.getBounds().intersects(bullet.getBounds())) {
                        enemy.takeDamage(bullet.getDamage());
                        if (enemy.isDestroyed()) {
                            score += enemy.getScoreValue();
                            enemies.remove(i);
                        }
                        bullets.remove(j);
                        break;
                    }
                }

                // 移除超出屏幕的敌机
                if (enemy.getY() > HEIGHT) {
                    enemies.remove(i);
                }
            }

            // 更新道具位置
            for (int i = powerUps.size() - 1; i >= 0; i--) {
                PowerUp powerUp = powerUps.get(i);
                powerUp.update();

                // 检测道具与玩家碰撞
                if (powerUp.getBounds().intersects(player.getBounds())) {
                    powerUp.applyEffect(player);
                    powerUps.remove(i);
                }

                // 移除超出屏幕的道具
                if (powerUp.getY() > HEIGHT) {
                    powerUps.remove(i);
                }
            }

            // 生成敌机
            enemySpawnCounter++;
            if (enemySpawnCounter >= ENEMY_SPAWN_RATE / level) {
                spawnEnemy();
                enemySpawnCounter = 0;
            }

            // 随机生成道具
            if (random.nextInt(POWER_UP_CHANCE) == 0) {
                spawnPowerUp();
            }

            // 升级逻辑
            if (score > level * 1000) {
                level++;
            }

            // 游戏结束检测
            if (player.getHealth() <= 0) {
                gameOver = true;
            }
        }

        private void spawnEnemy() {
            int maxWidth = Math.max(1, WIDTH - 50);
            int x = random.nextInt(maxWidth);
            int type = random.nextInt(3);

            switch (type) {
                case 0: // 小型敌机
                    enemies.add(new SmallEnemy(x, -50));
                    break;
                case 1: // 中型敌机
                    enemies.add(new MediumEnemy(x, -80));
                    break;
                case 2: // 大型敌机
                    enemies.add(new LargeEnemy(x, -100));
                    break;
            }
        }

        private void spawnPowerUp() {
            int maxX = Math.max(1, WIDTH - 30);
            int x = random.nextInt(maxX);
            int type = random.nextInt(2);

            switch (type) {
                case 0: // 生命恢复
                    powerUps.add(new HealthPowerUp(x, -30));
                    break;
                case 1: // 火力增强
                    powerUps.add(new FirePowerUp(x, -30));
                    break;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 绘制背景
            g.drawImage(background, 0, 0, this);

            // 绘制玩家
            player.draw(g);

            // 绘制敌机
            for (Enemy enemy : enemies) {
                enemy.draw(g);
            }

            // 绘制子弹
            for (Bullet bullet : bullets) {
                bullet.draw(g);
            }

            // 绘制道具
            for (PowerUp powerUp : powerUps) {
                powerUp.draw(g);
            }

            // 绘制UI
            drawUI(g);

            // 游戏结束画面
            if (gameOver) {
                drawGameOver(g);
            }
        }

        private void drawUI(Graphics g) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("分数: " + score, 20, 30);
            g.drawString("等级: " + level, 20, 60);

            // 绘制生命条
            g.setColor(Color.RED);
            g.fillRect(WIDTH - 220, 20, 200, 20);
            g.setColor(Color.GREEN);
            g.fillRect(WIDTH - 220, 20, (int)(200 * (player.getHealth() / 100.0)), 20);
            g.setColor(Color.WHITE);
            g.drawRect(WIDTH - 220, 20, 200, 20);
            g.drawString("生命: " + (int)player.getHealth(), WIDTH - 220, 50);

            // 绘制火力等级
            g.drawString("火力: " + player.getFireLevel(), WIDTH - 100, 80);
        }

        private void drawGameOver(Graphics g) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            String gameOverText = "游戏结束!";
            int textWidth = g.getFontMetrics().stringWidth(gameOverText);
            g.drawString(gameOverText, (WIDTH - textWidth) / 2, HEIGHT / 2 - 50);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String scoreText = "最终分数: " + score;
            textWidth = g.getFontMetrics().stringWidth(scoreText);
            g.drawString(scoreText, (WIDTH - textWidth) / 2, HEIGHT / 2);

            String restartText = "按R键重新开始";
            textWidth = g.getFontMetrics().stringWidth(restartText);
            g.drawString(restartText, (WIDTH - textWidth) / 2, HEIGHT / 2 + 50);
        }

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (gameOver) {
                if (key == KeyEvent.VK_R) {
                    restartGame();
                }
                return;
            }

            switch (key) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    player.setMovingLeft(true);
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    player.setMovingRight(true);
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    player.setMovingUp(true);
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    player.setMovingDown(true);
                    break;
                case KeyEvent.VK_SPACE:
                    player.fire(bullets);
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();

            switch (key) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    player.setMovingLeft(false);
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    player.setMovingRight(false);
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    player.setMovingUp(false);
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    player.setMovingDown(false);
                    break;
            }
        }

        private void restartGame() {
            player = new Player(WIDTH / 2 - 25, HEIGHT - 100);
            enemies.clear();
            bullets.clear();
            powerUps.clear();
            score = 0;
            level = 1;
            enemySpawnCounter = 0;
            gameOver = false;
        }
    }

    // 玩家飞机类
    class Player {
        private double x, y;
        private int width = 50;
        private int height = 50;
        private double health = 100;
        private boolean movingLeft, movingRight, movingUp, movingDown;
        private int fireLevel = 1;
        private long lastFireTime;
        private int fireCooldown = 300; // 射击冷却时间（毫秒）

        public Player(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void update() {
            if (movingLeft && x > 0) x -= PLAYER_SPEED;
            if (movingRight && x < WIDTH - width) x += PLAYER_SPEED;
            if (movingUp && y > 0) y -= PLAYER_SPEED;
            if (movingDown && y < HEIGHT - height) y += PLAYER_SPEED;
        }

        public void fire(ArrayList<Bullet> bullets) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFireTime < fireCooldown) return;

            lastFireTime = currentTime;

            switch (fireLevel) {
                case 1:
                    bullets.add(new Bullet(x + width / 2 - 2, y, 0, -BULLET_SPEED, 10));
                    break;
                case 2:
                    bullets.add(new Bullet(x + width / 4, y, 0, -BULLET_SPEED, 10));
                    bullets.add(new Bullet(x + 3 * width / 4, y, 0, -BULLET_SPEED, 10));
                    break;
                case 3:
                    bullets.add(new Bullet(x + width / 4, y, -1, -BULLET_SPEED, 10));
                    bullets.add(new Bullet(x + width / 2 - 2, y, 0, -BULLET_SPEED, 10));
                    bullets.add(new Bullet(x + 3 * width / 4, y, 1, -BULLET_SPEED, 10));
                    break;
            }
        }

        public void takeDamage(double damage) {
            health -= damage;
            if (health < 0) health = 0;
        }

        public void heal(double amount) {
            health += amount;
            if (health > 100) health = 100;
        }

        public void increaseFireLevel() {
            if (fireLevel < 3) fireLevel++;
        }

        public void draw(Graphics g) {
            // 绘制飞机主体
            g.setColor(Color.CYAN);
            int[] xPoints = {(int)x + width/2, (int)x, (int)x + width};
            int[] yPoints = {(int)y, (int)y + height, (int)y + height};
            g.fillPolygon(xPoints, yPoints, 3);

            // 绘制飞机机翼
            g.setColor(Color.BLUE);
            g.fillRect((int)x + width/4, (int)y + height/2, width/2, height/4);

            // 绘制飞机驾驶舱
            g.setColor(new Color(200, 200, 255));
            g.fillOval((int)x + width/3, (int)y + height/4, width/3, height/3);
        }

        public Rectangle getBounds() {
            return new Rectangle((int)x, (int)y, width, height);
        }

        // Getters and setters
        public double getHealth() { return health; }
        public int getFireLevel() { return fireLevel; }
        public void setMovingLeft(boolean movingLeft) { this.movingLeft = movingLeft; }
        public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }
        public void setMovingUp(boolean movingUp) { this.movingUp = movingUp; }
        public void setMovingDown(boolean movingDown) { this.movingDown = movingDown; }
    }

    // 敌机基类
    abstract class Enemy {
        protected double x, y;
        protected double speed;
        protected double health;
        protected double damage;
        protected int width, height;
        protected int scoreValue;
        protected Color color;

        public Enemy(double x, double y, double speed, double health, double damage, int width, int height, int scoreValue, Color color) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.health = health;
            this.damage = damage;
            this.width = width;
            this.height = height;
            this.scoreValue = scoreValue;
            this.color = color;
        }

        public void update() {
            y += speed;
        }

        public void takeDamage(double damage) {
            health -= damage;
        }

        public boolean isDestroyed() {
            return health <= 0;
        }

        public abstract void draw(Graphics g);

        public Rectangle getBounds() {
            return new Rectangle((int)x, (int)y, width, height);
        }

        // 添加getY()方法
        public double getY() {
            return y;
        }

        // 添加getX()方法
        public double getX() {
            return x;
        }

        // Getters
        public double getDamage() { return damage; }
        public int getScoreValue() { return scoreValue; }
    }

    // 小型敌机
    class SmallEnemy extends Enemy {
        public SmallEnemy(double x, double y) {
            super(x, y, 3, 20, 10, 30, 30, 100, Color.RED);
        }

        @Override
        public void draw(Graphics g) {
            g.setColor(color);
            g.fillOval((int)x, (int)y, width, height);

            // 添加细节
            g.setColor(Color.YELLOW);
            g.fillRect((int)x + 5, (int)y + 10, width - 10, 5);
        }
    }

    // 中型敌机
    class MediumEnemy extends Enemy {
        public MediumEnemy(double x, double y) {
            super(x, y, 2, 50, 20, 50, 40, 250, Color.ORANGE);
        }

        @Override
        public void draw(Graphics g) {
            g.setColor(color);
            g.fillRect((int)x, (int)y, width, height);

            // 添加细节
            g.setColor(Color.YELLOW);
            g.fillRect((int)x + 10, (int)y + 5, 30, 10);

            // 添加机翼
            g.setColor(Color.DARK_GRAY);
            g.fillRect((int)x - 10, (int)y + 15, 10, 5);
            g.fillRect((int)x + width, (int)y + 15, 10, 5);
        }
    }

    // 大型敌机
    class LargeEnemy extends Enemy {
        public LargeEnemy(double x, double y) {
            super(x, y, 1, 100, 30, 70, 60, 500, Color.MAGENTA);
        }

        @Override
        public void draw(Graphics g) {
            g.setColor(color);
            g.fillRect((int)x, (int)y, width, height);

            // 添加细节
            g.setColor(Color.RED);
            g.fillRect((int)x + 10, (int)y + 10, 50, 20);

            // 添加炮管
            g.setColor(Color.DARK_GRAY);
            g.fillRect((int)x + 20, (int)y - 10, 5, 10);
            g.fillRect((int)x + 45, (int)y - 10, 5, 10);
        }
    }

    // 子弹类
    class Bullet {
        private double x, y;
        private double dx, dy;
        private double speed;
        private double damage;
        private int width = 4;
        private int height = 10;

        public Bullet(double x, double y, double dx, double dy, double damage) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.speed = Math.sqrt(dx * dx + dy * dy);
            this.damage = damage;
        }

        public void update() {
            x += dx;
            y += dy;
        }

        public void draw(Graphics g) {
            // 绘制激光效果
            g.setColor(Color.YELLOW);
            g.fillRect((int)x, (int)y, width, height);

            // 添加光晕效果
            g.setColor(new Color(255, 255, 200, 100));
            g.fillOval((int)x - 2, (int)y - 2, width + 4, height + 4);
        }

        public Rectangle getBounds() {
            return new Rectangle((int)x, (int)y, width, height);
        }

        // Getters
        public double getY() { return y; }
        public double getDamage() { return damage; }
    }

    // 道具基类
    abstract class PowerUp {
        protected double x, y;
        protected double speed = 2;
        protected int width = 30;
        protected int height = 30;
        protected Color color;

        public PowerUp(double x, double y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public void update() {
            y += speed;
        }

        public abstract void applyEffect(Player player);

        public void draw(Graphics g) {
            // 绘制闪烁效果
            int alpha = 100 + (int)(155 * Math.abs(Math.sin(System.currentTimeMillis() / 200.0)));
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g.fillOval((int)x, (int)y, width, height);

            // 绘制边框
            g.setColor(Color.WHITE);
            g.drawOval((int)x, (int)y, width, height);
        }

        public Rectangle getBounds() {
            return new Rectangle((int)x, (int)y, width, height);
        }

        // 添加getY()方法
        public double getY() {
            return y;
        }
    }

    // 生命恢复道具
    class HealthPowerUp extends PowerUp {
        public HealthPowerUp(double x, double y) {
            super(x, y, Color.GREEN);
        }

        @Override
        public void applyEffect(Player player) {
            player.heal(30);
        }

        @Override
        public void draw(Graphics g) {
            super.draw(g);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("H", (int)x + 10, (int)y + 20);
        }
    }

    // 火力增强道具
    class FirePowerUp extends PowerUp {
        public FirePowerUp(double x, double y) {
            super(x, y, Color.YELLOW);
        }

        @Override
        public void applyEffect(Player player) {
            player.increaseFireLevel();
        }

        @Override
        public void draw(Graphics g) {
            super.draw(g);
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("F", (int)x + 10, (int)y + 20);
        }
    }
}