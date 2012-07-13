package com.gmail.igotburnt.ChestFix;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;


import uk.co.oliwali.HawkEye.HawkEye;

public class ChestFix extends JavaPlugin {
	public Logger log;
	//Hawkeye
	private HawkEye hawkEye =  null;
	
	//Config
	private File configFile;
	private YamlConfiguration config;
	private boolean lenient = false;
	
	//Blocks
	private HashSet<Material> transparent = new HashSet<Material>(55);
	private HashSet<Material> interact = new HashSet<Material>(30);
	private HashSet<Material> rightClickOnly = new HashSet<Material>(30);

	//Listener
	private ContainerListener containerListener = new ContainerListener(this);
	
	public void onEnable(){
		this.log = this.getLogger();
		
		Bukkit.getServer().getPluginManager().registerEvents(containerListener, this);
		
		log.info(this.getDescription().getName() +  this.getDescription().getVersion() + " Enabled ");
		
		/* Loading Config */
		this.configFile = new File(this.getDataFolder(), "config.yml");
		log.info("Loading Config");
		
		if(this.configFile == null){
			/* First run */
			log.info("Creating config");
			this.getConfig().options().copyDefaults(true);
			this.saveConfig();
		}
		
		this.config = YamlConfiguration.loadConfiguration(this.configFile);
		this.lenient = config.getBoolean("lenient");
		
		if(config.getBoolean("hawkeye")){
			this.hawkEye = (HawkEye) Bukkit.getPluginManager().getPlugin("HawkEye");
		}
		/* Begin loading blocks */
		log.info("Loading Transparent Blocks...");
		loadTransparentBlocks();
		log.info("Loading Interactable Blocks...");
		loadInteractBlocks();
		log.info("Loading right-click-only blocks...");
		loadRightClick();
	}
	 
	public void onDisable(){ 
		log.info(this.getDescription().getName() + " disabled");
	}
	public boolean isLenient(){
		return this.lenient;
	}

	public HashSet<Material> getTransparentBlocks(){
		return this.transparent;
	}
	
	public HawkEye getHawkEye(){
		return this.hawkEye;
	}
	public void loadRightClick(){
		this.rightClickOnly.clear();
		this.rightClickOnly.add(Material.ENCHANTMENT_TABLE);
		this.rightClickOnly.add(Material.WORKBENCH);
		this.rightClickOnly.add(Material.CHEST);
		this.rightClickOnly.add(Material.FURNACE);
		this.rightClickOnly.add(Material.DISPENSER);
		this.rightClickOnly.add(Material.JUKEBOX);
	}
	public HashSet<Material> getRightClickOnly(){
		return this.rightClickOnly;
	}
	public void loadTransparentBlocks(){
		this.transparent.clear();
		//ToDo: add extras to config file
		this.addTransparentBlock(Material.AIR);
		/* Misc */
		this.addTransparentBlock(Material.CAKE_BLOCK);
		
		/* Redstone Material */
		this.addTransparentBlock(Material.REDSTONE);
		this.addTransparentBlock(Material.REDSTONE_WIRE);
		
		/* Redstone Torches */
		this.addTransparentBlock(Material.REDSTONE_TORCH_OFF);
		this.addTransparentBlock(Material.REDSTONE_TORCH_ON);
		
		/* Diodes (Repeaters) */
		this.addTransparentBlock(Material.DIODE_BLOCK_OFF);
		this.addTransparentBlock(Material.DIODE_BLOCK_ON);
		
		/* Power Sources */
		this.addTransparentBlock(Material.DETECTOR_RAIL);
		this.addTransparentBlock(Material.LEVER);
		this.addTransparentBlock(Material.STONE_BUTTON);
		this.addTransparentBlock(Material.STONE_PLATE);
		this.addTransparentBlock(Material.WOOD_PLATE);
		
		/* Nature Material */
		this.addTransparentBlock(Material.RED_MUSHROOM);
		this.addTransparentBlock(Material.BROWN_MUSHROOM);
		
		this.addTransparentBlock(Material.RED_ROSE);
		this.addTransparentBlock(Material.YELLOW_FLOWER);

		/* Greens */
		this.addTransparentBlock(Material.LONG_GRASS);
		this.addTransparentBlock(Material.VINE);
		this.addTransparentBlock(Material.WATER_LILY);

		/* Seedy things */
		this.addTransparentBlock(Material.MELON_STEM);
		this.addTransparentBlock(Material.PUMPKIN_STEM);
		this.addTransparentBlock(Material.CROPS);
		this.addTransparentBlock(Material.NETHER_WARTS);
		
		/* Semi-nature */
		this.addTransparentBlock(Material.SNOW);
		this.addTransparentBlock(Material.FIRE);
		this.addTransparentBlock(Material.WEB);
		
		/* Lava & Water */
		this.addTransparentBlock(Material.LAVA);
		this.addTransparentBlock(Material.STATIONARY_LAVA);
		this.addTransparentBlock(Material.WATER);
		this.addTransparentBlock(Material.STATIONARY_WATER);
		
		/* Saplings and bushes */
		this.addTransparentBlock(Material.SAPLING);
		this.addTransparentBlock(Material.DEAD_BUSH);
		
		/* Construction Material */
		/* Fences */
		this.addTransparentBlock(Material.FENCE);
		this.addTransparentBlock(Material.FENCE_GATE);
		this.addTransparentBlock(Material.IRON_FENCE);
		this.addTransparentBlock(Material.NETHER_FENCE);
		
		/* Ladders, Signs */
		this.addTransparentBlock(Material.LADDER);
		this.addTransparentBlock(Material.SIGN);
		this.addTransparentBlock(Material.SIGN_POST);
		this.addTransparentBlock(Material.WALL_SIGN);
		
		/* Bed */
		this.addTransparentBlock(Material.BED_BLOCK);
		this.addTransparentBlock(Material.BED);
		
		/* Pistons */
		this.addTransparentBlock(Material.PISTON_EXTENSION);
		this.addTransparentBlock(Material.PISTON_MOVING_PIECE);
		this.addTransparentBlock(Material.RAILS);
		
		/* Torch & Trapdoor */		
		this.addTransparentBlock(Material.TORCH);
		this.addTransparentBlock(Material.TRAP_DOOR);
		
		List<Integer> confIds = config.getIntegerList("transparent");
		for(int i = 0; i < confIds.size(); i++){
			this.addTransparentBlock(Material.getMaterial(confIds.get(i)));
		}
	}
	
	public void loadInteractBlocks(){
		this.interact.clear();
		this.addInteractBlock(Material.CHEST);
		this.addInteractBlock(Material.FURNACE);
		this.addInteractBlock(Material.BREWING_STAND);
		this.addInteractBlock(Material.DISPENSER);
		this.addInteractBlock(Material.BURNING_FURNACE);
		this.addInteractBlock(Material.JUKEBOX);
		
		List<Integer> confIds = config.getIntegerList("interact");
		for(int i = 0; i < confIds.size(); i++){
			this.addInteractBlock(Material.getMaterial(confIds.get(i)));
		}
	}
	public void addTransparentBlock(Material mat){
		this.transparent.add(mat);
	}
	public void addInteractBlock(Material mat){
		this.getInteractBlocks().add(mat);
	}
	public HashSet<Material> getInteractBlocks(){
		return this.interact;
	}
}
