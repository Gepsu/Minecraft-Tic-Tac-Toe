package its.geppy.tictactoe.Utilities;

import its.geppy.tictactoe.Commands.SoundCommands;
import its.geppy.tictactoe.TicTacToe;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static its.geppy.tictactoe.TicTacToe.getMain;
import static its.geppy.tictactoe.TicTacToe.getStringInConfig;

public class GameData {

    protected Map<Vector, Byte[]> clickables;
    protected Map<Vector, ParticleJob> particleMap;

    private final ArmorStand stand;
    private Turn turn;

    private final boolean AI;
    private final Vector direction;
    private final Vector origin;

    private final Player challenger;
    private final LivingEntity opponent;
    private final boolean opponentOldAIState;

    private final Particle challengerParticles;
    private final Particle opponentParticles;

    private final long bet;

    private int ticksSinceReset = 0;

    private boolean pregame = true;
    private boolean gameOver = false;

    private int taskID;

    private final String[][] board = {
            {"", "", ""},
            {"", "", ""},
            {"", "", ""}
    };

    public enum ParticleJob {
        DEBUG,
        FRAME,
        X,
        O
    }

    public enum Turn {
        X,
        O
    }

    public enum Winner {
        CHALLENGER,
        OPPONENT,
        NOONE
    }

    public GameData(ArmorStand stand, Map<Vector, ParticleJob> particleMap, Player challenger, LivingEntity opponent, long bet) {
        this.stand = stand;
        this.particleMap = particleMap;
        this.origin = stand.getLocation().clone().add(0, 1, 0).toVector();
        this.challenger = challenger;
        this.opponent = opponent;
        this.bet = bet;
        this.AI = !(opponent instanceof Player);

        opponentOldAIState = opponent.hasAI();
        if (AI) opponent.setAI(false);

        Location tempDirection = stand.getLocation().clone();
        tempDirection.setPitch(0);
        this.direction = tempDirection.getDirection().normalize();

        String challengerOverrideParticles = challenger.getPersistentDataContainer().get(new NamespacedKey(getMain(), "ttt_particles"), PersistentDataType.STRING);
        String opponentOverrideParticles = (!AI) ? opponent.getPersistentDataContainer().get(new NamespacedKey(getMain(), "ttt_particles"), PersistentDataType.STRING) : null;

        if (getMain().getConfig().getBoolean("allow-player-particles")) {
            challengerParticles = challengerOverrideParticles == null ? Particle.valueOf(getStringInConfig("player1-particles")) : Particle.valueOf(challengerOverrideParticles);
            opponentParticles = opponentOverrideParticles == null ? Particle.valueOf(getStringInConfig("player2-particles")) : Particle.valueOf(opponentOverrideParticles);
        } else {
            challengerParticles = Particle.valueOf(getStringInConfig("player1-particles"));
            opponentParticles = Particle.valueOf(getStringInConfig("player2-particles"));
        }

        clickables = new HashMap<>();

        turn = Turn.X;

        Bukkit.getScheduler().runTaskLater(getMain(), () -> pregame = false, 10);
    }

    public boolean isPreGame() { return pregame; }

    public boolean getAI() {
        return AI;
    }

    public Turn nextTurn() {
        turn = (turn == Turn.X) ? Turn.O : Turn.X;
        return turn;
    }

    public Turn getTurn() {
        return turn;
    }

    public long getBetAmount() { return bet; }

    public int getTaskID() { return taskID; }

    public Player getChallenger() {
        return challenger;
    }

    public LivingEntity getOpponent() {
        return opponent;
    }

    public Map<Vector, Byte[]> getClickables() { return clickables; }

    public Vector calculatePosition(Vector origin, double x, double y) {
        return new Vector(
                origin.getX() + x * direction.getZ(),
                origin.getY() + y + BoardManager.boardSize / 2,
                origin.getZ() + x * -direction.getX()
        );
    }

    public Vector calculatePosition(double x, double y) {
        return new Vector(
                origin.getX() + x * direction.getZ(),
                origin.getY() + y + BoardManager.boardSize / 2,
                origin.getZ() + x * -direction.getX()
        );
    }

    public void removeClickable(Vector clickable) {
        ticksSinceReset = 0;

        Byte posX = clickables.get(clickable)[0];
        Byte posY = clickables.get(clickable)[1];

        if (posX != null && posY != null)
            board[posY + (byte) 1][posX + (byte) 1] = getTurn() == Turn.X ? "o" : "x";

        clickables.remove(clickable);

        Winner winner = winCheck(board);
        if (winner != Winner.NOONE) {
            queueGameEnd(taskID, winner);
            return;
        }

        if (AI && getTurn() == Turn.O)
            makeMove();
    }

    private void makeMove() {

        if (clickables.isEmpty())
            return;

        Map<Vector, String[][]> neutralMoves = new HashMap<>();

        for (Vector clickable : clickables.keySet()) {

            String[][] newBoard = Arrays.stream(board).map(String[]::clone).toArray(String[][]::new);

            Byte posX = clickables.get(clickable)[0];
            Byte posY = clickables.get(clickable)[1];

            if (posX != null && posY != null)
                newBoard[posY + (byte) 1][posX + (byte) 1] = "o";

            Winner winner = winCheck(newBoard);
            if (winner == Winner.OPPONENT) {
                opponent.teleport(opponent.getLocation().setDirection(clickable.toLocation(opponent.getWorld()).subtract(opponent.getEyeLocation()).toVector()));
                BoardManager.clicked(clickable, this);
                return;
            }

            neutralMoves.put(clickable, newBoard);

        }

        for (Vector clickable : neutralMoves.keySet()) {
            String[][] newBoard = Arrays.stream(neutralMoves.get(clickable)).map(String[]::clone).toArray(String[][]::new);

            Byte posX = clickables.get(clickable)[0];
            Byte posY = clickables.get(clickable)[1];

            if (posX != null && posY != null)
                newBoard[posY + (byte) 1][posX + (byte) 1] = "x";

            Winner winner = winCheck(newBoard);
            if (winner == Winner.CHALLENGER) {
                opponent.teleport(opponent.getLocation().setDirection(clickable.toLocation(opponent.getWorld()).subtract(opponent.getEyeLocation()).toVector()));
                BoardManager.clicked(clickable, this);
                return;
            }

        }

        int random = new Random().nextInt(neutralMoves.size());
        Vector clickable = new ArrayList<>(neutralMoves.keySet()).get(random);
        opponent.teleport(opponent.getLocation().setDirection(clickable.toLocation(opponent.getWorld()).subtract(opponent.getEyeLocation()).toVector()));
        BoardManager.clicked(clickable, this);

    }

    private Winner winCheck(String[][] board) {
        List<String> winConditions = Arrays.asList(
                "xxx------",
                "---xxx---",
                "------xxx",
                "x--x--x--",
                "-x--x--x-",
                "--x--x--x",
                "x---x---x",
                "--x-x-x--"
        );
        
        String challenger = "";
        String opponent = "";

        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 2; y++) {
                challenger += (board[x][y].equals("x")) ? "x" : "-";
                opponent += (board[x][y].equals("o")) ? "x" : "-";
            }
        }

        Winner winner = Winner.NOONE;

        String finalChallenger = challenger;
        String finalOpponent = opponent;

        for (String s : winConditions) {
            char[] chars = s.toCharArray();

            int correctChallenger = 0;
            int correctOpponent = 0;

            for (int i = 0; i < 9; i++) {
                if (chars[i] == finalChallenger.toCharArray()[i] && chars[i] != '-') correctChallenger++;
                if (chars[i] == finalOpponent.toCharArray()[i] && chars[i] != '-') correctOpponent++;
            }

            if (correctChallenger >= 3) winner = Winner.CHALLENGER;
            if (correctOpponent >= 3) winner = Winner.OPPONENT;

            if (winner != Winner.NOONE) break;

        }

        return winner;
    }

    public void startGame() {
        TicTacToe.activeGames.add(this);

        taskID = new BukkitRunnable() {
            @Override
            public void run() {
                for (Vector v : particleMap.keySet()) {
                    switch (particleMap.get(v)) {
                        case O:
                            stand.getWorld().spawnParticle(challengerParticles, v.toLocation(stand.getWorld()), 0);
                            break;
                        case X:
                            stand.getWorld().spawnParticle(opponentParticles, v.toLocation(stand.getWorld()), 0);
                            break;
                        default:
                            stand.getWorld().spawnParticle(Particle.valueOf(getStringInConfig("frame-particles")), v.toLocation(stand.getWorld()), 0);
                    }

                }

                ticksSinceReset += 6;

                if (clickables.isEmpty() && !isCancelled() && !gameOver) {
                    queueGameEnd(taskID, Winner.NOONE);
                    cancel();
                }

                if (origin.distance(challenger.getLocation().toVector()) > TicTacToe.maxDistanceFromBoard) {
                    queueGameEnd(taskID, Winner.OPPONENT);
                    cancel();
                }

                if (origin.distance(opponent.getLocation().toVector()) > TicTacToe.maxDistanceFromBoard) {
                    queueGameEnd(taskID, Winner.CHALLENGER);
                    cancel();
                }

                if (ticksSinceReset > TicTacToe.maxIdleTime) {
                    queueGameEnd(taskID, turn == Turn.O ? Winner.CHALLENGER : Winner.OPPONENT);
                    cancel();
                }

                if (stand.isDead()) {
                    RewardManager.refundBets(GameData.this);
                    endGame();
                    cancel();
                }
            }
        }.runTaskTimer(getMain(), 0, 6).getTaskId();
    }

    public void queueGameEnd(int taskID, Winner winner) {
        gameOver = true;
        clickables.clear();

        if (winner.equals(Winner.CHALLENGER)) {
            challenger.sendMessage(TicTacToe.getStringInConfig("win-message"));
            SoundCommands.playSound(challenger, Sound.ENTITY_PLAYER_LEVELUP);

            opponent.sendMessage(TicTacToe.getStringInConfig("lose-message"));
            if (opponent instanceof Player)
                SoundCommands.playSound((Player) opponent, Sound.ENTITY_PLAYER_HURT);

            RewardManager.giveReward(this, winner);

        } else if (winner.equals(Winner.OPPONENT)) {
            challenger.sendMessage(TicTacToe.getStringInConfig("lose-message"));
            SoundCommands.playSound(challenger, Sound.ENTITY_PLAYER_HURT);

            opponent.sendMessage(TicTacToe.getStringInConfig("win-message"));
            if (opponent instanceof Player)
                SoundCommands.playSound((Player) opponent, Sound.ENTITY_PLAYER_LEVELUP);

            RewardManager.giveReward(this, winner);

        } else {
            challenger.sendMessage(TicTacToe.getStringInConfig("tie-message"));
            opponent.sendMessage(TicTacToe.getStringInConfig("tie-message"));

            RewardManager.refundBets(this);

            SoundCommands.playSound(challenger, Sound.ENTITY_PLAYER_BREATH);
            if (opponent instanceof Player)
                SoundCommands.playSound((Player) opponent, Sound.ENTITY_PLAYER_BREATH);
        }

        Bukkit.getScheduler().runTaskLater(getMain(), () -> {
            endGame();
            Bukkit.getScheduler().cancelTask(taskID);
        }, 60);
    }

    public void endGame() {
        TicTacToe.activeGames.remove(this);
        if (AI) opponent.setAI(opponentOldAIState);

        stand.remove();
    }

}
