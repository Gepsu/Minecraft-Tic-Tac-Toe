package its.geppy.tictactoe.Commands;

import its.geppy.tictactoe.TicTacToe;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static its.geppy.tictactoe.TicTacToe.getMain;
import static its.geppy.tictactoe.TicTacToe.getStringInConfig;

public class ParticleCommands {

    public static void changeParticles(Player player, String[] args) {

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /ttt particles change <particleType>");
            return;
        }

        String particleType = args[2];
        List<String> allowedParticles = getMain().getConfig().getStringList("player-particles");

        if (!allowedParticles.contains(particleType)) {
            player.sendMessage(getStringInConfig("invalid-particle-type"));
            return;
        }

        player.getPersistentDataContainer().set(new NamespacedKey(getMain(), "ttt_particles"), PersistentDataType.STRING, particleType);

    }

    public static void reset(Player player, String[] args) {

        if (args.length > 2 && player.hasPermission("ttt.particles.reset.admin")) {
            Player targetPlayer = getMain().getServer().getPlayer(args[2]);

            if (targetPlayer == null) {
                player.sendMessage(TicTacToe.getStringInConfig("invalid-player-name"));
                return;
            }

            targetPlayer.getPersistentDataContainer().remove(new NamespacedKey(getMain(), "ttt_particles"));
            return;
        }

        player.getPersistentDataContainer().remove(new NamespacedKey(getMain(), "ttt_particles"));

    }
}
