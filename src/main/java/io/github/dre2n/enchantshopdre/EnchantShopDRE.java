/*
 * Copyright (C) 2017 Daniel Saukel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.dre2n.enchantshopdre;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import static org.bukkit.ChatColor.*;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Daniel Saukel
 */
public class EnchantShopDRE extends JavaPlugin implements Listener {

    Map<String, Integer> prices = new HashMap<>();
    String success = YELLOW + "You have successfully enchanted your " + DARK_AQUA + "%item%" + YELLOW + " with " + DARK_AQUA + "%enchantment%" + YELLOW + ".";
    String fail = DARK_RED + "You do not have enough experience!";
    String onInteract = "bs enchant";

    @Override
    public void onEnable() {
        if (!getConfig().contains("success")) {
            getConfig().set("success", success.replaceAll("\u00a7", "&"));
        }
        if (!getConfig().contains("fail")) {
            getConfig().set("fail", fail.replaceAll("\u00a7", "&"));
        }
        if (!getConfig().contains("onInteract")) {
            getConfig().set("onInteract", onInteract);
        }
        if (!getConfig().contains("prices")) {
            getConfig().createSection("prices");
        }
        saveConfig();
        success = ChatColor.translateAlternateColorCodes('&', getConfig().getString("success"));
        fail = ChatColor.translateAlternateColorCodes('&', getConfig().getString("fail"));
        Map<String, Object> prices = getConfig().getConfigurationSection("prices").getValues(false);
        for (Entry<String, Object> entry : prices.entrySet()) {
            this.prices.put(entry.getKey(), (int) entry.getValue());
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onEnchantTableInteract(PlayerInteractEvent event) {
        boolean wasOp = event.getPlayer().isOp();
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            event.getPlayer().setOp(true);
            event.getPlayer().performCommand(onInteract);
            event.getPlayer().setOp(wasOp);
        }
    }

    @EventHandler
    public void onAnvilUse(PrepareAnvilEvent event) {
        if (event.getInventory().contains(Material.ENCHANTED_BOOK)) {
            event.setResult(null);
        }
        ItemStack sword = event.getInventory().getItem(1);
        if (sword == null) {
            return;
        }
        if (sword.getType() == Material.IRON_SWORD || sword.getType() == Material.GOLD_SWORD || sword.getType() == Material.IRON_SWORD) {
            event.setResult(null);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            return false;
        }
        if (args.length < 3) {
            return false;
        }
        Enchantment enchantment = Enchantment.getByName(args[0]);
        if (enchantment == null) {
            return false;
        }
        int level = 0;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            return false;
        }
        Player player = Bukkit.getPlayer(args[2]);
        if (player == null || !player.isOnline()) {
            return false;
        }
        Integer price = prices.get(enchantment.getName() + "#" + level);
        if (price == null || player.getLevel() < price) {
            player.sendMessage(fail);
            return false;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) {
            return false;
        }
        String name = item.getItemMeta().getDisplayName() != null ? item.getItemMeta().getDisplayName() : item.getType().toString().replace("_", " ");
        item.addUnsafeEnchantment(enchantment, level);
        player.setLevel(player.getLevel() - price);
        player.sendMessage(success.replace("%item%", name).replace("%enchantment%", enchantment.getName().replace("_", " ")));
        return true;
    }

}
