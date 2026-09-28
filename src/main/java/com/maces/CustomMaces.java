package com.maces;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class CustomMaces extends JavaPlugin implements Listener, TabCompleter {

    private NamespacedKey maceKey;
    private final Map<UUID, ItemStack[]> hiddenArmor = new HashMap<>();

    @Override
    public void onEnable() {
        this.maceKey = new NamespacedKey(this, "mace_type");
        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("getmace") != null) {
            getCommand("getmace").setExecutor(this);
            getCommand("getmace").setTabCompleter(this);
        }

        registerRecipes();
        startAquaticHoldingTask();
        getLogger().info("Custom Maces v1.2 loaded successfully!");
    }

    private void startAquaticHoldingTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                ItemStack item = p.getInventory().getItemInMainHand();
                if (item.getType() == Material.MACE && item.hasItemMeta()) {
                    String type = item.getItemMeta().getPersistentDataContainer().get(maceKey, PersistentDataType.STRING);
                    if ("aquatic".equals(type)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, true, false));
                    }
                }
            }
        }, 0L, 20L); // Runs every 1 second
    }

    private void registerRecipes() {
        // v1.2: All recipes now use a Heavy Core in the center instead of a Mace
        registerSingleRecipe("slime_hammer_recipe", "slime", Material.SLIME_BLOCK);
        registerSingleRecipe("diamond_mace_recipe", "diamond", Material.DIAMOND_BLOCK);
        registerSingleRecipe("breeze_mace_recipe", "breeze", Material.BREEZE_ROD);
        registerSingleRecipe("frozen_mace_recipe", "frozen", Material.PACKED_ICE);
        registerSingleRecipe("aquatic_mace_recipe", "aquatic", Material.PRISMARINE);
        registerSingleRecipe("nether_mace_recipe", "nether", Material.NETHER_BRICK);
        registerSingleRecipe("ender_mace_recipe", "ender", Material.END_STONE);
        registerSingleRecipe("invis_mace_recipe", "invisibility", Material.PHANTOM_MEMBRANE);
        registerSingleRecipe("warden_mace_recipe", "warden", Material.REINFORCED_DEEPSLATE); // Note: unattainable in survival, used as placeholder
    }

    private void registerSingleRecipe(String keyName, String type, Material outerBlock) {
        NamespacedKey key = new NamespacedKey(this, keyName);
        if (Bukkit.getRecipe(key) == null) {
            ShapedRecipe recipe = new ShapedRecipe(key, createMace(type));
            recipe.shape("OOO", "OCO", "OOO");
            recipe.setIngredient('O', outerBlock);
            recipe.setIngredient('C', Material.HEAVY_CORE);
            Bukkit.addRecipe(recipe);
        }
    }

    public ItemStack createMace(String type) {
        ItemStack item = new ItemStack(Material.MACE);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        switch (type.toLowerCase()) {
            case "slime":
                meta.setDisplayName("§aSlime Hammer"); meta.setCustomModelData(1001); break;
            case "thor":
                meta.setDisplayName("§bThor's Gavel"); meta.setCustomModelData(1002); break;
            case "toxic":
                meta.setDisplayName("§2Toxic Thumper"); meta.setCustomModelData(1003); break;
            case "wither":
                meta.setDisplayName("§8Wither's Kiss"); meta.setCustomModelData(1004); break;
            case "sonic":
                meta.setDisplayName("§3Sonic Boom"); meta.setCustomModelData(1005); break;
            case "bamboo":
                meta.setDisplayName("§eBamboo Whacker"); meta.setCustomModelData(1006); break;
            case "diamond":
                meta.setDisplayName("§bDiamond Mace"); meta.setCustomModelData(1007); break;
            case "breeze":
                meta.setDisplayName("§fBreeze Mace"); meta.setCustomModelData(1008); break;
            case "frozen":
                meta.setDisplayName("§bFrozen Mace"); meta.setCustomModelData(1009); break;
            case "aquatic":
                meta.setDisplayName("§3Aquatic Mace"); meta.setCustomModelData(1010); break;
            case "nether":
                meta.setDisplayName("§4Nether Mace"); meta.setCustomModelData(1011); break;
            case "ender":
                meta.setDisplayName("§5Ender Mace"); meta.setCustomModelData(1012); break;
            case "invisibility":
                meta.setDisplayName("§7Invisibility Mace"); meta.setCustomModelData(1013); break;
            case "warden":
                meta.setDisplayName("§1Warden Mace"); meta.setCustomModelData(1014); break;
            default: return null;
        }
        pdc.set(maceKey, PersistentDataType.STRING, type.toLowerCase());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("§cUsage: /getmace <type>");
            return true;
        }
        ItemStack mace = createMace(args[0]);
        if (mace != null) {
            player.getInventory().addItem(mace);
        } else {
            player.sendMessage("§cUnknown type.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> validTypes = Arrays.asList("slime", "thor", "toxic", "wither", "sonic", "bamboo", 
                    "diamond", "breeze", "frozen", "aquatic", "nether", "ender", "invisibility", "warden");
            List<String> suggestions = new ArrayList<>();
            for (String t : validTypes) if (t.startsWith(args[0].toLowerCase())) suggestions.add(t);
            return suggestions;
        }
        return List.of();
    }

    // Breeze Mace Right-Click
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player p = event.getPlayer();
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.MACE && item.hasItemMeta()) {
                String type = item.getItemMeta().getPersistentDataContainer().get(maceKey, PersistentDataType.STRING);
                if ("breeze".equals(type)) {
                    if (!p.hasCooldown(Material.MACE)) {
                        p.setVelocity(new Vector(0, 1.5, 0)); // Approx 10 blocks high
                        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BREEZE_JUMP, 1f, 1f);
                        p.setCooldown(Material.MACE, 60); // 3 sec cooldown
                    }
                }
            }
        }
    }

    // Armor Restoration Fallbacks
    @EventHandler
    public void onQuit(PlayerQuitEvent e) { restoreArmor(e.getPlayer()); }
    
    @EventHandler
    public void onDeath(PlayerDeathEvent e) { restoreArmor(e.getEntity()); }

    private void restoreArmor(Player player) {
        if (hiddenArmor.containsKey(player.getUniqueId())) {
            player.getInventory().setArmorContents(hiddenArmor.remove(player.getUniqueId()));
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    @EventHandler
    public void onMaceSmash(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() != Material.MACE || !item.hasItemMeta()) return;
        String maceType = item.getItemMeta().getPersistentDataContainer().get(maceKey, PersistentDataType.STRING);
        if (maceType == null) return;

        float fallDistance = player.getFallDistance();
        
        if (maceType.equals("bamboo")) {
            if (fallDistance > 1.0) handleBambooSmash(player, event);
            return;
        }
        
        if (fallDistance <= 1.5) return;

        Entity target = event.getEntity();
        Location targetLoc = target.getLocation();

        switch (maceType) {
            case "slime": handleSlimeSmash(player, fallDistance); break;
            case "thor": handleThorSmash(targetLoc, fallDistance); break;
            case "toxic": handleToxicSmash(targetLoc); break;
            case "wither": handleWitherSmash(player, targetLoc, target); break;
            case "sonic": handleSonicSmash(player); break;
            case "diamond":
                event.setDamage(event.getDamage() + (fallDistance * 2.5)); // Heavy scaling
                player.getWorld().playSound(targetLoc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 2f, 1f);
                break;
            case "frozen":
                if (target instanceof LivingEntity) ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, targetLoc, 50, 0.5, 0.5, 0.5, 0.05);
                break;
            case "aquatic":
                handleAquaticSmash(player, targetLoc, event);
                break;
            case "nether":
                handleNetherSmash(player, targetLoc, target, event);
                break;
            case "ender":
                handleEnderSmash(targetLoc, target);
                break;
            case "invisibility":
                handleInvisSmash(player);
                break;
            case "warden":
                handleWardenSmash(player, targetLoc, target, event);
                break;
        }
    }

    private void handleAquaticSmash(Player player, Location targetLoc, EntityDamageByEntityEvent event) {
        boolean raining = player.getWorld().hasStorm();
        if (raining) {
            event.setDamage(event.getDamage() * 1.5);
            Bukkit.getScheduler().runTaskLater(this, () -> player.setVelocity(new Vector(0, 1.2, 0)), 1L);
        }
        
        List<BlockState> cachedStates = new ArrayList<>();
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block b = targetLoc.clone().add(x, -1, z).getBlock();
                if (b.getType().isSolid() && b.getType() != Material.AIR) {
                    cachedStates.add(b.getState());
                    b.setType(Math.random() > 0.5 ? Material.PRISMARINE : Material.DARK_PRISMARINE);
                }
            }
        }
        player.getWorld().playSound(targetLoc, Sound.ITEM_TRIDENT_HIT, 1f, 1f);
        
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (BlockState state : cachedStates) state.update(true, false);
        }, 100L); // Revert after 5 seconds
    }

    private void handleNetherSmash(Player player, Location targetLoc, Entity target, EntityDamageByEntityEvent event) {
        target.setFireTicks(100);
        player.setHealth(Math.min(player.getHealth() + (event.getDamage() * 0.25), player.getMaxHealth()));
        
        if (target instanceof LivingEntity) {
            spawnMinions(targetLoc, (LivingEntity) target, EntityType.WITHER_SKELETON, 3);
        }
    }

    private void handleEnderSmash(Location targetLoc, Entity target) {
        Location randomTeleport = targetLoc.clone().add((Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10);
        target.teleport(randomTeleport);
        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        
        if (target instanceof LivingEntity) {
            spawnMinions(randomTeleport, (LivingEntity) target, EntityType.ENDERMITE, 1);
        }
    }

    private void spawnMinions(Location loc, LivingEntity target, EntityType type, int count) {
        for (int i = 0; i < count; i++) {
            Mob mob = (Mob) loc.getWorld().spawnEntity(loc, type);
            mob.setTarget(target);
            
            // Task to check if target is lost
            Bukkit.getScheduler().runTaskTimer(this, task -> {
                if (mob.isDead()) { task.cancel(); return; }
                if (mob.getTarget() == null || mob.getTarget().isDead() || mob.getTarget().getLocation().distance(mob.getLocation()) > 30) {
                    mob.remove();
                    task.cancel();
                }
            }, 0L, 20L);
            
            // Absolute 10 second despawn
            Bukkit.getScheduler().runTaskLater(this, mob::remove, 200L);
        }
    }

    private void handleInvisSmash(Player player) {
        hiddenArmor.put(player.getUniqueId(), player.getInventory().getArmorContents());
        player.getInventory().setArmorContents(null);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, false, false));
        
        Bukkit.getScheduler().runTaskLater(this, () -> player.setVelocity(new Vector(0, 1.8, 0)), 1L);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_SWOOP, 1f, 1f);
        
        Bukkit.getScheduler().runTaskLater(this, () -> restoreArmor(player), 100L);
    }

    private void handleWardenSmash(Player player, Location targetLoc, Entity target, EntityDamageByEntityEvent event) {
        if (target instanceof Player) {
            Player pTarget = (Player) target;
            if (pTarget.isBlocking()) pTarget.setCooldown(Material.SHIELD, 100);
        }
        
        if (target instanceof LivingEntity) {
            ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
            ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));
        }
        player.getWorld().spawnParticle(Particle.SCULK_SOUL, targetLoc, 30, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(targetLoc, Sound.ENTITY_WARDEN_ROAR, 1f, 1.5f);
    }

    // Retained Legacy V1.1 Handlers (Slime, Thor, Toxic, Wither, Sonic, Bamboo)
    private void handleSlimeSmash(Player player, float fallDistance) {
        double upwardPower = Math.min(0.7 + (fallDistance * 0.09), 2.4);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.setVelocity(new Vector(0, upwardPower, 0));
            player.getWorld().spawnParticle(Particle.ITEM_SLIME, player.getLocation(), 75, 0.8, 0.3, 0.8, 0.15);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 2.0f, 0.7f);
        }, 1L);
    }

    private void handleThorSmash(Location loc, float fallDistance) {
        int strikes = Math.max(1, (int) (fallDistance / 5));
        for (int i = 0; i < strikes; i++) {
            Bukkit.getScheduler().runTaskLater(this, () -> loc.getWorld().strikeLightning(loc), i * 5L);
        }
    }

    private void handleToxicSmash(Location loc) {
        AreaEffectCloud cloud = (AreaEffectCloud) loc.getWorld().spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
        cloud.setRadius(4.0f); cloud.setDuration(200); cloud.setParticle(Particle.DRAGON_BREATH);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 200, 1), true);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0), true);
    }

    private void handleWitherSmash(Player player, Location targetLoc, Entity target) {
        if (target instanceof LivingEntity) ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
        Location skullSpawn = targetLoc.clone().add(0, 1.5, 0);
        for (int i = 0; i < 3; i++) {
            WitherSkull skull = (WitherSkull) targetLoc.getWorld().spawnEntity(skullSpawn, EntityType.WITHER_SKULL);
            double angle = (i * Math.PI * 2) / 3;
            Vector dir = new Vector(Math.cos(angle), 0.3, Math.sin(angle)).normalize();
            skull.setDirection(dir); skull.setVelocity(dir.multiply(1.2)); skull.setShooter(player);
        }
    }

    private void handleSonicSmash(Player player) {
        Location start = player.getEyeLocation();
        Vector dir = start.getDirection().normalize();
        player.getWorld().playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 3.0f, 1.0f);
        for (int i = 1; i <= 15; i++) {
            Location point = start.clone().add(dir.clone().multiply(i));
            player.getWorld().spawnParticle(Particle.SONIC_BOOM, point, 1);
            for (Entity e : point.getWorld().getNearbyEntities(point, 1.5, 1.5, 1.5)) {
                if (e instanceof LivingEntity && !e.equals(player)) ((LivingEntity) e).damage(12.0, player);
            }
        }
    }

    private void handleBambooSmash(Player player, EntityDamageByEntityEvent event) {
        event.setDamage(Math.min(event.getDamage(), 5.0)); 
        Location loc = event.getEntity().getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_BAMBOO_BREAK, 2.0f, 1.2f);
    }
}
