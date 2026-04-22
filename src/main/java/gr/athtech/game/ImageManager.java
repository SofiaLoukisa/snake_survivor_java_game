package gr.athtech.game;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class ImageManager {

    // Eikones gia to fidi kai to perivallon
    public BufferedImage head, body, bodyShadow, wall, rock, mushroom;
    // Eikones gia ta UI elements kai to nero
    public BufferedImage menuBg, star;
    public BufferedImage waterSide, waterCorner;
    // Pinakas gia oles tis eikones twn froutwn
    public BufferedImage[] fruits = new BufferedImage[6];

    public ImageManager() {
        try {
            // Fortwsh twn vasikwn eikonwn tou paixnidiou
            head = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/head.png")));
            body = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/body.png")));
            bodyShadow = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/body_shadow.png")));

            wall = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/wall.png")));
            rock = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/rock.png")));
            mushroom = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/mushroom.png")));

            menuBg = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/jungle_bg.png")));
            star = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/star.png")));

            waterSide = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/water_side.png")));
            waterCorner = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/water_corner.png")));

            // Fortwsh twn froutwn (apothikeush stis theseis 1 ews 5 tou pinaka)
            for (int i = 1; i <= 5; i++) {
                fruits[i] = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/images/fruit" + i + ".png")));
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("Sfalma kata tin fortwsh twn eikonwn. Elenkste ta onomata kai to path twn arxeiwn.");
            e.printStackTrace();
        }
    }
}