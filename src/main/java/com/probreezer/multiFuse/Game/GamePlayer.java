package com.probreezer.multiFuse.Game;

import com.probreezer.multiFuse.MultiFuse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public class GamePlayer {
    private final MultiFuse plugin;
    private final TeamManager teamManager;
    public UUID Id;
    public String name;
    public String kit;
    public Location spawn;

    public GamePlayer(MultiFuse plugin, Player player) {
        this.plugin = plugin;
        this.teamManager = plugin.game.teamManager;
        this.Id = player.getUniqueId();
        this.name = player.getName();
        setTeam(player, null);
    }

    public void setTeam(Player player, String team) {
        if (team != null) {
            var currentProjectedTeamSize = teamManager.getOtherTeamPlayers(team) == null || teamManager.getOtherTeamPlayers(team).size() <= 1 ? 0 : teamManager.getOtherTeamPlayers(team).size();
            var newProjectedTeamSize = (teamManager.getTeamPlayers(team) == null ? 0 : teamManager.getTeamPlayers(team).size()) + 1;

            var loadBalancingCheck = (Math.max(currentProjectedTeamSize, newProjectedTeamSize) - Math.min(currentProjectedTeamSize, newProjectedTeamSize)) <= 1;

            if (!loadBalancingCheck) {
                player.sendMessage("This team already has too many players");
                return;
            }
            this.spawn = getRandomSpawnPoint(team);
        }

        teamManager.addPlayerToTeam(team, player, this);

        team = (team != null) ? team : "Gray";
        plugin.scoreboard.setPlayerTeam(player, team);
    }

    public void setKit(String kit) {
        this.kit = kit;
    }

    public Location getRandomSpawnPoint(String team) {
        var world = Bukkit.getServer().getWorlds().getFirst();
        var random = new Random();
        var spawnArea = teamManager.getTeam(team).spawnArea;

        double x1 = spawnArea.x1, x2 = spawnArea.x2, y1 = spawnArea.y1, y2 = spawnArea.y2, z1 = spawnArea.z1, z2 = spawnArea.z2;

        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxZ = Math.max(z1, z2);

        double randomX = minX + ((maxX - minX) * random.nextDouble());
        double randomY = minY + ((maxY - minY) * random.nextDouble());
        double randomZ = minZ + ((maxZ - minZ) * random.nextDouble());

        return new Location(world, randomX, randomY, randomZ);
    }

    public void resetPlayer(Player player) {
        teamManager.addPlayerToTeam(null, player, this);
        this.kit = null;
        this.spawn = null;
    }
}
