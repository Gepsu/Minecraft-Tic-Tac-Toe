package its.geppy.tictactoe.Commands;

import its.geppy.tictactoe.TicTacToe;
import its.geppy.tictactoe.Utilities.BoardManager;
import net.ess3.api.MaxMoneyException;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;

import static its.geppy.tictactoe.TicTacToe.*;

public class ChallengeCommand {

    private static List<Challenge> activeChallenges = new ArrayList<>();

    public static List<Challenge> getActiveChallenges() { return activeChallenges; }

    public static void challenge(Player player, String[] args) {

        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /ttt challenge player <player> <bet>");
            return;
        }

        Player targetPlayer = getMain().getServer().getPlayer(args[2]);
        long betAmount;

        if (targetPlayer == null) {
            player.sendMessage(TicTacToe.getStringInConfig("invalid-player-name"));
            return;
        }

        if (TicTacToe.isPlaying(player) != null) return;
        if (TicTacToe.isPlaying(targetPlayer) != null) return;

        if (!targetPlayer.hasPermission("ttt.play")) {
            player.sendMessage(TicTacToe.getStringInConfig("other-player-doesnt-have-permission"));
            return;
        }

        if (targetPlayer.equals(player)) {
            player.sendMessage(TicTacToe.getStringInConfig("cant-challenge-yourself"));
            return;
        }

        try {
            betAmount = Math.abs(Long.parseLong(args[3]));
        } catch (Exception ignored) {
            player.sendMessage(TicTacToe.getStringInConfig("invalid-bet-amount"));
            return;
        }

        if (player.getLocation().distance(targetPlayer.getLocation()) > getMain().getConfig().getInt("maximum-distance-from-board")) {
            player.sendMessage(TicTacToe.getStringInConfig("player-too-far"));
            return;
        }

        if (getEssentials().getUser(player).getMoney().longValue() < betAmount) {
            player.sendMessage(TicTacToe.getStringInConfig("you-dont-have-enough-money"));
            return;
        }

        if (getEssentials().getUser(targetPlayer).getMoney().longValue() < betAmount) {
            player.sendMessage(TicTacToe.getStringInConfig("other-player-doesnt-have-enough-money"));
            return;
        }

        activeChallenges.add(new Challenge(player, targetPlayer, betAmount));

        String playerMsg = TicTacToe.getStringInConfig("you-challenged-player");
        playerMsg = playerMsg.replaceAll("\\$player", targetPlayer.getName());
        playerMsg = playerMsg.replaceAll("\\$bet", betAmount + "");
        player.sendMessage(playerMsg);

        String targetMsg = TicTacToe.getStringInConfig("player-challenged-you");
        targetMsg = targetMsg.replaceAll("\\$player", player.getName());
        targetMsg = targetMsg.replaceAll("\\$bet", betAmount + "");
        targetPlayer.sendMessage(targetMsg);

        TextComponent cancelButton = new TextComponent(TicTacToe.getStringInConfig("cancel-button"));
        cancelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ttt challenge cancel " + targetPlayer.getName()));
        player.spigot().sendMessage(cancelButton);

        TextComponent denyButton = new TextComponent(TicTacToe.getStringInConfig("deny-button"));
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ttt challenge cancel " + player.getName()));
        TextComponent acceptButton = new TextComponent(TicTacToe.getStringInConfig("accept-button"));
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ttt challenge accept " + player.getName()));
        targetPlayer.spigot().sendMessage(denyButton);
        targetPlayer.spigot().sendMessage(acceptButton);

    }

    public static void acceptChallenge(Player player, String[] args) {

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /ttt challenge accept <player>");
            return;
        }

        if (TicTacToe.isPlaying(player) != null) return;

        Challenge challenge = getChallenge(player, args);
        if (challenge == null)
            return;

        if (player.equals(challenge.challenger))
            return;

        Player challenger = challenge.challenger;
        Player targetPlayer = challenge.opponent;
        long betAmount = challenge.betAmount;

        if (challenger.getLocation().distance(targetPlayer.getLocation()) > getMain().getConfig().getInt("maximum-distance-from-board")) {
            player.sendMessage(TicTacToe.getStringInConfig("player-too-far"));
            cancelChallenge(challenge);
            return;
        }

        if (getEssentials().getUser(player).getMoney().longValue() < betAmount) {
            player.sendMessage(TicTacToe.getStringInConfig("you-dont-have-enough-money"));
            cancelChallenge(challenge);
            return;
        }

        if (getEssentials().getUser(challenger).getMoney().longValue() < betAmount) {
            player.sendMessage(TicTacToe.getStringInConfig("other-player-doesnt-have-enough-money"));
            cancelChallenge(challenge);
            return;
        }

        try {
            getEssentials().getUser(challenger).setMoney(getEssentials().getUser(challenger).getMoney().subtract(BigDecimal.valueOf(betAmount)));
            getEssentials().getUser(targetPlayer).setMoney(getEssentials().getUser(targetPlayer).getMoney().subtract(BigDecimal.valueOf(betAmount)));

            SoundCommands.playSound(challenger, Sound.BLOCK_BELL_RESONATE);
            SoundCommands.playSound(targetPlayer, Sound.BLOCK_BELL_RESONATE);
            BoardManager.buildBoard(challenger, targetPlayer, betAmount);

        } catch (Exception ignored) { }

        activeChallenges.remove(challenge);

    }

    private static void cancelChallenge(Challenge challenge) {
        String cancelMessage = getStringInConfig("cancelled-challenge");

        challenge.challenger.sendMessage(cancelMessage.replaceAll("\\$player", challenge.opponent.getName()));
        challenge.opponent.sendMessage(cancelMessage.replaceAll("\\$player", challenge.challenger.getName()));

        activeChallenges.remove(challenge);
    }

    public static void cancelChallenge(Player player, String[] args) {

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /ttt challenge cancel <player>");
            return;
        }

        Challenge challenge = getChallenge(player, args);
        if (challenge == null)
            return;

        cancelChallenge(challenge);

    }

    private static Challenge getChallenge(Player player, String[] args) {
        return activeChallenges.stream()
                .filter(c -> (c.challenger.equals(Bukkit.getServer().getPlayer(args[2])) && c.opponent.equals(player)) ||
                        (c.challenger.equals(player) && c.opponent.equals(Bukkit.getServer().getPlayer(args[2])) ))
                .findFirst()
                .orElse(null);
    }


}

class Challenge {

    public Challenge(Player challenger, Player opponent, long betAmount) {
        this.challenger = challenger;
        this.opponent = opponent;
        this.betAmount = betAmount;
    }

    public Player challenger;
    public Player opponent;
    public long betAmount;

}