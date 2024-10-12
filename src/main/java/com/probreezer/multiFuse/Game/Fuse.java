package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.Blocks.BlockManager;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.Coordinates;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class Fuse {
    public final Material fuseBlock;
    private final MultiFuse plugin;
    private final Location hologramLocation;
    public int id;
    public String colour;
    public float health;
    public float maxHealth;
    public int percentageHealth;
    public boolean respawned;
    public Coordinates coordinates;
    public Block block;
    private FileConfiguration config;
    private FuseManager fuseManager;


    public Fuse(MultiFuse plugin, FuseManager fuseManager, int id, String colour, int health, Coordinates coordinates) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.fuseManager = fuseManager;
        this.fuseBlock = BlockManager.getTeamFuseBlock(colour);
        this.id = id;
        this.colour = colour;

        boolean debug = config.getBoolean("Debug", false);
        if (debug) {
            health /= 4;
        }

        this.health = health;
        this.maxHealth = health;
        this.percentageHealth = 100;
        this.respawned = false;
        this.coordinates = coordinates;
        this.hologramLocation = new Location(Bukkit.getWorld("world"), coordinates.x + 0.5, coordinates.y + 1.5, coordinates.z + 0.5);

        createFuse(colour, coordinates);
    }

    public String getTeamColour() {
        return this.colour;
    }

    public int getHealth() {
        return (int) this.health;
    }

    private void createFuse(String colour, Coordinates coordinates) {
        plugin.getLogger().info("Creating fuse for team " + colour);
        var fuse = this.fuseBlock;
        var world = plugin.getServer().getWorlds().getFirst();
        this.block = world.getBlockAt(coordinates.x, coordinates.y, coordinates.z);
        this.block.setType(fuse);
        this.plugin.hologramManager.addHologram(colour + id, List.of("&lFuse " + id), hologramLocation);
    }

    private int getPercentageHealth() {
        return (this.maxHealth > 0) ? (int) Math.ceil((this.health / this.maxHealth) * 100) : 0;
    }

    public void takeDamage(String colour) {
        if (this.health <= 0) return;

        if (this.health - 1 <= 0) {
            this.health = 0;
        } else {
            this.health -= 1;
        }

        var location = this.block.getLocation().add(0.5, 0.5, 0.5);
        this.block.getWorld().spawnParticle(Particle.SMOKE, location, 100, 0.2, 0.2, 0.2, 0.05);

        if (this.health <= 0) {
            if (!this.respawned) {
                this.block.setType(Material.valueOf(this.colour.toUpperCase() + "_STAINED_GLASS"));
                this.block.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, location, 100, 0.5, 0.5, 0.5, 0.1);
            } else {
                this.block.setType(Material.AIR);
                this.block.getWorld().spawnParticle(Particle.EXPLOSION, location, 20, 0.2, 0.2, 0.2, 0.05);
            }
        }

        this.percentageHealth = getPercentageHealth();

        var totalTeamFuseHealth = fuseManager.getTeamFuseHealth(this.colour);
        if (totalTeamFuseHealth <= 0) {
            plugin.game.endGame(colour);
        }
    }

    public void repair(Player player) {
        if (this.health >= this.maxHealth || this.health <= 0) return;
        if (this.health + 1 > this.maxHealth) {
            this.health = this.maxHealth;
        } else {
            this.health += 1;
        }

        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);

        this.percentageHealth = (this.maxHealth > 0) ? (int) Math.ceil((this.health / this.maxHealth) * 100) : 0;
    }

    public void respawn(Player player) {
        if (this.health > 0 || this.respawned) return;

        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
        this.block.setType(this.fuseBlock);
        this.health = this.maxHealth / 4;
        this.percentageHealth = getPercentageHealth();
        this.respawned = true;
    }
}
