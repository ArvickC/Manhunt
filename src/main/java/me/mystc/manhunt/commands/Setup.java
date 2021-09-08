package me.mystc.manhunt.commands;

import me.mystc.manhunt.Countdown;
import me.mystc.manhunt.Manhunt;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.awt.*;

public class Setup implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Var
        boolean isHunterOnline = false;

        // Checks
        if(!sender.hasPermission("manhunt.setup")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c No Permission"));
            return false;
        }
        if(Manhunt.preStart || Manhunt.playing) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c Game running!"));
            return false;
        }
        if(args.length == 0 || args == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c Incorrect Usage:&7 /setup [genWorld] [speedrunner]"));
            return false;
        }
        if(Bukkit.getOnlinePlayers().size() <= 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c Not enough players!"));
            return false;
        }

        //TODO gui setup

        // Args (NO GUI SETUP)
        if(args.length >= 1) {
            // Check Enough Args
            if(args.length != 2) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c Incorrect Usage:&7 /setup [genWorld] [speedrunner]"));
                return false;
            }

            // Args Walkthrough
            if(args[0].equalsIgnoreCase("true")) {
                Manhunt.genWorld = true;
            } else if(args[0].equalsIgnoreCase("false")) {
                Manhunt.genWorld = false;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c Incorrect Usage:&7 /setup [genWorld(true/false)] [speedrunner]"));
                return false;
            }

            if(Bukkit.getPlayer(args[1]).isOnline()) {
                if(Manhunt.spectators.contains(Bukkit.getPlayer(args[1]))) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c That runner is a spectator!"));
                    return false;
                }
                Manhunt.runner = Bukkit.getPlayer(args[1]);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7]&c That runner isn't online!"));
                return false;
            }

            // Start Game
            worldSetup(Manhunt.genWorld);
            Manhunt.preStart = true;
            teleportPlayers();
            countDown();
        }

        return false;
    }

    public void worldSetup(boolean gen) {
        if(gen) {
            WorldCreator wc = new WorldCreator("Manhunt");
            wc.environment(World.Environment.NORMAL);
            wc.type(WorldType.NORMAL);
            wc.createWorld();
            WorldCreator wc_n = new WorldCreator("Manhunt_nether");
            wc_n.environment(World.Environment.NETHER);
            wc_n.type(WorldType.NORMAL);
            wc_n.createWorld();
            WorldCreator wc_e = new WorldCreator("Manhunt_end");
            wc_e.environment(World.Environment.THE_END);
            wc_e.type(WorldType.NORMAL);
            wc_e.createWorld();
        } else return;
    }

    public void teleportPlayers() {
        if(Manhunt.genWorld) {
            setupPlayers("Manhunt");
        } else {
            setupPlayers("world");
        }

        for(Player p : Manhunt.spectators) {
            p.teleport(Manhunt.runner.getLocation());
        }
    }

    public void countDown() {
        new Countdown(10, Manhunt.getInstance()) {
            @Override
            public void count(int current) {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§eGame starting in:§c " + current));
                    //p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eGame starting in:&c " + current));
                }
                if(current == 0) {
                    Manhunt.preStart = false;
                    Manhunt.playing = true;
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§eGame starting in:§c " + current));
                        //p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&eGame &astarted&e!"));
                        p.setWalkSpeed(0.2F);
                        for(PotionEffect effect : p.getActivePotionEffects()) {
                            p.removePotionEffect(effect.getType());
                        }
                    }
                }
            }
        }.start();
    }

    private void setupPlayers(String worldName) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(!p.equals(Manhunt.runner) && !Manhunt.spectators.contains(p)) {
                Manhunt.gamers.add(p);
            }
        }

        Block locSpeedrun = Bukkit.getWorld(worldName).getHighestBlockAt(0, 0);
        //Manhunt.runner.setBedSpawnLocation(Manhunt.runner.getLocation());
        Manhunt.runner.teleport(locSpeedrun.getLocation());
        Manhunt.runner.setGameMode(GameMode.SURVIVAL);
        Manhunt.runner.setHealth(20f);
        Manhunt.runner.setFoodLevel(20);
        Manhunt.runner.getInventory().clear();

        double angleCount = 6.28319/Manhunt.gamers.size();
        //System.out.println(ChatColor.translateAlternateColorCodes('&', "&a" + angleCount));
        double angle = 0;
        int radius = 5;

        for(Player p : Manhunt.gamers) {
            double xLoc = Math.cos(angle)*radius;
            //System.out.println(ChatColor.translateAlternateColorCodes('&', "&aX: " + xLoc));
            double zLoc = Math.sin(angle)*radius;
            //System.out.println(ChatColor.translateAlternateColorCodes('&', "&aZ: " + zLoc));
            Block loc = Bukkit.getWorld(worldName).getHighestBlockAt((int)(xLoc), (int)(zLoc));
            p.teleport(loc.getLocation());
            angle += angleCount;
            p.setHealth(20f);
            p.setGameMode(GameMode.SURVIVAL);
            p.setFoodLevel(20);
            p.setBedSpawnLocation(p.getLocation());
            p.getInventory().clear();

            ItemStack compass = new ItemStack(Material.COMPASS);
            ItemMeta compassMeta = compass.getItemMeta();
            compassMeta.setDisplayName(ChatColor.GREEN + "Tracker" + ChatColor.BLACK + " | " + ChatColor.GOLD + Manhunt.runner.getName());
            compass.setItemMeta(compassMeta);
            p.getInventory().addItem(compass);
        }
    }
}
