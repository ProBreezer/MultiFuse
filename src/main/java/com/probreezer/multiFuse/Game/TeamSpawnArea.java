package com.probreezer.multiFuse.Game;

public class TeamSpawnArea {
    public double x1, y1, z1, x2, y2, z2;

    public TeamSpawnArea(String area1, String area2) {
        var corner1 = area1.split(",");
        var corner2 = area2.split(",");

        this.x1 = Double.parseDouble(corner1[0]);
        this.y1 = Double.parseDouble(corner1[1]);
        this.z1 = Double.parseDouble(corner1[2]);
        this.x2 = Double.parseDouble(corner2[0]);
        this.y2 = Double.parseDouble(corner2[1]);
        this.z2 = Double.parseDouble(corner2[2]);
    }
}
