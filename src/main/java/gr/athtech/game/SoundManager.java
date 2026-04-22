package gr.athtech.game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;

public class SoundManager {

    Clip bgmClip; // Kanali gia ti mousiki vathous
    Clip sfxClip; // Kanali gia ta ixhtika efe
    URL[] soundURL = new URL[10];
    boolean isMuted = false;

    public SoundManager() {
        // Arxikopoihsh twn paths gia ola ta arxeia ixou
        soundURL[0] = getClass().getResource("/sounds/eat_correct.wav");
        soundURL[1] = getClass().getResource("/sounds/eat_wrong.wav");
        soundURL[2] = getClass().getResource("/sounds/jump.wav");
        soundURL[3] = getClass().getResource("/sounds/crash.wav");
        soundURL[4] = getClass().getResource("/sounds/mushroom.wav");
        soundURL[5] = getClass().getResource("/sounds/level_win.wav");
        soundURL[6] = getClass().getResource("/sounds/menu_music.wav");
        soundURL[7] = getClass().getResource("/sounds/battle_music.wav");
    }

    // Paizei ena ixhtiko efe mia fora (den diakoptei tin mousiki)
    public void playSFX(int i) {
        if (soundURL[i] == null || isMuted) return;
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            sfxClip = AudioSystem.getClip();
            sfxClip.open(ais);
            sfxClip.start();
        } catch (Exception e) {
            System.out.println("Adunamia anaparagwghs tou SFX.");
        }
    }

    // Ksekinaei tin mousiki kai tin vazei se sunexi epanalipsi (loop)
    public void playBGM(int i) {
        if (soundURL[i] == null) return;
        try {
            if (bgmClip != null) bgmClip.stop(); // Stamataei to proigoumeno kommati an yparxei
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(ais);
            if (!isMuted) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.out.println("Adunamia anaparagwghs tou BGM.");
        }
    }

    // Stamataei ti mousiki
    public void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
        }
    }

    // Rithmizei thn entasi ths mousikhs (decibels) wste na min einai poly dynati
    public void setVolume(float decibels) {
        if (bgmClip != null && bgmClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(decibels);
        }
    }

    // Enaalagh metaksi Mute kai Unmute
    public void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            if (bgmClip != null) bgmClip.stop();
            if (sfxClip != null) sfxClip.stop();
        } else {
            if (bgmClip != null) bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
}