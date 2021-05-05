package its.geppy.tictactoe.Utilities;

import its.geppy.tictactoe.TicTacToe;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static its.geppy.tictactoe.TicTacToe.getMain;

public class GameData {

    protected List<MagmaCube> magmaCubes;
    protected Map<Vector, ParticleJob> particleMap;

    private final ArmorStand stand;
    private Turn turn;

    private final boolean AI;
    private final Vector direction;
    private final Vector origin;

    private final Player challenger;
    private final LivingEntity opponent;
    private final boolean opponentOldAIState;

    private int ticksSinceReset = 0;

    private int taskID;

    private final String[][] board = {
            {"", "", ""},
            {"", "", ""},
            {"", "", ""}
    };

    public enum ParticleJob {
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

    public GameData(ArmorStand stand, Map<Vector, ParticleJob> particleMap, Player challenger, LivingEntity opponent) {
        this.stand = stand;
        this.particleMap = particleMap;
        this.origin = stand.getLocation().clone().add(0, 1, 0).toVector();
        this.challenger = challenger;
        this.opponent = opponent;
        this.AI = !(opponent instanceof Player);

        opponentOldAIState = opponent.hasAI();
        if (AI) opponent.setAI(false);

        Location tempDirection = stand.getLocation().clone();
        tempDirection.setPitch(0);
        this.direction = tempDirection.getDirection().normalize();

        magmaCubes = new ArrayList<>();

        turn = Turn.X;
    }

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

    public Player getChallenger() {
        return challenger;
    }

    public LivingEntity getOpponent() {
        return opponent;
    }

    public List<MagmaCube> getMagmaCubes() {
        return magmaCubes;
    }

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

    public void removeClickable(MagmaCube cube) {
        ticksSinceReset = 0;

        PersistentDataContainer dataContainer = cube.getPersistentDataContainer();
        Byte posX = dataContainer.get(new NamespacedKey(getMain(), "board_x"), PersistentDataType.BYTE);
        Byte posY = dataContainer.get(new NamespacedKey(getMain(), "board_y"), PersistentDataType.BYTE);

        if (posX != null && posY != null)
            board[posY + (byte) 1][posX + (byte) 1] = getTurn() == Turn.X ? "o" : "x";

        magmaCubes.remove(cube);
        cube.remove();

        Winner winner = winCheck(board);
        if (winner != Winner.NOONE) {
            queueGameEnd(taskID, winner);
            return;
        }

        if (AI && getTurn() == Turn.O)
            makeMove();
    }

    private void makeMove() {

        if (magmaCubes.isEmpty())
            return;

        Map<MagmaCube, String[][]> neutralMoves = new HashMap<>();

        for (MagmaCube cube : magmaCubes) {

            String[][] newBoard = Arrays.stream(board).map(String[]::clone).toArray(String[][]::new);

            PersistentDataContainer dataContainer = cube.getPersistentDataContainer();
            Byte posX = dataContainer.get(new NamespacedKey(getMain(), "board_x"), PersistentDataType.BYTE);
            Byte posY = dataContainer.get(new NamespacedKey(getMain(), "board_y"), PersistentDataType.BYTE);

            if (posX != null && posY != null)
                newBoard[posY + (byte) 1][posX + (byte) 1] = "o";

            Winner winner = winCheck(newBoard);
            if (winner == Winner.OPPONENT) {
                opponent.teleport(opponent.getLocation().setDirection(cube.getLocation().subtract(opponent.getEyeLocation()).toVector()));
                BoardManager.clicked(cube, this);
                return;
            }

            neutralMoves.put(cube, newBoard);

        }

        for (MagmaCube cube : neutralMoves.keySet()) {
            String[][] newBoard = Arrays.stream(neutralMoves.get(cube)).map(String[]::clone).toArray(String[][]::new);

            PersistentDataContainer dataContainer = cube.getPersistentDataContainer();
            Byte posX = dataContainer.get(new NamespacedKey(getMain(), "board_x"), PersistentDataType.BYTE);
            Byte posY = dataContainer.get(new NamespacedKey(getMain(), "board_y"), PersistentDataType.BYTE);

            if (posX != null && posY != null)
                newBoard[posY + (byte) 1][posX + (byte) 1] = "x";

            Winner winner = winCheck(newBoard);
            if (winner == Winner.CHALLENGER) {
                opponent.teleport(opponent.getLocation().setDirection(cube.getLocation().subtract(opponent.getEyeLocation()).toVector()));
                BoardManager.clicked(cube, this);
                return;
            }

        }

        int random = new Random().nextInt(neutralMoves.size());
        MagmaCube cube = new ArrayList<>(neutralMoves.keySet()).get(random);
        opponent.teleport(opponent.getLocation().setDirection(cube.getLocation().subtract(opponent.getEyeLocation()).toVector()));
        BoardManager.clicked(cube, this);

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

        try {
            TicTacToe.noCollisionTeam.addEntry(challenger.getUniqueId().toString());
            TicTacToe.noCollisionTeam.addEntry(opponent.getUniqueId().toString());
        } catch (Exception ignored) { }

        taskID = new BukkitRunnable() {
            @Override
            public void run() {
                for (Vector v : particleMap.keySet()) {
                    switch (particleMap.get(v)) {
                        case X:
                            stand.getWorld().spawnParticle(Particle.COMPOSTER, v.toLocation(stand.getWorld()), 5);
                            break;
                        case O:
                            stand.getWorld().spawnParticle(Particle.FLAME, v.toLocation(stand.getWorld()), 0);
                            break;
                        default:
                            stand.getWorld().spawnParticle(Particle.END_ROD, v.toLocation(stand.getWorld()), 0);
                    }

                }

                ticksSinceReset += 6;

                if (magmaCubes.isEmpty() && !isCancelled()) {
                    queueGameEnd(taskID, Winner.NOONE);
                    cancel();
                }

                if (origin.distance(challenger.getLocation().toVector()) > TicTacToe.maxDistanceFromBoard ||
                        origin.distance(opponent.getLocation().toVector()) > TicTacToe.maxDistanceFromBoard ||
                        stand.isDead() || ticksSinceReset > TicTacToe.maxIdleTime) {
                    endGame();
                    cancel();
                }
            }
        }.runTaskTimer(getMain(), 0, 6).getTaskId();
    }

    private void queueGameEnd(int taskID, Winner winner) {
        for (int i = 0; i < magmaCubes.size(); i++) {
            try {
                TicTacToe.noCollisionTeam.removeEntry(magmaCubes.get(i).getUniqueId().toString());
                TicTacToe.noCollisionTeam.removeEntry(challenger.getName());
                TicTacToe.noCollisionTeam.removeEntry(opponent.getName());
            } catch (Exception ignored) { }
            magmaCubes.get(i).remove();
        }

        if (winner.equals(Winner.CHALLENGER)) {
            challenger.sendMessage(ChatColor.GREEN + "You won!");
            SoundManager.playSound(challenger, Sound.ENTITY_PLAYER_LEVELUP);

            opponent.sendMessage(ChatColor.RED + "You lost!");
            if (opponent instanceof Player)
                SoundManager.playSound((Player) opponent, Sound.ENTITY_PLAYER_HURT);

        } else if (winner.equals(Winner.OPPONENT)) {
            challenger.sendMessage(ChatColor.RED + "You lost!");
            SoundManager.playSound(challenger, Sound.ENTITY_PLAYER_HURT);

            opponent.sendMessage(ChatColor.GREEN + "You won!");
            if (opponent instanceof Player)
                SoundManager.playSound((Player) opponent, Sound.ENTITY_PLAYER_LEVELUP);

        } else {
            challenger.sendMessage(ChatColor.YELLOW + "You tied!");
            opponent.sendMessage(ChatColor.YELLOW + "You tied!");

            SoundManager.playSound(challenger, Sound.ENTITY_PLAYER_BREATH);
            if (opponent instanceof Player)
                SoundManager.playSound((Player) opponent, Sound.ENTITY_PLAYER_BREATH);
        }

        Bukkit.getScheduler().runTaskLater(getMain(), () -> {
            endGame();
            Bukkit.getScheduler().cancelTask(taskID);
        }, 60);
    }

    public void endGame() {
        TicTacToe.activeGames.remove(this);
        if (AI) opponent.setAI(opponentOldAIState);

        for (int i = 0; i < magmaCubes.size(); i++) {
            try {
                TicTacToe.noCollisionTeam.removeEntry(magmaCubes.get(i).getUniqueId().toString());
                TicTacToe.noCollisionTeam.removeEntry(challenger.getUniqueId().toString());
                TicTacToe.noCollisionTeam.removeEntry(opponent.getUniqueId().toString());
            } catch (Exception ignored) { }
            magmaCubes.get(i).remove();
        }

        stand.remove();
    }

}
