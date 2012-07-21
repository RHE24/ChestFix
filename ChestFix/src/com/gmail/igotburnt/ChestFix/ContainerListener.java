package com.gmail.igotburnt.ChestFix;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Door;
import org.bukkit.util.BlockIterator;
import uk.co.oliwali.HawkEye.util.HawkEyeAPI;

public class ContainerListener implements Listener{
	ChestFix plugin;
	
	public ContainerListener(ChestFix plugin){
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockInteract(PlayerInteractEvent e){
		if(e.isCancelled() || (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)) return;
		if(e.getPlayer().hasPermission("chestfix.bypass")) return;
		Block b = e.getClickedBlock();
		
		//If the block they used was a container E.g. chest, and they left clicked it, it doesn't matter
		//So return.
		if(e.getAction() == Action.LEFT_CLICK_BLOCK && plugin.getRightClickOnly().contains(b.getType())){
			return;
		}
		
		if(plugin.getInteractBlocks().contains(b.getType())){
			Player p = e.getPlayer();
			//Get all blocks in 5m.
			//Foreach block, if block == b, return;
			//				 if NotTransparent, cancel event, return
			//				 if block == farthest seen, cancel event, return
			
			//Stores all blocks (Nearly) seen.  Used for leniency.
			HashSet<Block> nearly = new HashSet<Block>(30);
			
			Block c = null;
			BlockIterator bIt = new BlockIterator((LivingEntity) p, 5);
			while(bIt.hasNext()){
				c = bIt.next();
				if(c.equals(b)){
					return; //Success
				}
				if(plugin.getTransparentBlocks().contains(c.getType())){
					if(plugin.isLenient()){
						/*
						 * 		L
						 * 	  L B L 
						 * 		L
						 * Where B = Main block
						 * 		 L = Lenient block.
						 * (Add a block above, below, left, right, behind, infront)
						 */
						nearly.add(c.getRelative(1, 0, 0));
						nearly.add(c.getRelative(-1, 0, 0));
						nearly.add(c.getRelative(0, 1, 0));
						nearly.add(c.getRelative(0, -1, 0));
						nearly.add(c.getRelative(0, 0, 1));
						nearly.add(c.getRelative(0, 0, -1));
					}
					continue;
				}
				else{
					//Double chests
					if(b.getState() instanceof Chest && getChestNextTo(b) == c){		
						return;
					}
					//Open doors
					if(c.getState().getData() instanceof Door){
						Door d = (Door) c.getState().getData();
						if(d.isTopHalf()) d = (Door) c.getRelative(0, -1, 0).getState().getData();
						if(d.isOpen()){
							continue;
						}
					}
					//Invalid block
					//Breaking destroys the loop.
					break;
				}
			}
			if(plugin.isLenient()){
				if(nearly.contains(b)){
					return;
				}
			}
			/* They:
			 * - May be using a chest from too far away
			 * - May be looking at a solid block or closed door
			 * - May be looking into the air and freecamming somewhere
			 * If lenient, they're far off. If not lenient, they could be closer.
			 */
			this.sendError(p, b, c);
			e.setCancelled(true);
			return;
		}
	}
	/**
	 * Gets the chest (or null) directly next to a block. Does not check vertical or diagonal.
	 * @param b The block to check next to.
	 * @return The chest.
	 */
	private Block getChestNextTo(Block b){
		Block[] c = new Block[4];
		c[0] = (b.getLocation().add(1, 0, 0).getBlock());
		c[1] = (b.getLocation().add(-1, 0, 0).getBlock());
		c[2] = (b.getLocation().add(0, 0, 1).getBlock());
		c[3] = (b.getLocation().add(0, 0, -1).getBlock());
		
		for(Block d : c){
			if(d.getType() == Material.CHEST){
				return d;
			}
		}
		return null;
	}
	/**
	 * Sends an error message to a player stating they freecammed.
	 * @param p The player who caused the event
	 * @param b The block they tried to use
	 * @param c The block they were looking at
	 */
	private void sendError(Player p, Block b, Block c){
		if(plugin.getConfig().isBoolean("message")){
			p.sendMessage(ChatColor.RED + "[ChestFix] " + ChatColor.YELLOW + "You used a "+b.getType().toString()+" but were looking at " + c.getType().toString() + ".");
		}
		if(plugin.getHawkEye() != null){
			HawkEyeAPI.addCustomEntry(plugin, "Freecammed through " + c.getType().toString()+". ", p, b.getLocation(), b.getType().toString());
		}
		if(plugin.getConfig().getBoolean("log.server-log")){
			plugin.log.info(p.getName() + " freecammed through " + c.getType().toString()+".");
		}
		if(plugin.getConfig().getBoolean("notify-mods")){
			for(Player player : Bukkit.getOnlinePlayers()){
				if(player != p && player.hasPermission("chestfix.notify")){
					player.sendMessage(ChatColor.RED + "[ChestFix] " + ChatColor.YELLOW + p.getName() + " used a " + b.getType()+ " they couldn't see.  This might be lag or a hack.");
				}
			}
		}
	}
}