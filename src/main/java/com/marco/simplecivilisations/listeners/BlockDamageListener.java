package com.marco.simplecivilisations.listeners;

import com.marco.simplecivilisations.SimpleCivilisations;
import com.marco.simplecivilisations.sql.Civilisation;
import com.marco.simplecivilisations.sql.Pillar;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class BlockDamageListener extends EventListener {
    public BlockDamageListener(SimpleCivilisations plugin) {
        super(plugin);
    }

    @EventHandler
    public void listen(BlockDamageEvent event) {
        if (event.getBlock().getType() != Material.OBSIDIAN) return;
        Block block = event.getBlock();
        for (Civilisation civilisation : plugin.civilisations.values()) {
            for (Pillar pillar : civilisation.getPillars()) {
                if (block.getLocation().equals(pillar.getLocation()) && pillar.isActive()) {
                    Player player = event.getPlayer();
                    if (!civilisation.hasMember(player.getUniqueId())) {
                        if (event.getItemInHand().getType() != Material.DIAMOND_PICKAXE || event.getItemInHand().getEnchantments().size() != 0) {
                            event.setCancelled(true);
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "You can only break this block with an unenchanted diamond pickaxe."));
                            return;
                        }

                        int online = 0;
                        for (UUID uuid : civilisation.getMembers()) if (Bukkit.getPlayer(uuid) != null) online++;
                        int require = (civilisation.getMembers().size() + 2) / 3;

                        if (online < require) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 0.5f);
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.YELLOW + civilisation.getName() + ChatColor.RED + " does not have enough members online (" + online + "/" + require + ")."));
                            return;
                        }

                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "You are now contesting the territory of " + ChatColor.YELLOW + civilisation.getName() + ChatColor.RED + "."));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 90, 1));
                        Bukkit.getOnlinePlayers().forEach(p -> {
                            if (civilisation.hasMember(p.getUniqueId())) {
                                p.sendTitle(ChatColor.RED + "Pillar being contested!", ChatColor.GRAY + "One of your pillars is being contested.", 1, 80, 1);
                                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
                            }
                        });
                    }
                    return;
                }
            }
        }
    }
}
