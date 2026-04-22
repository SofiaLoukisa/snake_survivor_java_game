package gr.athtech.game;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        // Dimiourgia tou kentrikou parathyrou tou paixnidiou
        JFrame window = new JFrame("Snake Survivor");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        // Prosthetoume to GamePanel mesa sto parathyro
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        // To pack prosarmozei to megethos tou parathyrou wste na xwresei to GamePanel
        window.pack();

        // Kentrarei to parathyro stin othoni kai to emfanizei
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}