package eternia.managers;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import eternia.SinnohSpawns;
import eternia.comm.Zone;
import eternia.configuration.ConfigManager;
import eternia.utilities.WorldInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SpawningManager {

    private Zone zone;
    private static final Random RAND = new Random();

    public SpawningManager(Zone zone) {
        this.zone = zone;
    }

    public EntityPixelmon spawnPokemon() {
        String choice = choosePokemon();
        if (choice == null)
            return null;
        Vector3i position = RandomHelper.getRandomElementFromList(zone.getSpawnPositions());
        return new PixelmonManager(choice, position.getX(), position.getY() + 1, position.getZ()).doSpawn();
    }

    public String choosePokemon() {
        List<Map.Entry<String, Integer>> entries = Lists.newArrayList(zone.getPokemon().entrySet());
        Collections.shuffle(entries);
        for (Map.Entry<String, Integer> entry : entries) {
            String alias = entry.getKey();
            if (doesSpawn(alias, entry.getValue())) {
                if(!ConfigManager.getPokemonNode(alias).isVirtual()) {
                    return alias;
                } else {
                    SinnohSpawns.getLogger().info(alias + " is not a valid alias! - [" + zone + "]");
                }
            }
        }
        return null;
    }

    private int getRand(int coord) {
        return coord + (RAND.nextInt(zone.getRadius() * 2) - zone.getRadius());
    }

    private boolean doesSpawn(String alias, int weight) {
        return isCorrectTime(alias) && RAND.nextInt(1001) <= weight;
    }

    private boolean isCorrectTime(String alias) {
        if(!ConfigManager.getPokemonNode(alias, "Time").isVirtual()) {
            String time = ConfigManager.getPokemonNode(alias, "Time").getString();
            return (time.equalsIgnoreCase("day") && WorldInfo.isDay()) || (time.equalsIgnoreCase("night") && !WorldInfo.isDay());
        }
        return true;
    }
}
