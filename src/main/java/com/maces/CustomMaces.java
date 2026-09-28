package com.maces;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomMaces extends JavaPlugin implements Listener, TabCompleter {

    private NamespacedKey maceKey;

    @Override
    public void onEnable() {
        this.maceKey = new NamespacedKey(this, "mace_type");
        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("getmace") != null) {
            getCommand("getmace").setExecutor(this);
            getCommand("getmace").setTabCompleter(this);
        }

        registerRecipes();
        getLogger().info("Custom Maces v1.1 loaded successfully!");
    }

    private void registerRecipes() {
        NamespacedKey slimeKey = new NamespacedKey(this, "craft_slime_hammer");
        
        // Prevent duplicate recipe registration on server reloads
        if (Bukkit.getRecipe(slimeKey) == null) {
            ShapedRecipe slimeRecipe = new ShapedRecipe(slimeKey, createMace("slime"));
            // 7 Slime blocks wrapped around a center Mace
            slimeRecipe.shape("SSS", "SMS", " S ");
            slimeRecipe.setIngredient('S', Material.SLIME_BLOCK);
            slimeRecipe.setIngredient('M', Material.MACE);
            Bukkit.addRecipe(slimeRecipe);
        }
    }

    public ItemStack createMace(String type) {
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        switch (type.toLowerCase()) {
            case "slime":
                meta.setDisplayName("§aSlime Hammer");
                meta.setLore(Arrays.asList("§7A bouncy hammer infused with pure slime.", "§eAbility: §fSuper Rebound"));
                meta.setCustomModelData(1001);
                pdc.set(maceKey, PersistentDataType.STRING, "slime");
                break;
            case "thor":
                meta.setDisplayName("§bThor's Gavel");
                meta.setLore(Arrays.asList("§7Channel the fury of the storm.", "§eAbility: §fLightning Strike"));
                meta.setCustomModelData(1002);
                pdc.set(maceKey, PersistentDataType.STRING, "thor");
                break;
            case "toxic":
                meta.setDisplayName("§2Toxic Thumper");
                meta.setLore(Arrays.asList("§7Dripping with lethal venom.", "§eAbility: §fPoison Gas"));
                meta.setCustomModelData(1003);
                pdc.set(maceKey, PersistentDataType.STRING, "toxic");
                break;
            case "wither":
                meta.setDisplayName("§8Wither's Kiss");
                meta.setLore(Arrays.asList("§7Forged from nether decay.", "§eAbility: §fWither Nova"));
                meta.setCustomModelData(1004);
                pdc.set(maceKey, PersistentDataType.STRING, "wither");
                break;
            case "sonic":
                meta.setDisplayName("§3Sonic Boom");
                meta.setLore(Arrays.asList("§7Harnesses Deep Dark vibrations.", "§eAbility: §fSonic Shriek"));
                meta.setCustomModelData(1005);
                pdc.set(maceKey, PersistentDataType.STRING, "sonic");
                break;
            case "bamboo":
                meta.setDisplayName("§eBamboo Whacker");
                meta.setLore(Arrays.asList("§7Lightweight and rapid.", "§eAbility: §fQuick Smash"));
                meta.setCustomModelData(1006);
                pdc.set(maceKey, PersistentDataType.STRING, "bamboo");
                break;
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
            player.sendMessage("§cUnknown mace type! Try: slime, thor, toxic, wither, sonic, bamboo");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> validTypes = Arrays.asList("slime", "thor", "toxic", "wither", "sonic", "bamboo");
            List<String> suggestions = new ArrayList<>();
            for (String t : validTypes) {
                if (t.startsWith(args[0].toLowerCase())) {
                    suggestions.add(t);
                }
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

        String maceType = pdc.get(maceKey, PersistentDataType.STRING);
        if (maceType == null) return;

        float fallDistance = player.getFallDistance();
        
        // Bamboo Whacker allows smashes off a flat-ground jump (1.0 block height)
        if (maceType.equals("bamboo")) {
            if (fallDistance <= 1.0) return;
            handleBambooSmash(player, event);
            return;
        }

        // All other maces require the standard 1.5 block drop
        if (fallDistance <= 1.5) return;

        switch (maceType) {
            case "slime":
                handleSlimeSmash(player, fallDistance);
                break;
            case "thor":
                handleThorSmash(player, event.getEntity().getLocation(), fallDistance);
                break;
            case "toxic":
                handleToxicSmash(player, event.getEntity().getLocation());
                break;
            case "wither":
                handleWitherSmash(player, event.getEntity().getLocation(), event.getEntity());
                break;
            case "sonic":
                handleSonicSmash(player);
                break;
        }
    }

    private void handleSlimeSmash(Player player, float fallDistance) {
        double upwardPower = Math.min(0.7 + (fallDistance * 0.09), 2.4);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.setVelocity(new Vector(0, upwardPower, 0));
            player.getWorld().spawnParticle(Particle.ITEM_SLIME, player.getLocation(), 75, 0.8, 0.3, 0.8, 0.15);
            player.getWorld().spawnParticle(Particle.SNEEZE, player.getLocation().add(0, 0.2, 0), 40, 0.6, 0.2, 0.6, 0.05);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 2.0f, 0.7f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_SQUISH, 1.8f, 0.9f);
        }, 1L);
    }

    private void handleThorSmash(Player player, Location targetLoc, float fallDistance) {
        // Calculate 1 strike per 5 blocks fallen (minimum 1)
        int strikes = Math.max(1, (int) (fallDistance / 5));
        
        for (int i = 0; i < strikes; i++) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                targetLoc.getWorld().strikeLightning(targetLoc);
            }, i * 5L); // Stagger the strikes slightly for dramatic effect
        }
    }

    private void handleToxicSmash(Player player, Location targetLoc) {
        AreaEffectCloud cloud = (AreaEffectCloud) targetLoc.getWorld().spawnEntity(targetLoc, EntityType.AREA_EFFECT_CLOUD);
        cloud.setRadius(4.0f);
        cloud.setDuration(200); // 10 seconds
        cloud.setParticle(Particle.DRAGON_BREATH);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 200, 1), true);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0), true);
        
        targetLoc.getWorld().playSound(targetLoc, Sound.BLOCK_BREWING_STAND_BREW, 1.5f, 0.5f);
    }

    private void handleWitherSmash(Player player, Location targetLoc, Entity targetEntity) {
        if (targetEntity instanceof LivingEntity) {
            ((LivingEntity) targetEntity).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
        }
        
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_WITHER_SHOOT, 1.5f, 0.8f);
        
        Location skullSpawn = targetLoc.clone().add(0, 1.5, 0);
        for (int i = 0; i < 3; i++) {
            WitherSkull skull = (WitherSkull) targetLoc.getWorld().spawnEntity(skullSpawn, EntityType.WITHER_SKULL);
            
            // Calculate outward spiral vectors
            double angle = (i * Math.PI * 2) / 3;
            Vector dir = new Vector(Math.cos(angle), 0.3, Math.sin(angle)).normalize();
            
            skull.setDirection(dir);
            skull.setVelocity(dir.multiply(1.2));
            skull.setShooter(player);
        }
    }

    private void handleSonicSmash(Player player) {
        Location start = player.getEyeLocation();
        Vector dir = start.getDirection().normalize();
        
        player.getWorld().playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 1.0f);
        
        // Cast a line of particles and damage 15 blocks forward
        for (int i = 1; i <= 15; i++) {
            Location point = start.clone().add(dir.clone().multiply(i));
            player.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1);
            
            for (Entity e : point.getWorld().getNearbyEntities(point, 1.5, 1.5, 1.5)) {
                if (e instanceof LivingEntity && !e.equals(player)) {
                    ((LivingEntity) e).damage(12.0, player); // Pierce damage
                }
            }
        }
    }

    private void handleBambooSmash(Player player, EntityDamageByEntityEvent event) {
        // Cap the base damage so you can't abuse the 1-block jump for massive damage
        event.setDamage(Math.min(event.getDamage(), 5.0)); 
        
        Location loc = event.getEntity().getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_BAMBOO_BREAK, 2.0f, 1.2f);
        loc.getWorld().spawnParticle(Particle.BLOCK, loc.add(0, 1, 0), 30, 0.4, 0.4, 0.4, Material.BAMBOO.createBlockData());
    }
}
