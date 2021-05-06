package its.geppy.tictactoe;

import its.geppy.tictactoe.Commands.AllCommands;
import its.geppy.tictactoe.Listeners.cancelEvents;
import its.geppy.tictactoe.Listeners.onPlayerInteractEvent;
import its.geppy.tictactoe.Listeners.onPlayerQuitEvent;
import its.geppy.tictactoe.Utilities.GameData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TicTacToe extends JavaPlugin implements CommandExecutor {

    public static List<GameData> activeGames = new ArrayList<>();

    private static TicTacToe mainInstance;
    public static TicTacToe getMain() { return mainInstance; }

    public static int maxIdleTime;
    public static int maxDistanceFromBoard;

    @Override
    public void onEnable() {

        mainInstance = this;

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        Objects.requireNonNull(getCommand("tictactoe")).setExecutor(new AllCommands());
        Objects.requireNonNull(getCommand("ttt")).setExecutor(new AllCommands());

        getServer().getPluginManager().registerEvents(new cancelEvents(), this);
        getServer().getPluginManager().registerEvents(new onPlayerInteractEvent(), this);
        getServer().getPluginManager().registerEvents(new onPlayerQuitEvent(), this);

        maxIdleTime = getMain().getConfig().getInt("maximum-idle-time-in-seconds") * 20;
        maxDistanceFromBoard = getMain().getConfig().getInt("maximum-distance-from-board");

    }

    @Override
    public void onDisable() {

        for (int i = 0; i < activeGames.size(); i++) {
            try {
                activeGames.get(i).endGame();
            } catch (Exception ignored) { }
        }

    }

    public static ItemStack getChallengeItem() {
        ItemStack stickTacToe = new ItemStack(Material.STICK, 1);
        ItemMeta meta = stickTacToe.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lStick&f&lTac&6&lToe"));
        stickTacToe.setItemMeta(meta);

        return stickTacToe;
    }

}
