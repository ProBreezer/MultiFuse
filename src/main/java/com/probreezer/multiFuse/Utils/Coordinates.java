package com.probreezer.multiFuse.Utils;

public class Coordinates {
    public int x;
    public int y;
    public int z;

    public Coordinates(String value) {
        var coordinates = value.split(",");
        this.x = Integer.parseInt(coordinates[0]);
        this.y = Integer.parseInt(coordinates[1]);
        this.z = Integer.parseInt(coordinates[2]);
    }
}