package its.geppy.tictactoe.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.*;

import static its.geppy.tictactoe.TicTacToe.getMain;

public class RewardManager {

    private static List<RewardLimit> gamesPlayedVs = new ArrayList<>();

    public static void giveReward(GameData game, GameData.Winner winner) {

        if (limitReached(game)) {
            game.getChallenger().sendMessage(ChatColor.YELLOW + "Reward limit reached");
            game.getOpponent().sendMessage(ChatColor.YELLOW + "Reward limit reached");
            return;
        }

        Map<String, int[]> weightedRewards = new HashMap<>();

        String opponentType = game.getOpponent().getType().toString();
        ConfigurationSection rewardsList = getMain().getConfig().getConfigurationSection("rewards." + opponentType);

        if (rewardsList == null)
            return;

        int weight = 0;

        for (String s : rewardsList.getKeys(false)){

            int pathWeight = getMain().getConfig().getInt("rewards." + opponentType + "." + s + ".weight");
            if (pathWeight == 0)
                continue;

            weightedRewards.put(s, new int[] {weight, weight + pathWeight});
            weight += pathWeight;

        }

        if (weightedRewards.isEmpty())
            return;

        int randomWeight = new Random().nextInt(weight);
        String randomReward = weightedRewards.keySet().stream()
                .filter(s -> weightedRewards.get(s)[0] <= randomWeight && weightedRewards.get(s)[1] > randomWeight)
                .findFirst()
                .orElse(null);

        if (randomReward == null)
            return;

        String winnerReward = getMain().getConfig().getString("rewards." + opponentType + "." + randomReward + ".winner");
        String loserReward = getMain().getConfig().getString("rewards." + opponentType + "." + randomReward + ".loser");

        LivingEntity winnerEntity = (winner == GameData.Winner.CHALLENGER) ? game.getChallenger() : game.getOpponent();
        LivingEntity loserEntity = (winner != GameData.Winner.CHALLENGER) ? game.getChallenger() : game.getOpponent();

        if (winnerReward != null && winnerEntity instanceof Player) {
            winnerReward = winnerReward.replaceAll("\\$winner", winnerEntity.getName());
            winnerReward = winnerReward.replaceAll("\\$loser", loserEntity.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), winnerReward);
        }

        if (loserReward != null && loserEntity instanceof Player) {
            loserReward = loserReward.replaceAll("\\$loser", loserEntity.getName());
            loserReward = loserReward.replaceAll("\\$winner", winnerEntity.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), loserReward);
        }

        addToGamesPlayed(game);

    }

    private static boolean limitReached(GameData game) {

        if (!(game.getOpponent() instanceof Player))
            return false;

        RewardLimit rewardLimit = getRewardLimit(game).orElse(null);

        if (rewardLimit == null)
            return false;

        int maxGamesPlayed = getMain().getConfig().getInt("reward-limit.amount");

        if (maxGamesPlayed == 0)
            return false;

        if (rewardLimit.timesPlayed < maxGamesPlayed) {
            return false;
        } else {
            if (rewardLimit.getResetTime().isBefore(LocalDateTime.now())) {
                gamesPlayedVs.remove(rewardLimit);
                return false;
            }
        }

        return true;
    }

    private static void addToGamesPlayed(GameData game) {

        if (!(game.getOpponent() instanceof Player))
            return;

        int maxGamesPlayed = getMain().getConfig().getInt("reward-limit.amount");

        if (maxGamesPlayed == 0)
            return;

        RewardLimit rewardLimit = getRewardLimit(game).orElse(null);

        if (rewardLimit == null) {
            rewardLimit = new RewardLimit(game.getChallenger().getUniqueId(), game.getOpponent().getUniqueId());
            gamesPlayedVs.add(rewardLimit);
        }

        rewardLimit.timesPlayed += 1;

    }

    private static Optional<RewardLimit> getRewardLimit(GameData game) {
        return gamesPlayedVs.stream()
                .filter(g -> (g.player1.equals(game.getChallenger().getUniqueId()) || g.player2.equals(game.getChallenger().getUniqueId())) &&
                             (g.player1.equals(game.getOpponent().getUniqueId()) || g.player2.equals(game.getOpponent().getUniqueId())))
                .findFirst();

    }

}

class RewardLimit {

    public RewardLimit(UUID player1, UUID player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.timesPlayed = 0;
        this.timeAdded = LocalDateTime.now();
    }

    public final UUID player1;
    public final UUID player2;
    public int timesPlayed;

    private final LocalDateTime timeAdded;

    public LocalDateTime getResetTime() {
        int minutes = getMain().getConfig().getInt("reward-limit.time");
        return timeAdded.plusMinutes(minutes);
    }

}