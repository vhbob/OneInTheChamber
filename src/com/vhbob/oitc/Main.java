package com.vhbob.oitc;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	public Main main;
	public ArrayList<Arena> arenas;
	private Economy econ;
	public HashMap<Arena, ArrayList<Sign>> signs;

	@Override
	public void onEnable() {
		if (!setupEconomy()) {
			this.getLogger().severe("Disabled due to no Vault dependency found!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		main = this;
		saveDefaultConfig();
		saveConfig();
		arenas = new ArrayList<Arena>();
		getCommand("chamber").setExecutor(new ArenaSetup(this));
		Bukkit.getPluginManager().registerEvents(new Events(this), this);
		signs = new HashMap<Arena, ArrayList<Sign>>();
		if (getConfig().contains("arenas")) {
			for (String name : getConfig().getConfigurationSection("arenas").getKeys(false)) {
				Arena a = new Arena(name, this);
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Enabling arena " + name);
				arenas.add(a);
				for (int i = 1; getConfig().contains("signs." + a.getName() + "." + i + ".world"); i++) {
					World w = Bukkit.getWorld(getConfig().getString("signs." + a.getName() + "." + i + ".world"));
					int x = getConfig().getInt("signs." + a.getName() + "." + i + ".x");
					int y = getConfig().getInt("signs." + a.getName() + "." + i + ".y");
					int z = getConfig().getInt("signs." + a.getName() + "." + i + ".z");
					Location l = new Location(w, x, y, z);
					Block b = l.getBlock();
					if (b.getType().toString().contains("SIGN")) {
						Sign s = (Sign) b.getState();
						if (signs.keySet().contains(getArena(a.getName()))) {
							signs.get(getArena(a.getName())).add(s);
						} else {
							ArrayList<Sign> signsToAdd = new ArrayList<Sign>();
							signsToAdd.add(s);
							signs.put(a, signsToAdd);
						}
					}
				}
			}
		}
		updateSigns();

		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Bow Battles has been enabled!");
	}

	@Override
	public void onDisable() {

		for (Arena a : signs.keySet()) {
			int locNum = 1;
			for (Sign s : signs.get(a)) {
				getConfig().set("signs." + a.getName() + "." + locNum + ".world", s.getLocation().getWorld().getName());
				getConfig().set("signs." + a.getName() + "." + locNum + ".x", s.getLocation().getBlockX());
				getConfig().set("signs." + a.getName() + "." + locNum + ".y", s.getLocation().getBlockY());
				getConfig().set("signs." + a.getName() + "." + locNum + ".z", s.getLocation().getBlockZ());
				saveConfig();
				locNum++;
			}
		}
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Bow Battles has been disabled!");
	}

	public Arena getArena(String name) {
		for (Arena a : arenas) {
			if (a.getName().equalsIgnoreCase(name)) {
				return a;
			}
		}
		return null;
	}

	public void addArena(Arena a) {
		arenas.add(a);
	}

	private boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public Economy getEconomy() {
		return econ;
	}

	public void updateSigns() {
		for (Arena a : signs.keySet()) {
			for (Sign s : signs.get(a)) {
				s.setLine(0, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Click to join!");
				s.setLine(1, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Arena " + a.getName());
				if (!a.isRunning()) {
					s.setLine(2, ChatColor.GOLD + "" + ChatColor.BOLD + "Game in lobby");
				} else {
					s.setLine(2, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Game running!");
				}
				s.setLine(3, ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Players: " + a.getPlayers().size() + "/"
						+ main.getConfig().getInt("max-players"));
				s.update();
			}
		}
	}

}
