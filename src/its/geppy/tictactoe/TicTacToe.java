package its.geppy.tictactoe;

import com.earth2me.essentials.Essentials;
import its.geppy.tictactoe.Commands.AllCommands;
import its.geppy.tictactoe.Listeners.cancelEvents;
import its.geppy.tictactoe.Listeners.onPlayerInteractEvent;
import its.geppy.tictactoe.Listeners.onPlayerQuitEvent;
import its.geppy.tictactoe.Utilities.GameData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TicTacToe extends JavaPlugin implements CommandExecutor {

    public static List<GameData> activeGames = new ArrayList<>();

    private static TicTacToe mainInstance;
    public static TicTacToe getMain() { return mainInstance; }

    public static int maxIdleTime;
    public static int maxDistanceFromBoard;

    private static ItemStack tool;

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

        setToolItem();

    }

    @Override
    public void onDisable() {

        for (int i = 0; i < activeGames.size(); i++) {
            try {
                activeGames.get(i).endGame();
            } catch (Exception ignored) { }
        }

    }

    private static void setToolItem() {
        String materialString = getMain().getConfig().getString("item-material");
        if (materialString == null) materialString = "STICK";

        Material itemMaterial = Material.getMaterial(materialString);
        tool = new ItemStack(itemMaterial, 1);
        ItemMeta meta = tool.getItemMeta();
        meta.setDisplayName(getStringInConfig("item-name"));
        List<String> lore = getMain().getConfig().getStringList("item-lore");
        lore = lore.stream()
                .map(l -> ChatColor.translateAlternateColorCodes('&', l))
                .collect(Collectors.toList());
        meta.setLore(lore);
        tool.setItemMeta(meta);
    }

    public static ItemStack getToolItem() { return tool; }

    public static Essentials getEssentials() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
        if (plugin instanceof Essentials)
            return (Essentials) plugin;
        return null;
    }

    public static String getStringInConfig(String path) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getMain().getConfig().getString(path)));
    }

    public static GameData isPlaying(LivingEntity entity) {
        return TicTacToe.activeGames.stream()
                .filter(game -> {
                    if (game.getChallenger().equals(entity))
                        return true;

                    if (game.getOpponent().equals(entity))
                        return true;

                    return false;
                })
                .findFirst()
                .orElse(null);
    }

}
