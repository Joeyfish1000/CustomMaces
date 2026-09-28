package com.maces;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomMaces extends JavaPlugin implements Listener, TabCompleter {

    // Unique key used to identify mace types invisibly
    private NamespacedKey maceKey;

    @Override
    public void onEnable() {
        this.maceKey = new NamespacedKey(this, "mace_type");
        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("getmace") != null) {
            getCommand("getmace").setExecutor(this);
            getCommand("getmace").setTabCompleter(this);
        }

        getLogger().info("Custom Maces loaded successfully!");
    }

    // Factory method to build any custom mace cleanly
    public ItemStack createMace(String type) {
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        switch (type.toLowerCase()) {
            case "slime":
                meta.setDisplayName("§aSlime Hammer");
                meta.setLore(Arrays.asList(
                    "§7A bouncy hammer infused with pure slime.",
                    "§eAbility: §fSuper Rebound",
                    "§7Smashes launch you back into the air!"
                ));
                // CustomModelData: Link this to your future resource pack
                meta.setCustomModelData(1001);
                pdc.set(maceKey, PersistentDataType.STRING, "slime");
                break;

            // Future maces (e.g. Zephyr, Inferno) plug directly in here!
            default:
                return null;
        }

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cUsage: /getmace <type>");
            return true;
        }

        ItemStack mace = createMace(args[0]);
        if (mace != null) {
            player.getInventory().addItem(mace);
            player.sendMessage("§aReceived: " + mace.getItemMeta().getDisplayName());
        } else {
            player.sendMessage("§cUnknown mace type! Try: slime");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            if ("slime".startsWith(args[0].toLowerCase())) {
                suggestions.add("slime");
            }
            return suggestions;
        }
        return List.of();
    }

    @EventHandler
    public void onMaceSmash(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.MACE || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // Check internal identifier rather than display name
        String maceType = pdc.get(maceKey, PersistentDataType.STRING);
        if (maceType == null) return;

        float fallDistance = player.getFallDistance();

        // Must drop at least 1.5 blocks for a valid smash
        if (fallDistance <= 1.5) return;

        // Route effects based on the mace ID
        switch (maceType) {
            case "slime":
                handleSlimeSmash(player, fallDistance);
                break;
            // Additional maces will be handled here
        }
    }

    private void handleSlimeSmash(Player player, float fallDistance) {
        double upwardPower = Math.min(0.7 + (fallDistance * 0.09), 2.4);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.setVelocity(new Vector(0, upwardPower, 0));

            // Massive slime explosion visual: slime balls + green dust
            player.getWorld().spawnParticle(Particle.ITEM_SLIME, player.getLocation(), 75, 0.8, 0.3, 0.8, 0.15);
            player.getWorld().spawnParticle(Particle.SNEEZE, player.getLocation().add(0, 0.2, 0), 40, 0.6, 0.2, 0.6, 0.05);

            // Audio cues: heavy squash sound
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 2.0f, 0.7f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_SQUISH, 1.8f, 0.9f);

            player.sendMessage("§a§lBOING!");
        }, 1L);
    }
}
