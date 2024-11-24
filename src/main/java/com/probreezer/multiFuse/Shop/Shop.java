package com.probreezer.multiFuse.Shop;

import com.probreezer.multiFuse.Blocks.BlockManager;
import com.probreezer.multiFuse.MultiFuse;
import com.probreezer.multiFuse.Utils.ConfigUtils;
import com.probreezer.multiFuse.Utils.Coordinates;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class Shop {
    private final MultiFuse plugin;
    private final Location hologramLocation;
    public Material shopBlock;
    public String colour;
    public Coordinates coordinates;
    public Block block;
    private YamlConfiguration config;

    public Shop(MultiFuse plugin, String team) {
        this.config = ConfigUtils.getConfig("teams");
        this.colour = team;

        var teamColour = this.colour;
        var teamSection = this.config.getConfigurationSection(teamColour);

        this.plugin = plugin;
        this.shopBlock = BlockManager.getTeamShopBlock(teamColour);
        this.coordinates = new Coordinates(teamSection.getString("ShopLocation"));
        this.hologramLocation = new Location(Bukkit.getWorld("world"), coordinates.x + 0.5, coordinates.y + 1.5, coordinates.z + 0.5);

        createShop(colour, coordinates);
    }

    private void createShop(String colour, Coordinates coordinates) {
        plugin.getLogger().info("Creating shop for team " + colour);
        var shop = this.shopBlock;
        var world = plugin.getServer().getWorlds().getFirst();
        this.block = world.getBlockAt(coordinates.x, coordinates.y, coordinates.z);
        this.block.setType(shop);
        this.plugin.hologramManager.addHologram(colour + "shop", List.of("&lShop"), hologramLocation);
    }
}
