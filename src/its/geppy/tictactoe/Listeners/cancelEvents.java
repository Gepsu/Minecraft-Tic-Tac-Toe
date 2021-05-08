package its.geppy.tictactoe.Listeners;

import its.geppy.tictactoe.TicTacToe;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class cancelEvents implements Listener {

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {

        ItemStack heldItem = e.getPlayer().getInventory().getItemInMainHand();

        if (heldItem.isSimilar(TicTacToe.getToolItem()))
            e.setCancelled(true);


    }

}
