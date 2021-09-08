package me.mystc.manhunt.commands;

import me.mystc.manhunt.Manhunt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Spectator implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Checks
        if(!sender.hasPermission("manhunt.spectator")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c No Permission"));
            return false;
        }
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c You need to be a player to run that command"));
            return false;
        }

        Player p = (Player)sender;
        if(Manhunt.spectators.contains(p)) {
            Block loc = Bukkit.getWorld("world").getHighestBlockAt(0, 0);
            p.teleport(loc.getLocation().add(new Vector(0, 1, 0)));
            p.getInventory().clear();
            p.setSaturation(20f);
            p.setGameMode(GameMode.SURVIVAL);
            p.setBedSpawnLocation(loc.getLocation().add(new Vector(0, 1, 0)));
            p.setHealth(20f);
            Manhunt.spectators.remove(p);
        } else {
            Manhunt.setSpectator(p);
        }

        return false;
    }
}
