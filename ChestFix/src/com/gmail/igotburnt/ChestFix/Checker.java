package com.gmail.igotburnt.ChestFix;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;
import org.bukkit.util.BlockIterator;

public class Checker{
	private ChestFix plugin;
	public Checker(ChestFix plugin){
		this.plugin = plugin;
	}
	
	/**
	 * Returns true if a player can see the given block
	 * @param p The player
	 * @param b The block they are looking at
	 * @return True if they can see it, false if they're freecamming etc
	 */
	public boolean isFacing(Player p, Block b){
		return isFacing(p.getLocation(), b);
	}
	
	public boolean isFacing(Location loc, Block b){
		//Get all blocks in 5m.
		//Foreach block, if block == b, return;
		//				 if NotTransparent, cancel event, return
		//				 if block == farthest seen, cancel event, return
		
		//Stores all blocks (Nearly) seen.  Used for leniency.
		
		Block c;
		BlockIterator bIt = new BlockIterator(loc, 0, 5);
		while(bIt.hasNext()){
			c = bIt.next();
			if(c.equals(b)){
				return true; //Success
			}
			if(plugin.getTransparentBlocks().contains(c.getType())){
				continue;
			}
			else{
				//Double chests
				if(b.getType() == Material.CHEST && getChestNextTo(b) == c){		
					return true;
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

		return false;
	}
	
	/**
	 * Checks if a player can see block b if they move their camera,
	 * but do not move their character. E.g. x,y,z must stay the same, but they can
	 * look in any direction.
	 * @param p The player
	 * @param b The block to check for
	 * @return true if they can see it, false if they can't
	 */
	public boolean canSee(Player p, Block b){
		Location pLoc = p.getLocation().clone();
		Location bLoc = b.getLocation().clone();
		
		//Adjust to eye height
		pLoc.add(0, 1.62, 0);
		
		//Check they're looking at it
		if(isFacing(pLoc, b)) return true;
		
		// Check if they can see any corner of the chest. If they can, return true.
		Location corner = lookAt(pLoc, bLoc);
		if(isFacing(corner, b)) return true;
		
		bLoc = b.getLocation().clone().add(1,0,0);
		corner = lookAt(pLoc, bLoc);
		if(isFacing(corner, b)) return true;
		
		bLoc = b.getLocation().clone().add(0,1,0);
		corner = lookAt(pLoc, bLoc);
		if(isFacing(corner, b)) return true;
		
		bLoc = b.getLocation().clone().add(0,0,1);
		corner = lookAt(pLoc, bLoc);
		if(isFacing(corner, b)) return true;
		
		bLoc = b.getLocation().clone().add(1,1,0);
		corner = lookAt(pLoc, bLoc);
		if(isFacing(corner, b)) return true;
		
		bLoc = b.getLocation().clone().add(1,0,1);
		corner = lookAt(pLoc, bLoc);
		if(isFacing(corner, b)) return true;
		
		bLoc = b.getLocation().clone().add(0,1,1);
		corner = lookAt(pLoc, bLoc);
		if(isFacing(corner, b)) return true;
		
		bLoc = b.getLocation().clone().add(1,1,1);
		corner = lookAt(pLoc, bLoc);
		if(isFacing(corner, b)) return true;
		
		//Uh oh. You're not looking at it and can't see any corner of it.
		return false;
	}
	
	/**
	 * Returns loc with modified pitch/yaw angles so it faces lookat
	 * @param loc The location a players head is
	 * @param lookat The location they should be looking
	 * @return The location the player should be facing to have their crosshairs on the location lookAt
	 * Kudos to bergerkiller for most of this function
	 */
	public Location lookAt(Location loc, Location lookat) {
        //Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();

        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();

        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }

        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

        float pitch = (float) -Math.atan(dy/dxz);

        // Set values, convert to degrees
        // Minecraft yaw (vertical) angles are inverted (negative)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI + 360);
        // But pitch angles are normal
        loc.setPitch(pitch * 180f / (float) Math.PI);
        
        return loc;
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
}