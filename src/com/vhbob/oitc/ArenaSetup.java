package com.vhbob.oitc;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaSetup implements CommandExecutor {

	public Main main;

	public ArenaSetup(Main m) {
		main = m;
	}

	public void sendAvailableCommands(Player p) {
		p.sendMessage(ChatColor.GREEN + "-=" + ChatColor.BOLD
				+ "Here are your available commands for One in the Chamber" + ChatColor.GREEN + "=-");
		if (p.isOp() || p.hasPermission("oitc.set.lobby")) {
			p.sendMessage(ChatColor.GREEN + "/chamber setLobby - sets the lobby location");
		}
		if (p.isOp() || p.hasPermission("oitc.add.spawn")) {
			p.sendMessage(ChatColor.GREEN
					+ "/chamber (addBlueSpawn / addRedSpawn) (arena name) - adds a spawn location to the given arena");
		}
		if (p.isOp() || p.hasPermission("oitc.delete.arena")) {
			p.sendMessage(ChatColor.GREEN + "/chamber deleteArena (arena name) - deletes the given arena");
		}
		if (p.isOp() || p.hasPermission("oitc.set.waitingroom")) {
			p.sendMessage(ChatColor.GREEN
					+ "/chamber set waitingRoom (arena name) - sets the waitingroom for the given arena");
		}
		if (p.isOp() || p.hasPermission("oitc.create.sign")) {
			p.sendMessage(
					ChatColor.GREEN + "/chamber createSign (arena name) - creates a sign to join the given arena");
		}
		if (p.isOp() || p.hasPermission("oitc.reload.config")) {
			p.sendMessage(ChatColor.GREEN + "/chamber reload - reloads the Bow Battles plugin");
		}
		if (p.isOp() || p.hasPermission("oitc.force.start")) {
			p.sendMessage(ChatColor.GREEN + "/chamber forceStart (arena name) forces an arena's game to start");
		}
		if (p.isOp() || p.hasPermission("oitc.force.start")) {
			p.sendMessage(ChatColor.GREEN + "/chamber forceRestart (arena name) forces an arena to restart");
		}
		p.sendMessage(ChatColor.GREEN + "/chamber leave - leave the current lobby");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
		if (cmd.getName().equalsIgnoreCase("chamber")) {
			main.reloadConfig();
			if (sender instanceof Player) {
				Player p = (Player) sender;
				if (args.length == 0) {
					sendAvailableCommands(p);
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (p.hasPermission("oitc.reload.config") || p.isOp()) {
						main.reloadConfig();
						main.saveConfig();
						main.updateSigns();
						p.sendMessage(ChatColor.GREEN + "" + "Bow Battles has been reloaded!");
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								main.getConfig().getString("no-permission")));
					}
				} else if (args[0].equalsIgnoreCase("leave")) {
					boolean found = false;
					for (Arena a : main.arenas) {
						if (a.getPlayers().contains(p)) {
							a.removePlayer(p);
							p.sendMessage(ChatColor.DARK_RED + "You have left the game");
							found = true;
							main.updateSigns();
						}
					}
					if (!found) {
						p.sendMessage(ChatColor.DARK_RED + "Error: You are not in a game!");
					}
				} else if (args[0].equalsIgnoreCase("setlobby")) {
					if (p.hasPermission("oitc.set.lobby") || p.isOp()) {
						main.getConfig().set("lobby", p.getLocation());
						main.saveConfig();
						p.sendMessage(
								ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lobby-set")));
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								main.getConfig().getString("no-permission")));
					}
				} else if (args[0].equalsIgnoreCase("forceStart") && args.length == 2) {
					if (main.getArena(args[1]) != null) {
						if (p.hasPermission("oitc.force.start") || p.isOp()) {
							if (!main.getArena(args[1]).isStarting()) {
								main.getArena(args[1]).startGame();
								p.sendMessage(ChatColor.GREEN + "Game is now starting in Arena " + args[1]);
							} else {
								p.sendMessage(ChatColor.DARK_RED + "That arena is already starting!");
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									main.getConfig().getString("no-permission")));
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								main.getConfig().getString("cant-find-arena")));
					}
				} else if (args[0].equalsIgnoreCase("forceRestart") && args.length == 2) {
					if (main.getArena(args[1]) != null) {
						if (p.hasPermission("oitc.force.restart") || p.isOp()) {
							main.getArena(args[1]).resetArena();
							p.sendMessage(ChatColor.GREEN + "Now restarting Arena " + args[1]);
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								main.getConfig().getString("cant-find-arena")));
					}
				} else if (args[0].equalsIgnoreCase("set") && args.length == 3) {
					if (p.isOp() || p.hasPermission("oitc.set.waitingroom")) {
						if (main.getConfig().contains("arenas." + args[2] + ".spawns.red")) {
							main.getConfig().set("rooms." + args[2], p.getLocation());
							main.saveConfig();
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									main.getConfig().getString("waiting-room-set")));
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									main.getConfig().getString("cant-find-arena")));
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								main.getConfig().getString("no-permission")));
					}
				} else if (args[0].equalsIgnoreCase("createSign") && args.length == 2) {
					if (p.isOp() || p.hasPermission("oitc.create.sign")) {
						if (main.getConfig().contains("arenas." + args[1] + ".spawns.red")) {
							Block b = p.getTargetBlockExact(7);
							if (!b.getType().equals(Material.AIR)) {
								if (b.getRelative(0, -1, 0).getType().equals(Material.AIR)) {
									b.setType(Material.OAK_WALL_SIGN);
									org.bukkit.block.data.type.WallSign s = (org.bukkit.block.data.type.WallSign) b
											.getBlockData();
									s.setFacing(p.getFacing().getOppositeFace());
									b.setBlockData(s);
								} else {
									b.setType(Material.OAK_SIGN);
									org.bukkit.block.data.type.Sign s = (org.bukkit.block.data.type.Sign) b
											.getBlockData();

									s.setRotation(p.getFacing().getOppositeFace());
									b.setBlockData(s);
								}
								Sign si = (Sign) b.getState();
								si.setLine(0, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Click to join!");
								si.setLine(1, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Arena " + args[1]);
								si.setLine(2, ChatColor.GOLD + "" + ChatColor.BOLD + "Game in lobby");
								si.setLine(3,
										ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Players: "
												+ main.getArena(args[1]).getPlayers().size() + "/"
												+ main.getConfig().getInt("max-players"));
								si.update();
								if (main.signs.keySet().contains(main.getArena(args[1]))) {
									main.signs.get(main.getArena(args[1])).add(si);
								} else {
									ArrayList<Sign> signs = new ArrayList<Sign>();
									signs.add(si);
									main.signs.put(main.getArena(args[1]), signs);
								}
								p.sendMessage(ChatColor.translateAlternateColorCodes('&',
										main.getConfig().getString("sign-created")));
							} else {
								p.sendMessage(ChatColor.DARK_RED
										+ "Error: You must target a block within 7 blocks of your location");
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									main.getConfig().getString("cant-find-arena")));
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								main.getConfig().getString("no-permission")));
					}
				} else if (args[0].equalsIgnoreCase("deleteArena") && args.length == 2) {
					if (p.hasPermission("oitc.delete.arena") || p.isOp()) {
						if (main.getConfig().contains("arenas." + args[1] + ".spawns.red")) {
							main.getConfig().set("signs", null);
							main.getConfig().set("arenas." + args[1], null);
							main.getConfig().set("rooms." + args[1], null);
							main.saveConfig();
							p.sendMessage(ChatColor.GREEN + "Arena " + args[1] + " has been " + ChatColor.DARK_RED
									+ "deleted");
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									main.getConfig().getString("cant-find-arena")));
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								main.getConfig().getString("no-permission")));
					}
				} else if (args[0].equalsIgnoreCase("addBlueSpawn") && args.length == 2) {
					if (p.hasPermission("oitc.add.spawn") || p.isOp()) {
						Arena a = new Arena(args[1], main);
						if (main.getArena(a.getName()) == null) {
							main.addArena(a);
						}
						main.getConfig().set("arenas." + args[1] + ".spawns.blue", p.getLocation());
						main.saveConfig();
						p.sendMessage(ChatColor.GREEN + "Arena " + args[1] + " spawn set for team " + ChatColor.BLUE
								+ "blue");
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								main.getConfig().getString("no-permission")));
					}
				} else if (args[0].equalsIgnoreCase("addRedSpawn") && args.length == 2) {
					if (p.hasPermission("oitc.add.spawn") || p.isOp()) {
						Arena a = new Arena(args[1], main);
						if (main.getArena(a.getName()) == null) {
							main.addArena(a);
						}
						main.getConfig().set("arenas." + args[1] + ".spawns.red", p.getLocation());
						main.saveConfig();
						p.sendMessage(
								ChatColor.GREEN + "Arena " + args[1] + " spawn set for team " + ChatColor.RED + "red");
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								main.getConfig().getString("no-permission")));
					}
				} else {
					sendAvailableCommands(p);
				}
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "Error: You are not a player!");
			}
		}
		return false;
	}
}
