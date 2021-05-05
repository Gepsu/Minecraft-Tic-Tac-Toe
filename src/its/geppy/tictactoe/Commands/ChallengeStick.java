package its.geppy.tictactoe.Commands;

import its.geppy.tictactoe.TicTacToe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChallengeStick {

    private static Map<UUID, LocalDateTime> cooldowns = new HashMap<>();

    public static void spawnItem(Player player) {

        if (player.getInventory().contains(TicTacToe.getChallengeItem())) {
            player.sendMessage(ChatColor.RED + "You already have the item.");
            return;
        }

        if (isInCooldown(player)) {
            player.sendMessage(ChatColor.RED + "The command is in cooldown.");
            return;
        }

        if (player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            player.getInventory().setItemInMainHand(TicTacToe.getChallengeItem());
            setCooldown(player);
            return;
        }

        player.getInventory().addItem(TicTacToe.getChallengeItem());
        setCooldown(player);

    }

    private static boolean isInCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId()))
            return false;

        LocalDateTime time = LocalDateTime.now();

        return !cooldowns.get(player.getUniqueId()).isBefore(time);

    }

    private static void setCooldown(Player player) {
        int cooldownLength = TicTacToe.getMain().getConfig().getInt("sticktactoe-cooldown-in-seconds");

        LocalDateTime time = LocalDateTime.now().plusSeconds(cooldownLength);

        cooldowns.put(player.getUniqueId(), time);

    }

}
