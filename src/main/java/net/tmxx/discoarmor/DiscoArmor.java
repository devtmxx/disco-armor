/**
 * Adds some cool disco armor to minecraft servers.
 * Copyright (C) 2016  Tim Mauersberger
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.tmxx.discoarmor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A cool disco armor plugin for minecraft servers.
 *
 * @author tmxx
 * @version 1.0
 */
public class DiscoArmor extends JavaPlugin implements Listener, Runnable {
    private final Set< UUID > discoPlayers = new HashSet<>();
    private final float speed = 0.025F;
    private float hue = 1F;

    /**
     * Enables the disco armor plugin.
     */
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents( this, this );
        this.getServer().getScheduler().runTaskTimer( this, this, 0L, 1L );
    }

    /**
     * Invoked if a player uses the '/discoarmor' or '/da' command. This toggles the players disco armor status. So if
     * the player has disco armor enabled, it will disable, and otherwise enable it.
     *
     * @param sender    the sender of the command
     * @param command   the executed command
     * @param label     the exact used label
     * @param args      provided arguments
     * @return          true in any cases
     */
    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
        // check if the player is a sender and if he has the required permission
        if ( sender instanceof Player && sender.hasPermission( "discoarmor.use" ) ) {
            Player player = ( Player ) sender;
            if ( this.discoPlayers.contains( player.getUniqueId() ) ) {
                // disable disco armor
                this.discoPlayers.remove( player.getUniqueId() );
                player.sendMessage( ChatColor.GREEN + "You have successfully removed your disco armor" );
                ItemStack air = new ItemStack( Material.AIR );
                player.getInventory().setArmorContents( new ItemStack[] {
                        air, air, air, air
                } );
                player.updateInventory();
            } else {
                // enable disco armor
                this.discoPlayers.add( player.getUniqueId() );
                player.sendMessage( ChatColor.GREEN + "Look at your beautiful disco armor" );
            }
        }
        return true;
    }

    /**
     * Listens for the {@link PlayerQuitEvent} and if a player has its disco armor enabled, it will disable it to
     * troubleshoot some problems.
     *
     * @param event the event
     */
    @EventHandler
    public void onPlayerQuit( PlayerQuitEvent event ) {
        this.discoPlayers.remove( event.getPlayer().getUniqueId() );
        // clear the players armor contents
        ItemStack air = new ItemStack( Material.AIR );
        Player player = event.getPlayer();
        player.getInventory().setArmorContents( new ItemStack[] {
                air, air, air, air
        } );
        player.updateInventory();
    }

    @Override
    public void run() {
        this.discoPlayers.forEach( uuid -> {
            Player player = Bukkit.getPlayer( uuid );
            if ( player != null ) {
                // set the players armor contents based on the current hue value
                player.getInventory().setArmorContents( new ItemStack[] {
                        this.create( Material.LEATHER_BOOTS ),
                        this.create( Material.LEATHER_LEGGINGS ),
                        this.create( Material.LEATHER_CHESTPLATE ),
                        this.create( Material.LEATHER_HELMET )
                } );
                player.updateInventory();
            }
        } );
        // Update the hue value. As in our case the hue value will be substracted from its floored value and then
        // multiplied by 360 to create an usable angle, we will keep the hue value between 0 and 1.
        this.hue += this.speed;
        if ( this.hue >= 1 ) {
            this.hue = 0;
        }
    }

    private ItemStack create( Material material ) {
        ItemStack itemStack = new ItemStack( material );
        LeatherArmorMeta leatherArmorMeta = ( LeatherArmorMeta ) itemStack.getItemMeta();
        java.awt.Color color = java.awt.Color.getHSBColor( this.hue, 1, 1 );
        leatherArmorMeta.setColor( Color.fromRGB( color.getRed(), color.getGreen(), color.getBlue() ) );
        itemStack.setItemMeta( leatherArmorMeta );
        return itemStack;
    }
}
