package com.vhbob.oitc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {

	public Main main;

	public Events(Main m) {
		main = m;
	}

	@EventHandler
	public void onClickSign(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (e.getClickedBlock().getType().toString().contains("SIGN")) {
				Sign s = (Sign) e.getClickedBlock().getState();
				if (s.getLine(1).toLowerCase().contains("arena")) {
					String arenaName = ChatColor.stripColor(s.getLine(1).replace("Arena ", ""));
					boolean found = false;
					Arena arena = null;
					Player p = e.getPlayer();
					for (Arena looking : main.arenas) {
						if (looking.getName().equals(arenaName)) {
							found = true;
							arena = looking;
							break;
						}
					}
					if (found && arena != null) {
						if (main.getConfig().contains("rooms." + arena.getName())
								&& main.getConfig().contains("arenas." + arena.getName() + ".spawns.red")) {
							if (!arena.isRunning()) {
								if (arena.getPlayers().size() < main.getConfig().getInt("max-players")) {
									arena.addPlayer(p);
									if (arena.getPlayers().size() >= main.getConfig().getInt("min-players")
											&& !arena.isStarting()) {
										arena.startGame();
										Bukkit.getScheduler().runTaskLater(main, new Runnable() {
											@Override
											public void run() {
												main.updateSigns();
											}
										}, 120);
									}
								} else {
									p.sendMessage(ChatColor.translateAlternateColorCodes('&',
											main.getConfig().getString("arena-full")));
								}
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&',
										main.getConfig().getString("already-running")));
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&',
									main.getConfig().getString("setup-error")));
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&',
								main.getConfig().getString("cant-find-arena")));
					}
				}
			}
		}
	}

	@EventHandler
	public void onKill(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Arrow && e.getEntityType() == EntityType.PLAYER) {
			Player damaged = (Player) e.getEntity();
			Arrow a = (Arrow) e.getDamager();
			if (a.getShooter() instanceof Player) {
				Player p = (Player) a.getShooter();
				for (Arena arena : main.arenas) {
					if (arena.getPlayers().contains(p)) {
						if ((arena.getBlueTeam().contains(p) && arena.getBlueTeam().contains(damaged))
								|| (arena.getRedTeam().contains(p) && arena.getRedTeam().contains(damaged))) {
							p.sendMessage(ChatColor.DARK_RED + "No friendly fire!");
							e.setCancelled(true);
							break;
						}
						int rew = main.getConfig().getInt("reward");
						main.getEconomy().depositPlayer(p, rew);
						p.sendMessage(ChatColor.GOLD + "+" + rew + " coins");
						arena.death(damaged, p);
						Bukkit.getScheduler().runTaskLater(main, new Runnable() {

							@Override
							public void run() {
								main.updateSigns();
							}
						}, 120);
						break;
					}
				}
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if (e.getBlock().getType().toString().contains("SIGN")) {
			Sign s = (Sign) e.getBlock().getState();
			for (Arena a : main.signs.keySet()) {
				if (main.signs.get(a).contains(s)) {
					if (e.getPlayer().hasPermission("oitc.break.sign") || e.getPlayer().isOp()) {
						main.signs.get(a).remove(s);
						e.getPlayer().sendMessage(ChatColor.DARK_RED + "You broke an arena sign!");
					} else {
						e.getPlayer()
								.sendMessage(ChatColor.DARK_RED + "Error: You do not have permission to break that!");
					}
				}
			}
		}
	}

	@EventHandler
	public void leaveArena(PlayerInteractEvent e) {
		if (e.getPlayer().getInventory().getItemInMainHand() != null) {
			if (e.getPlayer().getInventory().getItemInMainHand().hasItemMeta() && ChatColor
					.stripColor(e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName())
					.equalsIgnoreCase("Return to lobby")) {
				for (Arena a : main.arenas) {
					if (a.getPlayers().contains(e.getPlayer())) {
						a.removePlayer(e.getPlayer());
						e.getPlayer().sendMessage(
								ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("leave-game")));
					}
				}
				main.updateSigns();
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		for (Arena a : main.arenas) {
			if (a.getPlayers().contains(p)) {
				a.death(p, p);
			}
		}
	}

	@EventHandler
	public void onDc(PlayerQuitEvent e) {
		for (Arena a : main.arenas) {
			if (a.getPlayers().contains(e.getPlayer())) {
				a.removePlayer(e.getPlayer());
			}
		}
	}

}
