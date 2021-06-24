package its.geppy.tictactoe.Commands;

import its.geppy.tictactoe.TicTacToe;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ToolCommand {

    private static Map<UUID, LocalDateTime> cooldowns = new HashMap<>();

    public static void spawnItem(Player player) {

        if (player.getInventory().contains(TicTacToe.getToolItem())) {
            player.sendMessage(TicTacToe.getStringInConfig("tool-already-in-inventory"));
            return;
        }

        if (isInCooldown(player)) {
            player.sendMessage(TicTacToe.getStringInConfig("tool-command-in-cooldown"));
            return;
        }

        if (player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            player.getInventory().setItemInMainHand(TicTacToe.getToolItem());
            setCooldown(player);
            return;
        }

        player.getInventory().addItem(TicTacToe.getToolItem());
        setCooldown(player);

    }

    private static boolean isInCooldown(Player player) {
        if (player.hasPermission("ttt.tool.nocooldown"))
            return false;

        if (!cooldowns.containsKey(player.getUniqueId()))
            return false;

        LocalDateTime time = LocalDateTime.now();

        return !cooldowns.get(player.getUniqueId()).isBefore(time);

    }

    private static void setCooldown(Player player) {
        int cooldownLength = TicTacToe.getMain().getConfig().getInt("tool-cooldown-in-seconds");

        LocalDateTime time = LocalDateTime.now().plusSeconds(cooldownLength);

        cooldowns.put(player.getUniqueId(), time);

    }
}
