package com.probreezer.multiFuse.Utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ItemUtils {
    public static String getFriendlyItemName(String item) {
        var itemArray = item.split("_");
        var itemMap = Arrays.stream(itemArray).map(word ->
                word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()
        );
        var itemName = itemMap.collect(Collectors.joining(" "));
        return itemName;
    }
}