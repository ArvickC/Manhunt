package me.mystc.manhunt;

import me.mystc.manhunt.commands.End;
import me.mystc.manhunt.commands.GamersList;
import me.mystc.manhunt.commands.Setup;
import me.mystc.manhunt.commands.Spectator;
import me.mystc.manhunt.commands.tabcomplete.SetupTabComplete;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;

public final class Manhunt extends JavaPlugin implements Listener {
    // Var
    private static Manhunt instance;

    End end = new End();
    Setup setup = new Setup();
    SetupTabComplete setupTab = new SetupTabComplete();
    Spectator spectator = new Spectator();
    GamersList gamersList = new GamersList();

    public static Player runner;
    public static boolean preStart = false;
    public static boolean playing = false;
    public static boolean genWorld = false;
    public static Location target;

    public static ArrayList<Player> spectators = new ArrayList<>();
    public static ArrayList<Player> gamers = new ArrayList<>();

    public static World unload = null;
    public static World unload_n = null;
    public static World unload_e = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("end").setExecutor(end);
        getCommand("setup").setExecutor(setup);
        getCommand("setup").setTabCompleter(setupTab);
        getCommand("spectator").setExecutor(spectator);
        System.out.println(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7] Plugin &aActivated&7."));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        gameEnd();
        System.out.println(ChatColor.translateAlternateColorCodes('&', "&7[&aManhunt&7] Plugin &cDeactivated&7."));
    }

    // Events
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(preStart) {
            e.getPlayer().setWalkSpeed(0F);
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, -255, false, false));
        }
        if(playing) {
            target = runner.getLocation();

            for(Player p : gamers) {
                if(runner.getWorld().equals(p.getWorld())) {
                    p.setCompassTarget(target);
                } else {
                    p.setCompassTarget(p.getLocation());
                }
            }
        }
    }

    /*
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if(playing) {
            if(e.getEntity().equals(runner)) {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p == runner) {
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cHunters Win!"), null, 5, 60, 5);
                    } else {
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&aHunters Win!"), null, 5, 60, 5);
                    }
                }
                gameEnd();
            }
        }
    }
     */

    @EventHandler
    public void onDeath(EntityDamageEvent e) {
        if(!playing) return;
        if(!(e.getEntity() instanceof Player)) return;
        Player p = (Player)(e.getEntity());
        if(!Manhunt.runner.equals(p)) return;

        if ((p.getHealth() - e.getFinalDamage()) <= 0) {
            e.setCancelled(true);
            for(Player pl : Bukkit.getOnlinePlayers()) {
                if(pl == runner) {
                    pl.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cHunters Win!"), null, 5, 60, 5);
                } else {
                    pl.sendTitle(ChatColor.translateAlternateColorCodes('&', "&aHunters Win!"), null, 5, 60, 5);
                }
            }
            p.teleport(Bukkit.getWorld("world").getHighestBlockAt(0, 0).getLocation());
            gameEnd();
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if(playing) {
            if(gamers.contains(e.getPlayer())) {
                if(genWorld) {
                    e.setRespawnLocation(Bukkit.getWorld("Manhunt").getHighestBlockAt(0, 0).getLocation());
                    ItemStack compass = new ItemStack(Material.COMPASS);
                    ItemMeta compassMeta = compass.getItemMeta();
                    compassMeta.setDisplayName(ChatColor.GREEN + "Tracker" + ChatColor.BLACK + " | " + ChatColor.GOLD + Manhunt.runner.getName());
                    compass.setItemMeta(compassMeta);
                    e.getPlayer().getInventory().addItem(compass);
                } else {
                    e.setRespawnLocation(Bukkit.getWorld("world").getHighestBlockAt(0, 0).getLocation());
                }
            }
        } else {
            e.setRespawnLocation(Bukkit.getWorld("world").getHighestBlockAt(0, 0).getLocation());
        }
    }

    @EventHandler
    public void beatGame(EntityDeathEvent e) {
        //System.out.println(ChatColor.translateAlternateColorCodes('&', "&fENTITY DEATH"));
        if(e.getEntity() instanceof EnderDragon) {
            //System.out.println(ChatColor.translateAlternateColorCodes('&', "&fENDER DRAGON DEATH"));
            // GenWorld
            if(genWorld) {
                if(e.getEntity().getWorld().equals(Bukkit.getWorld("Manhunt_end"))) {
                    //System.out.println(ChatColor.translateAlternateColorCodes('&', "&fENDER DRAGON DEATH IN CORRECT WORLD"));
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        ((EnderDragon) e.getEntity()).getBossBar().setProgress(0);
                        ((EnderDragon) e.getEntity()).getBossBar().removeAll();
                        if(p == runner) {
                            p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&aRunner Wins!"), null, 5, 60, 5);
                        } else {
                            p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cRunner Wins!"), null, 5, 60, 5);
                        }
                    }
                    gameEnd();
                }
            // No GenWorld
            } else {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p == runner) {
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&aRunner Wins!"), null, 5, 60, 5);
                    } else {
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cRunner Wins!"), null, 5, 60, 5);
                    }
                }
                gameEnd();
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(preStart || playing) {
            setSpectator(e.getPlayer());
            e.getPlayer().teleport(runner.getLocation());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if(preStart || playing) {
            if(e.getPlayer().equals(runner)) {
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if(p == runner) {
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cHunters Win!"), ChatColor.translateAlternateColorCodes('&', "&7Runner Left"), 5, 60, 5);
                    } else {
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&aHunters Win!"), ChatColor.translateAlternateColorCodes('&', "&7Runner Left"), 5, 60, 5);
                    }
                }
                gameEnd();
            } else {
                if(Bukkit.getOnlinePlayers().size() > 2) {
                    // 2(or more) players online
                    System.out.println(ChatColor.translateAlternateColorCodes('&', "&aTWO OR MORE PLAYERS ONLINE"));
                    boolean isHunter = false;

                    for(Player p : Bukkit.getOnlinePlayers()) {
                        if(!spectators.contains(p) && !p.equals(runner)) {
                            isHunter = true;
                            break;
                        }
                    }
                    // Hunters are left
                    if(isHunter) return;
                    System.out.println(ChatColor.translateAlternateColorCodes('&', "&aNO HUNTERS ONLINE"));

                    // No hunters left. End game
                    for(Player pl : Bukkit.getOnlinePlayers()) {
                        pl.sendTitle(ChatColor.translateAlternateColorCodes('&', "&aRunner Wins!"), null, 5, 60, 5);
                    }
                    gameEnd();
                } else {
                    // Only runners left.
                    System.out.println(ChatColor.translateAlternateColorCodes('&', "&aONLY RUNNER LEFT"));
                    for(Player pl : Bukkit.getOnlinePlayers()) {
                        pl.sendTitle(ChatColor.translateAlternateColorCodes('&', "&aRunner Wins!"), null, 5, 60, 5);
                    }
                    gameEnd();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        if(!playing && !preStart) return;
        if(!genWorld) return;

        World fromWorld = e.getFrom().getWorld();
        switch (e.getCause()) {
            case NETHER_PORTAL:
                switch (fromWorld.getEnvironment()) {
                    case NORMAL:
                        //System.out.println(ChatColor.translateAlternateColorCodes('&', "&cGOING TO NETHER"));
                        Location newTo2 = e.getTo();
                        newTo2.setWorld(Bukkit.getWorld("Manhunt_nether"));
                        e.setTo(newTo2);
                        break;
                    case NETHER:
                        //System.out.println(ChatColor.translateAlternateColorCodes('&', "&aGOING TO OVERWORLD"));
                        Location newTo = e.getFrom().multiply(8.0D);
                        newTo.setWorld(Bukkit.getWorld("Manhunt"));
                        e.setTo(newTo);
                        break;
                    default:
                        break;
                }
                break;
            case END_PORTAL:
                //System.out.println(ChatColor.translateAlternateColorCodes('&', "&fGOING TO END"));
                e.getTo().setWorld(Bukkit.getWorld("Manhunt_end"));
                break;
            default:
                break;
        }
    }

    // Functions
    public static void gameEnd() {
        runner = null;
        preStart = false;
        playing = false;
        for(Player p : Bukkit.getOnlinePlayers()) {
            Block loc = Bukkit.getWorld("world").getHighestBlockAt(0, 0);
            p.teleport(loc.getLocation().add(new Vector(0, 1, 0)));
            p.getInventory().clear();
            p.setSaturation(20f);
            p.setGameMode(GameMode.SURVIVAL);
            p.setBedSpawnLocation(loc.getLocation().add(new Vector(0, 1, 0)));
            p.setHealth(20f);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1);
            spectators.clear();
            gamers.clear();
        }
        if(genWorld) {
            unload = Bukkit.getWorld("Manhunt");
            unload_n = Bukkit.getWorld("Manhunt_nether");
            unload_e = Bukkit.getWorld("Manhunt_end");
            File deleteFolder = unload.getWorldFolder();
            File deleteFolder_n = unload_n.getWorldFolder();
            File deleteFolder_e = unload_e.getWorldFolder();
            unloadWorld(unload);
            unloadWorld(unload_n);
            unloadWorld(unload_e);
            deleteWorld(deleteFolder);
            deleteWorld(deleteFolder_n);
            deleteWorld(deleteFolder_e);
        }
        genWorld = false;
    }

    public static void setSpectator(Player p) {
        spectators.add(p);
        p.setHealth(0);
        p.getInventory().clear();
        p.setGameMode(GameMode.SPECTATOR);
        p.setHealth(20f);
        p.setSaturation(20f);
    }

    public static void unloadWorld(World world) {
        if(!world.equals(null)) {
            Bukkit.getServer().unloadWorld(world, true);
        }
    }

    public static boolean deleteWorld(File path) {
        if(path.exists()) {
            File files[] = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteWorld(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return(path.delete());
    }

    public static Manhunt getInstance() {
        return instance;
    }
}
