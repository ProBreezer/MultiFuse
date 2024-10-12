package com.probreezer.multiFuse.Utils;

import java.util.Random;

public class RandomUtils {
    public static int getWeightedRandom(int min, int max) {
        if (min >= max) {
            return min;
        }

        var random = new Random();
        double randomValue = random.nextDouble();
        int range = max - min + 1;
        return min + (int) Math.floor(Math.pow(randomValue, 3) * range);
    }

}
