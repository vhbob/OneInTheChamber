package com.vhbob.oitc;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Arena {

	private ArrayList<Player> players, redPlayers, bluePlayers, redAliveList, blueAliveList;
	private HashMap<Player, ItemStack[]> invs;
	private HashMap<Player, Integer> kills;
	private int redScore, blueScore, round, redAlive, blueAlive;
	private String arena;
	private Main main;
	private boolean running, starting;

	public Arena(String arena, Main m) {
		this.arena = arena;
		main = m;
		redScore = 0;
		running = false;
		starting = false;
		blueScore = 0;
		round = 0;
		kills = new HashMap<Player, Integer>();
		invs = new HashMap<Player, ItemStack[]>();
		players = new ArrayList<Player>();
		bluePlayers = new ArrayList<Player>();
		redPlayers = new ArrayList<Player>();
		redAliveList = new ArrayList<Player>();
		blueAliveList = new ArrayList<Player>();
	}

	public String getName() {
		return arena;
	}

	public void addPlayer(Player p) {
		if (!players.contains(p)) {
			invs.put(p, p.getInventory().getContents());
			p.getInventory().clear();
			players.add(p);
			kills.put(p, 0);
			Location loc = (Location) main.getConfig().get("rooms." + arena);
			p.teleport(loc);
			for (Player player : players) {
				player.sendMessage(org.bukkit.ChatColor.GREEN + p.getDisplayName() + " has joined the game!");
				player.sendMessage(org.bukkit.ChatColor.GREEN + "Players: " + players.size() + "/"
						+ main.getConfig().getInt("max-players"));
				if (players.size() < main.getConfig().getInt("min-players")) {
					player.sendMessage(org.bukkit.ChatColor.GREEN + "" + main.getConfig().getInt("min-players")
							+ " Players are needed to start the game.");
				}
			}
			main.updateSigns();
			Bukkit.getScheduler().runTaskLater(main, new Runnable() {

				@Override
				public void run() {
					ItemStack leave = new ItemStack(Material.COMPASS);
					ItemMeta leaveMeta = leave.getItemMeta();
					leaveMeta.setDisplayName(
							org.bukkit.ChatColor.GOLD + "" + org.bukkit.ChatColor.BOLD + "Return to lobby");
					leave.setItemMeta(leaveMeta);
					p.getInventory().setItem(4, leave);
				}
			}, 1);
		}
	}

	public void removePlayer(Player p) {
		p.getInventory().clear();
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		if (players.contains(p)) {
			if (blueAliveList.contains(p)) {
				blueAlive--;
			}
			players.remove(p);
		}
		if (bluePlayers.contains(p)) {
			bluePlayers.remove(p);
		} else if (redPlayers.contains(p)) {
			if (redAliveList.contains(p)) {
				redAlive--;
			}
			redPlayers.remove(p);
		}
		if (main.getConfig().contains("lobby")) {
			Location loc = (Location) main.getConfig().get("lobby");
			p.teleport(loc);
		} else {
			p.sendMessage(
					org.bukkit.ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("setup-error")));
		}
		if (invs.containsKey(p)) {
			p.getInventory().setContents(invs.get(p));
			invs.remove(p);
		}
		if (kills.containsKey(p)) {
			kills.remove(p);
		}
		p.setGameMode(GameMode.valueOf(main.getConfig().getString("lobby-gamemode")));
		checkPlayerNumber();
		main.updateSigns();
	}

	public void setRunning(boolean b) {
		running = b;
	}

	public void startGame() {
		redScore = 0;
		blueScore = 0;
		round = 0;
		starting = true;
		for (Player p : players) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("game-start")));
			Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				@Override
				public void run() {
					p.sendMessage(org.bukkit.ChatColor.DARK_GREEN + "" + org.bukkit.ChatColor.BOLD + "5");
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 100, 0.529732f);
				}
			}, 20);
			Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				@Override
				public void run() {
					p.sendMessage(org.bukkit.ChatColor.DARK_GREEN + "" + org.bukkit.ChatColor.BOLD + "4");
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 100, 0.594604f);
				}
			}, 40);
			Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				@Override
				public void run() {
					p.sendMessage(org.bukkit.ChatColor.DARK_GREEN + "" + org.bukkit.ChatColor.BOLD + "3!");
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 100, 0.667420f);
				}
			}, 60);
			Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				@Override
				public void run() {
					p.sendMessage(org.bukkit.ChatColor.DARK_GREEN + "" + org.bukkit.ChatColor.BOLD + "2!!");
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 100, 0.707107f);
				}
			}, 80);
			Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				@Override
				public void run() {
					p.sendMessage(org.bukkit.ChatColor.DARK_GREEN + "" + org.bukkit.ChatColor.BOLD + "1!!!");
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 100, 0.890899f);
				}
			}, 100);
		}
		Bukkit.getScheduler().runTaskLater(main, new Runnable() {

			@Override
			public void run() {
				startRound();
				main.updateSigns();
			}

		}, 120);
	}

	public ArrayList<Player> getBlueTeam() {
		return bluePlayers;
	}

	public ArrayList<Player> getRedTeam() {
		return redPlayers;
	}

	public boolean isStarting() {
		return starting;
	}

	public boolean isRunning() {
		return running;
	}

	public void startRound() {
		running = true;
		round++;

		if (round == 1) {
			for (Player p : players) {
				if (redPlayers.size() > bluePlayers.size()) {
					bluePlayers.add(p);
					p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
							main.getConfig().getString("placed-on-blue")));
				} else {
					redPlayers.add(p);
					p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
							main.getConfig().getString("placed-on-red")));
				}
			}
		}

		blueAliveList = bluePlayers;
		redAliveList = redPlayers;
		blueAlive = bluePlayers.size();
		redAlive = redPlayers.size();

		updateScores();

		ItemStack redHelm = new ItemStack(Material.LEATHER_HELMET);
		LeatherArmorMeta redHelmm = (LeatherArmorMeta) redHelm.getItemMeta();
		redHelmm.setColor(Color.RED);
		redHelm.setItemMeta(redHelmm);
		ItemStack redBoots = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta redBootsm = (LeatherArmorMeta) redBoots.getItemMeta();
		redBootsm.setColor(Color.RED);
		redBoots.setItemMeta(redBootsm);
		ItemStack redChest = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta redChestm = (LeatherArmorMeta) redChest.getItemMeta();
		redChestm.setColor(Color.RED);
		redChest.setItemMeta(redChestm);
		ItemStack redLegs = new ItemStack(Material.LEATHER_LEGGINGS);
		LeatherArmorMeta redLegsm = (LeatherArmorMeta) redLegs.getItemMeta();
		redLegsm.setColor(Color.RED);
		redLegs.setItemMeta(redLegsm);

		ItemStack blueHelm = new ItemStack(Material.LEATHER_HELMET);
		LeatherArmorMeta blueHelmm = (LeatherArmorMeta) blueHelm.getItemMeta();
		blueHelmm.setColor(Color.BLUE);
		blueHelm.setItemMeta(blueHelmm);
		ItemStack blueBoots = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta blueBootsm = (LeatherArmorMeta) blueBoots.getItemMeta();
		blueBootsm.setColor(Color.BLUE);
		blueBoots.setItemMeta(blueBootsm);
		ItemStack blueChest = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta blueChestm = (LeatherArmorMeta) blueChest.getItemMeta();
		blueChestm.setColor(Color.BLUE);
		blueChest.setItemMeta(blueChestm);
		ItemStack blueLegs = new ItemStack(Material.LEATHER_LEGGINGS);
		LeatherArmorMeta blueLegsm = (LeatherArmorMeta) blueLegs.getItemMeta();
		blueLegsm.setColor(Color.BLUE);
		blueLegs.setItemMeta(blueLegsm);

		ItemStack blueFiller = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
		ItemMeta blueFillerMeta = blueFiller.getItemMeta();
		blueFillerMeta.setDisplayName(org.bukkit.ChatColor.BLUE + "You are on the BLUE team");
		blueFiller.setItemMeta(blueFillerMeta);

		ItemStack redFiller = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta redFillerMeta = redFiller.getItemMeta();
		redFillerMeta.setDisplayName(org.bukkit.ChatColor.RED + "You are on the RED team");
		redFiller.setItemMeta(redFillerMeta);

		// gear players
		for (Player p : players) {
			p.setGameMode(GameMode.valueOf(main.getConfig().getString("game-gamemode")));
			p.getInventory().clear();
			ItemStack bow = new ItemStack(Material.BOW);
			bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
			p.getInventory().addItem(bow);
			p.getInventory().addItem(new ItemStack(Material.ARROW));
			if (redPlayers.contains(p)) {
				p.getInventory().setHelmet(redHelm);
				p.getInventory().setBoots(redBoots);
				p.getInventory().setChestplate(redChest);
				p.getInventory().setLeggings(redLegs);
				for (int i = 0; i < p.getInventory().getSize(); i++) {
					if (p.getInventory().getItem(i) == null
							|| p.getInventory().getItem(i).getType().equals(Material.AIR)) {
						p.getInventory().setItem(i, redFiller);
					}
				}
			} else {
				p.getInventory().setHelmet(blueHelm);
				p.getInventory().setBoots(blueBoots);
				p.getInventory().setChestplate(blueChest);
				p.getInventory().setLeggings(blueLegs);
				for (int i = 0; i < p.getInventory().getSize(); i++) {
					if (p.getInventory().getItem(i) == null
							|| p.getInventory().getItem(i).getType().equals(Material.AIR)) {
						p.getInventory().setItem(i, blueFiller);
					}
				}
			}
			p.getInventory().setItemInOffHand(new ItemStack(Material.AIR, 0));
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
					new ComponentBuilder(ChatColor.AQUA + "" + ChatColor.BOLD + "Round " + round + " has started!")
							.create());
		}

		// spawn teams
		if (main.getConfig().contains("arenas." + arena + ".spawns.red")
				&& main.getConfig().contains("arenas." + arena + ".spawns.blue")) {
			Location blueSpawn = (Location) main.getConfig().get("arenas." + arena + ".spawns.blue");
			Location redSpawn = (Location) main.getConfig().get("arenas." + arena + ".spawns.red");
			for (Player p : players) {
				if (bluePlayers.contains(p)) {
					p.teleport(blueSpawn);
					p.setHealth(20);
					p.setFoodLevel(20);
				} else if (redPlayers.contains(p)) {
					p.teleport(redSpawn);
					p.setHealth(20);
					p.setFoodLevel(20);
				}
			}
		} else {
			for (Player p : players) {

				running = false;
				starting = false;
				removePlayer(p);

				p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
						main.getConfig().getString("setup-error")));
			}
		}
		// make sure enought players exist
		checkPlayerNumber();
	}

	public void scoreRed() {
		redScore++;
	}

	public void scoreBlue() {
		blueScore++;
	}

	public int getRedScore() {
		return redScore;
	}

	public int getBlueScore() {
		return blueScore;
	}

	public void death(Player killed, Player killer) {
		killed.getInventory().clear();
		killed.setGameMode(GameMode.SPECTATOR);
		if (killer != killed) {
			killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 100);
			for (Player p : players) {
				if (bluePlayers.contains(killed)) {
					p.sendMessage(org.bukkit.ChatColor.BLUE + killed.getDisplayName() + org.bukkit.ChatColor.RESET
							+ " was slain by " + org.bukkit.ChatColor.RED + killer.getName());
				} else {
					p.sendMessage(org.bukkit.ChatColor.RED + killed.getDisplayName() + org.bukkit.ChatColor.RESET
							+ " was slain by " + org.bukkit.ChatColor.BLUE + killer.getName());
				}

			}
			kills.put(killer, kills.get(killer) + 1);
			updateScores();
		} else {
			for (Player p : players) {
				p.sendMessage(org.bukkit.ChatColor.RED + killed.getName() + " died");
			}
		}

		if (bluePlayers.contains(killed)) {
			blueAlive--;
		} else {
			redAlive--;
		}

		if (blueAlive <= 0 || redAlive <= 0) {
			if (bluePlayers.contains(killed) && redAlive > 0) {
				scoreRed();
			} else if (blueAlive > 0 && redPlayers.contains(killed)) {
				scoreBlue();
			}
			if (redScore < ((main.getConfig().getInt("rounds") / 2) + 1)
					&& blueScore < ((main.getConfig().getInt("rounds") / 2) + 1)) {
				for (Player p : players) {
					if (bluePlayers.contains(killed)) {
						p.sendTitle(
								org.bukkit.ChatColor.translateAlternateColorCodes('&',
										main.getConfig().getString("red-score")),
								org.bukkit.ChatColor.RED + "" + redScore + org.bukkit.ChatColor.RESET + " to "
										+ org.bukkit.ChatColor.BLUE + blueScore,
								10, 70, 20);
					} else {
						p.sendTitle(
								org.bukkit.ChatColor.translateAlternateColorCodes('&',
										main.getConfig().getString("blue-score")),
								org.bukkit.ChatColor.BLUE + "" + blueScore + org.bukkit.ChatColor.RESET + " to "
										+ org.bukkit.ChatColor.RED + redScore,
								10, 70, 20);
					}
					p.sendMessage(
							ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("next-round")));
					p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100, 100);
				}
				Bukkit.getScheduler().runTaskLater(main, new Runnable() {

					@Override
					public void run() {
						startRound();
					}
				}, 100);
			} else {
				for (Player p : players) {
					if (blueScore >= ((main.getConfig().getInt("rounds") / 2) + 1) && blueAlive > 0) {
						p.sendTitle(
								org.bukkit.ChatColor.translateAlternateColorCodes('&',
										main.getConfig().getString("blue-win")),
								org.bukkit.ChatColor.BLUE + "" + blueScore + org.bukkit.ChatColor.RESET + " to "
										+ org.bukkit.ChatColor.RED + redScore,
								10, 70, 20);
					} else {
						p.sendTitle(
								org.bukkit.ChatColor.translateAlternateColorCodes('&',
										main.getConfig().getString("red-win")),
								org.bukkit.ChatColor.RED + "" + redScore + org.bukkit.ChatColor.RESET + " to "
										+ org.bukkit.ChatColor.BLUE + blueScore,
								10, 70, 20);
					}
					p.getLocation().getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
				}
				if (blueScore >= ((main.getConfig().getInt("rounds") / 2) + 1) && blueAlive > 0) {
					for (Player bp : bluePlayers) {
						main.getEconomy().depositPlayer(bp, main.getConfig().getInt("win-reward"));
						bp.sendMessage(
								org.bukkit.ChatColor.GOLD + "+" + main.getConfig().getInt("win-reward") + " coins!");
					}
				} else {
					for (Player bp : redPlayers) {
						main.getEconomy().depositPlayer(bp, main.getConfig().getInt("win-reward"));
						bp.sendMessage(
								org.bukkit.ChatColor.GOLD + "+" + main.getConfig().getInt("win-reward") + " coins!");
					}
				}
				Bukkit.getScheduler().runTaskLater(main, new Runnable() {

					@Override
					public void run() {
						resetArena();
					}

				}, 100);
			}
		}
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	@SuppressWarnings("deprecation")
	public void updateScores() {
		// get highest scores
		ArrayList<Player> topScorers = new ArrayList<Player>();
		for (int i = 100; i >= 0; i--) {
			for (Player p : kills.keySet()) {
				if (kills.get(p) >= i && !topScorers.contains(p)) {
					if (topScorers.size() < 10) {
						topScorers.add(p);
					} else {
						break;
					}
				}
			}
		}
		ScoreboardManager scoreboardManager = main.getServer().getScoreboardManager();
		Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
		Objective objective = scoreboard.registerNewObjective("game", "sidebar",
				org.bukkit.ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("leaderboard")));
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		for (Player scorer : topScorers) {
			Team pScore = scoreboard.registerNewTeam(scorer.getName());
			String score = "";
			if (bluePlayers.contains(scorer)) {
				pScore.addEntry(org.bukkit.ChatColor.BLUE + scorer.getName() + ": ");
				score = org.bukkit.ChatColor.BLUE + scorer.getName() + ": ";
			} else {
				pScore.addEntry(org.bukkit.ChatColor.RED + scorer.getName() + ": ");
				score = org.bukkit.ChatColor.RED + scorer.getName() + ": ";
			}
			pScore.setSuffix("");
			pScore.setPrefix("");
			objective.getScore(score).setScore(kills.get(scorer));
		}

		Team red = scoreboard.registerNewTeam("redTeam");
		for (Player p : redPlayers) {
			red.addPlayer(p);
		}
		Team blue = scoreboard.registerNewTeam("blueTeam");
		for (Player p : bluePlayers) {
			blue.addPlayer(p);
		}
		if (main.getConfig().getBoolean("hide-names")) {
			red.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
			blue.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
		} else {
			red.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.ALWAYS);
			blue.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.ALWAYS);
		}

		for (Player player : players) {
			player.setScoreboard(scoreboard);
		}
	}

	public void resetArena() {
		running = false;
		starting = false;
		ArrayList<Player> toRemove = new ArrayList<Player>();
		for (Player pl : players) {
			toRemove.add(pl);
		}
		for (Player player : toRemove) {
			removePlayer(player);
		}
		players.clear();
		bluePlayers.clear();
		redPlayers.clear();
		kills.clear();
		main.updateSigns();
	}

	public void checkPlayerNumber() {
		// make sure enought players exist
		if ((redPlayers.size() <= 0 && bluePlayers.size() <= 0 || players.size() <= 1) && running) {
			for (Player p : players) {
				p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
						main.getConfig().getString("low-players")));
				if (bluePlayers.size() <= 0) {
					p.sendTitle(
							org.bukkit.ChatColor.translateAlternateColorCodes('&',
									main.getConfig().getString("red-win")),
							org.bukkit.ChatColor.RED + "" + redScore + org.bukkit.ChatColor.RESET + " to "
									+ org.bukkit.ChatColor.BLUE + blueScore,
							10, 70, 20);
				} else {
					p.sendTitle(
							org.bukkit.ChatColor.translateAlternateColorCodes('&',
									main.getConfig().getString("blue-win")),
							org.bukkit.ChatColor.BLUE + "" + blueScore + org.bukkit.ChatColor.RESET + " to "
									+ org.bukkit.ChatColor.RED + redScore,
							10, 70, 20);
				}
			}

			Bukkit.getScheduler().runTaskLater(main, new Runnable() {

				@Override
				public void run() {
					resetArena();
				}
			}, 100);
		}
	}

}
