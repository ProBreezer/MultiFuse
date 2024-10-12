package com.probreezer.multiFuse.Blocks;

import org.bukkit.Material;

public class TeamBlock {
    public Material fuseMaterial;
    public Material brokenFuseMaterial;
    public Material destoryedFuseMaterial;
    public Material shopMaterial;

    public TeamBlock(String team, String fuseMaterial, String shopMaterial) {
        this.fuseMaterial = Material.valueOf(fuseMaterial.toUpperCase());
        this.brokenFuseMaterial = Material.valueOf(team.toUpperCase() + "_STAINED_GLASS");
        this.destoryedFuseMaterial = Material.AIR;
        this.shopMaterial = Material.valueOf(shopMaterial.toUpperCase());
    }
}
