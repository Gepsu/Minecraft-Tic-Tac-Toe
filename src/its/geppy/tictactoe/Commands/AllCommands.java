package its.geppy.tictactoe.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static its.geppy.tictactoe.TicTacToe.getEssentials;
import static its.geppy.tictactoe.TicTacToe.getMain;

public class AllCommands implements CommandExecutor, TabCompleter {

    Map<String, String> commandList = new HashMap<String, String>() {{
        put("tool", "ttt.tool");
        put("sounds off", "ttt.play");
        put("sounds on", "ttt.play");
        if (getMain().getConfig().getBoolean("essentials-support") && getEssentials() != null) put("challenge player $playerList", "ttt.play");
        if (getMain().getConfig().getBoolean("essentials-support") && getEssentials() != null) put("challenge accept $acceptableChallenges", "ttt.play");
        if (getMain().getConfig().getBoolean("essentials-support") && getEssentials() != null) put("challenge cancel $cancellableChallenges", "ttt.play");
        if (getMain().getConfig().getBoolean("allow-player-particles")) put("particles change $particleList", "ttt.particles.change");
        if (getMain().getConfig().getBoolean("allow-player-particles")) put("particles reset $playerList", "ttt.particles.reset.admin");
        if (getMain().getConfig().getBoolean("allow-player-particles")) put("particles reset", "ttt.particles.reset");
    }};

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Invalid command.");
            return false;
        }

        String cmd = getCommand(player, args).toLowerCase();
        switch(cmd) {

            case "tool":
                ToolCommand.spawnItem(player);
                break;
            case "sounds on":
                SoundCommands.soundOn(player);
                break;
            case "sounds off":
                SoundCommands.soundOff(player);
                break;
            case "particles change":
                ParticleCommands.changeParticles(player, args);
                break;
            case "particles reset":
                ParticleCommands.reset(player, args);
                break;
            case "challenge player":
                ChallengeCommand.challenge(player, args);
                break;
            case "challenge accept":
                ChallengeCommand.acceptChallenge(player, args);
                break;
            case "challenge cancel":
                ChallengeCommand.cancelChallenge(player, args);
                break;
            case "no_permission":
                player.sendMessage(ChatColor.RED + "No permission.");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid command.");
        }


        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player))
            return null;

        Player player = (Player) sender;

        if (args.length == 0)
            return null;

        return autoComplete(player, args);

    }

    private String getCommand(Player player, String[] args) {
        String allArgs = String.join(" ", args);

        Pattern p = Pattern.compile("\\s[^a-z].+");

        for (String cmd : commandList.keySet()) {

            String simplifiedCmd = cmd.replaceAll(p.pattern(), "");
            if (allArgs.equalsIgnoreCase(simplifiedCmd) || allArgs.startsWith(simplifiedCmd)) {
                if (!player.hasPermission(commandList.get(cmd)))
                    return "no_permission";

                return simplifiedCmd;
            }

        }

        return "";
    }

    private List<String> autoComplete(Player player, String[] args) {
        List<String> returnList = new ArrayList<>();
        String allArgs = String.join(" ", args);
        String playersCurrentArg = args[args.length - 1];

        Pattern p = Pattern.compile("\\s[^a-z].+");

        for (String cmd : commandList.keySet()) {

            if (cmd.startsWith("!"))
                continue;

            String simplifiedCmd = cmd.replaceAll(p.pattern(), "");
            if (!simplifiedCmd.startsWith(allArgs) && !allArgs.startsWith(simplifiedCmd))
                continue;

            if (!player.hasPermission(commandList.get(cmd)))
                continue;

            String[] splitCommand = cmd.split(" ");
            if (splitCommand.length < args.length)
                continue;

            String currentArg = splitCommand[args.length - 1];
            List<String> invokeReturns = new ArrayList<>();

            if (currentArg.startsWith("$")) {
                try {
                    Method method = getClass().getDeclaredMethod(currentArg.replace("$", ""), Player.class, List.class);
                    invokeReturns = (List<String>) method.invoke(this, player, Arrays.asList(args));
                } catch (Exception e) { e.printStackTrace();}
            } else
                returnList.add(currentArg);

            for (String s : invokeReturns) {
                if (s.toLowerCase().startsWith(playersCurrentArg.toLowerCase()))
                    returnList.add(s);
            }

        }


        return returnList;
    }

    private List<String> particleList(Player player, List<String> args) {
        return getMain().getConfig().getStringList("player-particles");
    }

    private List<String> playerList(Player player, List<String> args) {
        return getMain().getServer().getOnlinePlayers().stream()
                .filter(p -> !p.equals(player))
                .map(HumanEntity::getName).collect(Collectors.toList());
    }

    private List<String> cancellableChallenges(Player player, List<String> args) {
        return ChallengeCommand.getActiveChallenges().stream()
                .filter(c -> c.challenger.equals(player) || c.opponent.equals(player))
                .map(c -> c.opponent.equals(player) ? c.challenger.getName() : c.opponent.getName())
                .collect(Collectors.toList());
    }

    private List<String> acceptableChallenges(Player player, List<String> args) {
        return ChallengeCommand.getActiveChallenges().stream()
                .filter(c -> c.opponent.equals(player))
                .map(c -> c.challenger.getName())
                .collect(Collectors.toList());
    }
}