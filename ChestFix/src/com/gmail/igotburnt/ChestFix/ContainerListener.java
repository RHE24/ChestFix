package com.gmail.igotburnt.ChestFix;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import uk.co.oliwali.HawkEye.util.HawkEyeAPI;

public class ContainerListener implements Listener{
	private ChestFix plugin;
	private Checker checker;
	
	public ContainerListener(ChestFix plugin){
		this.plugin = plugin;
		this.checker = new Checker(plugin);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockInteract(PlayerInteractEvent e){
		if(e.isCancelled() || (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)) return;
		if(e.getPlayer().hasPermission("chestfix.bypass")) return;
		//If the block they used was a container E.g. chest, and they left clicked it, it doesn't matter
		//So return.
		Block b = e.getClickedBlock();
		
		if(e.getAction() == Action.LEFT_CLICK_BLOCK && plugin.getRightClickOnly().contains(b.getType())){
			return;
		}
		if(plugin.getInteractBlocks().contains(b.getType())){
			if(!checker.canSee(e.getPlayer(), e.getClickedBlock())){
				sendError(e.getPlayer());
			}
		}
	}
	/**
	 * Sends an error message to a player stating they freecammed.
	 * @param p The player who caused the event
	 * @param b The block they tried to use
	 * @param c The block they were looking at
	 */
	private void sendError(Player p){
		if(plugin.getConfig().getBoolean("message")){
			p.sendMessage(ChatColor.RED + "[ChestFix] " + ChatColor.YELLOW + "You tried to use something you can't see.");
		}
		if(plugin.getHawkEye() != null){
			HawkEyeAPI.addCustomEntry(plugin, "Freecammed through something. ", p, p.getLocation(), "FREECAM");
		}
		if(plugin.getConfig().getBoolean("log.server-log")){
			plugin.log.info(p.getName() + " freecammed through something.");
		}
		if(plugin.getConfig().getBoolean("notify-mods")){
			for(Player player : Bukkit.getOnlinePlayers()){
				if(player != p && player.hasPermission("chestfix.notify")){
					player.sendMessage(ChatColor.RED + "[ChestFix] " + ChatColor.YELLOW + p.getName() + " used something they couldn't see.  This might be lag or a hack.");
				}
			}
		}
	}
}