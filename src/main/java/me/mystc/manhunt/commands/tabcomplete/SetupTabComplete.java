package me.mystc.manhunt.commands.tabcomplete;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetupTabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Arg 1 [true/false]
        if(args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("[generate world?]");
            list.add("true");
            list.add("false");
            return list;
        }
        // Arg 2 [speedrunner]
        if(args.length == 2) {
            List<String> list = new ArrayList<>();
            for(Player p : Bukkit.getOnlinePlayers()) {
                list.add(p.getName());
            }
            return list;
        }

        return null;
    }
}
