package com.gmail.igotburnt.ChestFix;

import java.util.ArrayList;
import java.util.List;

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
		Block b = e.getClickedBlock();
		
		
		if(plugin.getInteractBlocks().contains(b.getType())){
			Player p = e.getPlayer();
			
			//If the block they used was a container E.g. chest, and they left clicked it, it doesn't matter
			//So return.
			if(e.getAction() == Action.LEFT_CLICK_BLOCK && plugin.getRightClickOnly().contains(b.getType())){
				return;
			}
			
			//Get all blocks in 5m.
			//Foreach block, if block == b, return;
			//				 if NotTransparent, cancel event, return
			//				 if block == farthest seen, cancel event, return
					
			List<Block> seen = new ArrayList<Block>(500);
			
			BlockIterator bIt = new BlockIterator((LivingEntity) p, 5);
			while(bIt.hasNext()){
				seen.add(bIt.next());
			} 
			
			for(Block c : seen){
				if(c.equals(b)){
					//This is the block they're looking at.
					return;
				}
				
				if(!plugin.getTransparentBlocks().contains((byte) c.getTypeId())){
					//The block is not transparent
					if(b.getState() instanceof Chest && getChestNextTo(b) == c){		
						return; //The block they're looking at is the chest next to the block they used. AKA, they have access anyway. (Double Chest case)
					}
					
					if(c.getState().getData() instanceof Door){
						Door d = (Door) c.getState().getData();
						if(d.isTopHalf()) d = (Door) c.getRelative(0, -1, 0).getState().getData();
						if(d.isOpen()){
							continue;
						}
					}

					this.sendError(p, b, c);
					e.setCancelled(true);
					return;
				}  
				if(c == seen.get(seen.size() - 1)){
					this.sendError(p, b, c);
					e.setCancelled(true);
					return;
				}
				
				if(plugin.isLenient()){
					for(int x = -1; x <= 1; x++){
						for(int y = -1; y <= 1; y++){
							for(int z = -1; z <= 1; z++){
								if(c.getRelative(x, y, z).equals(b)){
									return;
								}
							}
						}
					}
				}
			}
		}
	}
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
	private void sendError(Player p, Block b, Block c){
		if(plugin.getConfig().isBoolean("message")){
			p.sendMessage(ChatColor.RED + "[ChestFix] " + ChatColor.YELLOW + "You used a "+b.getType().toString()+" but were looking at " + c.getType().toString() + ".");
		}
		if(plugin.getHawkEye() != null){
			HawkEyeAPI.addCustomEntry(plugin, "Freecammed through " + c.getType().toString()+". ", p, b.getLocation(), b.getType().toString());
		}
	}
}