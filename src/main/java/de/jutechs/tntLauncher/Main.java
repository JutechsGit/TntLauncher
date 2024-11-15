package de.jutechs.tntLauncher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class Main extends JavaPlugin implements Listener {

    private static final String TNT_LAUNCHER_NAME = ChatColor.RED + "T" + ChatColor.WHITE + "N" + ChatColor.RED + "T " + ChatColor.GOLD + "Launcher";;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        addTNTLauncherRecipe();
        getLogger().info("TNT Launcher plugin enabled!");
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        getLogger().info("TNT Launcher plugin disabled!");
        // Plugin shutdown logic
    }
    public ItemStack createTNTLauncher() {
        ItemStack launcher = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = launcher.getItemMeta();
        if (meta != null) {
            // Set the custom name
            meta.setDisplayName(TNT_LAUNCHER_NAME);

            // Add lore
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Unleash explosive power!");
            lore.add(ChatColor.YELLOW + "Right-click to launch TNT that");
            lore.add(ChatColor.YELLOW + "explodes on impact!");
            meta.setLore(lore);

            // Add glowing effect
            meta.addEnchant(Enchantment.INFINITY, 1, true);
            launcher.setItemMeta(meta);
        }
        return launcher;
    }

    // Add custom crafting recipe
    private void addTNTLauncherRecipe() {
        ItemStack tntLauncher = createTNTLauncher();
        NamespacedKey key = new NamespacedKey(this, "tnt_launcher");
        ShapedRecipe recipe = new ShapedRecipe(key, tntLauncher);
        recipe.shape("TBT", "BDB", "TBT");
        recipe.setIngredient('T', Material.TNT);
        recipe.setIngredient('B', Material.BLAZE_POWDER);
        recipe.setIngredient('D', Material.DIAMOND);

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals(TNT_LAUNCHER_NAME)) {
            event.setCancelled(true);

            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();

            // Check cooldown
            if (cooldowns.containsKey(playerId)) {
                long lastUse = cooldowns.get(playerId);
                if (currentTime - lastUse < 300) { // 100 milliseconds cooldown
                    return; // Ignore the interaction if still on cooldown
                }
            }

            // Update cooldown
            cooldowns.put(playerId, currentTime);

            // Launch TNT
            TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getLocation().add(0, 1.5, 0), EntityType.TNT);
            tnt.setVelocity(player.getLocation().getDirection().multiply(2)); // Adjust speed as needed
            tnt.setFuseTicks(80); // TNT fuse time in ticks
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getType() == EntityType.TNT) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            tnt.getWorld().createExplosion(tnt.getLocation(), 4.0F, false, false); // TNT explosion
            tnt.remove(); // Remove TNT entity after explosion
        }
    }
}