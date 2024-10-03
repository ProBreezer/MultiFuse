package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.MultiFuse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlayerManager {
    public MultiFuse plugin;
    public List<GamePlayer> gamePlayers = new ArrayList<>();

    public PlayerManager(MultiFuse plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(GamePlayer gamePlayer) {
        gamePlayers.add(gamePlayer);
    }

    public void removePlayer(UUID Id) {
        gamePlayers.stream().filter(p -> p.Id.equals(Id)).findFirst().ifPresent(gamePlayers::remove);
    }

    public Optional<GamePlayer> getPlayer(UUID id) {
        return gamePlayers.stream().filter(p -> p.Id.equals(id)).findFirst();
    }
}
