package its.geppy.tictactoe.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AllCommands implements CommandExecutor, TabCompleter {

    Map<String, String> commandList = new HashMap<String, String>() {{
        put("play", "ttt.play");
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

            case "play":
                ChallengeStick.spawnItem(player);
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
                    Method method = getClass().getDeclaredMethod(currentArg.replace("$", ""), List.class);
                    invokeReturns = (List<String>) method.invoke(this, Arrays.asList(args));
                } catch (Exception e) { e.printStackTrace();}
            } else
                returnList.add(currentArg);

            for (String s : invokeReturns) {
                if (s.startsWith(playersCurrentArg))
                    returnList.add(s);
            }

        }


        return returnList;
    }

    //private List<String> getCollections(List<String> args) {
    //    return new ArrayList<>(getMain().getCollectionsYML().getKeys(false));
    //}
}