package its.geppy.tictactoe.Commands;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static its.geppy.tictactoe.TicTacToe.getMain;

public class SoundCommands {

    public static void playSound(Player player, Sound sound) {

        PersistentDataContainer dataContainer = player.getPersistentDataContainer();
        Byte sounds = dataContainer.get(new NamespacedKey(getMain(), "ttt_sounds"), PersistentDataType.BYTE);

        if (sounds == null)
            soundOn(player);
        else if (sounds.equals(Byte.valueOf("0")))
            return;

        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 1, 1);

    }

    public static void soundOn(Player player) {

        PersistentDataContainer dataContainer = player.getPersistentDataContainer();
        dataContainer.set(new NamespacedKey(getMain(), "ttt_sounds"), PersistentDataType.BYTE, Byte.valueOf("1"));

    }

    public static void soundOff(Player player) {

        PersistentDataContainer dataContainer = player.getPersistentDataContainer();
        dataContainer.set(new NamespacedKey(getMain(), "ttt_sounds"), PersistentDataType.BYTE, Byte.valueOf("0"));

    }

}
