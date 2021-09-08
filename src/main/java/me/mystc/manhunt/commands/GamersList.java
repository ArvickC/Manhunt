package me.mystc.manhunt.commands;

import me.mystc.manhunt.Manhunt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GamersList implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        System.out.println(Manhunt.gamers);

        return false;
    }
}
