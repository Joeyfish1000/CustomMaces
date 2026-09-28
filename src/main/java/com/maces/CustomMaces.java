package com.maces;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

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
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.MACE && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§aSlime Hammer")) {
            float fallDistance = player.getFallDistance();

            // Only trigger if it counts as a fall attack
            if (fallDistance > 1.5) {
                // Calculate dynamic rebound based on height (capped so you don't breach orbit)
                double upwardPower = Math.min(0.6 + (fallDistance * 0.08), 2.2);

                // Run 1 tick later so vanilla smash physics don't zero-out the launch
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    player.setVelocity(new Vector(0, upwardPower, 0));
                    
                    // Satisfying slime squish audio and particles
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.5f, 0.8f);
                    player.getWorld().spawnParticle(Particle.ITEM_SLIME, player.getLocation(), 25, 0.4, 0.1, 0.4, 0.1);
                    player.sendMessage("§aBOING!");
                }, 1L);
            }
        }
    }
}
