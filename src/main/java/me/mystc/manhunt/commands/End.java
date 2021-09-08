package me.mystc.manhunt.commands;

import me.mystc.manhunt.Manhunt;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class End implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Checks
        if(!sender.hasPermission("manhunt.end")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c No Permission"));
            return false;
        }
        if(!Manhunt.preStart && !Manhunt.playing) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c No Game running!"));
            return false;
        }

        Manhunt.gameEnd();
        for(Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cGame Ended"), null, 5, 60, 5);
        }
        return false;
    }
}
