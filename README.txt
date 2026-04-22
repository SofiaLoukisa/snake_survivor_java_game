# 🐍 Snake Survivor

> A fast-paced twist on the classic Snake game — eat fruits **in the right order**, dodge obstacles and master the **jump mechanic** to survive.

Built with **Java 17 + Swing** as part of a Game Design & Development course.

**Team 1** — Sofia Loukissa · Anastasia Kouridaki · Evangelos Dimovits

---

## 🎮 Gameplay

Instead of eating any fruit you find, Snake Survivor forces you to eat them **in numbered order (1 → 2 → 3)**. Eating the wrong fruit resets the entire fruit set and adds a mistake to your score. Complete **6 sets of 3 fruits** to finish a level.

The game has two levels of increasing difficulty:

| | Level 1 — Learning Phase | Level 2 — Skill Phase |
|---|---|---|
| **Goal** | Complete 6 fruit trios | Complete 6 fruit trios |
| **Obstacles** | Walls, self-collision | + Rocks (fixed) + Mushrooms (spawning) |
| **Speed** | Constant | Increases every trio |
| **Unlock condition** | — | Earn ≥ 2 stars in Level 1 |

---

## ⭐ Scoring

Your performance is rated 1–3 stars based on **time** and **mistakes**:

| Stars | Level 1 | Level 2 |
|---|---|---|
| ⭐⭐⭐ | Under 60s, ≤ 1 mistake | Under 50s, 0 mistakes |
| ⭐⭐ | Under 90s, ≤ 3 mistakes | Under 80s, ≤ 3 mistakes |
| ⭐ | Any other result | Any other result |

Best times are saved to `highscores.txt` in the game's working directory and persist between sessions.

---

## 🕹️ Controls

| Key | Action |
|---|---|
| `Arrow Keys` | Move the snake |
| `Space` | **Jump** — skip over the tile directly in front of you |
| `P` / `Esc` | Pause / Resume |
| `M` | Mute / Unmute audio |
| `Enter` | Confirm / Advance screens |
| `R` | Restart the game |

> **Tip:** The jump skips one tile ahead and lands two tiles forward. Use it to escape tight spots or dodge mushrooms — but you can't jump over walls.

---

## 🚀 How to Run

### Option 1 — Run the JAR (easiest)

Make sure **Java 17 or later** is installed, then double-click the JAR or run:

```bash
java -jar SnakeSurvivor_Dimovits_Loukissa_Kouridaki.jar
```

### Option 2 — Build from source with Maven

```bash
# Clone / extract the project
cd "Snake Game Dimovits Loukissa Kouridaki"

# Build a runnable fat JAR
mvn package

# Run it
java -jar target/SnakeSurvivor-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Requirements

- **Java 17+** (the project uses records and switch expressions)
- **Maven 3.6+** (only needed if building from source)
- No external libraries — pure Java Swing

---

## 📁 Project Structure

```
├── src/main/java/gr/athtech/game/
│   ├── Main.java           # Entry point — creates the JFrame
│   ├── GamePanel.java      # All game logic, rendering and input
│   ├── ImageManager.java   # Loads sprite assets at startup
│   └── SoundManager.java   # BGM and SFX via javax.sound.sampled
│
├── src/main/resources/
│   ├── images/             # Sprites (snake, fruits, obstacles, backgrounds)
│   └── sounds/             # WAV audio files (music + SFX)
│
├── pom.xml                 # Maven build configuration
└── SnakeSurvivor_...jar    # Pre-built runnable JAR
```

---

## 🧩 Game Mechanics Summary

- **Ordered collection** — 3 fruits numbered 1–2–3 are always on screen. You must eat them in order.
- **Wrong fruit** — eating out of order increments your mistake counter and respawns all 3 fruits.
- **Jump** — press Space to leap over the tile immediately in front of you, landing 2 tiles ahead. The skipped tile is shown as a ghost segment.
- **Mushrooms (Level 2)** — a new poisonous mushroom spawns every 2 completed trios. Instant death on contact.
- **Speed escalation (Level 2)** — the snake moves 5 ms faster after each completed trio, down to a minimum of 50 ms/tick.
- **Pause compensation** — paused time is not counted toward your final score.

---

## 👥 Team

| Evangelos Dimovits | Developer |
| Sofia Loukissa | Developer |
| Anastasia Kouridaki | Developer |

---

## 📄 License

This project was developed for academic purposes at **Athens Tech College (ATC)** as part of the Game Design and Development course.
