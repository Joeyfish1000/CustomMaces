package com.maces;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomMaces extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Custom Maces has awoken!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("slimehammer") && sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack hammer = new ItemStack(Material.MACE);
            ItemMeta meta = hammer.getItemMeta();
            meta.setDisplayName("§aSlime Hammer");
            hammer.setItemMeta(meta);
            
            player.getInventory().addItem(hammer);
            player.sendMessage("§eBoing! You received the Slime Hammer!");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onMaceSmash(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();
            
            if (item.getType() == Material.MACE && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§aSlime Hammer")) {
                if (player.getFallDistance() > 1.5) {
                    player.setVelocity(player.getVelocity().setY(1.2));
                    player.sendMessage("§aBOING!");
                }
            }
        }
    }
}
