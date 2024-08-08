package eternia.configuration;

import eternia.SinnohSpawns;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static Path dir, zones, pokemon;
    private static ConfigurationLoader<CommentedConfigurationNode> zonesLoad, pokemonLoad;
    private static CommentedConfigurationNode zonesNode, pokemonNode;
    private static final String ZONES = "Zones.conf", POKE = "Pokemon.conf";

    public static void setup(Path folder) {
        dir = folder;
        zones = dir.resolve(ZONES);
        pokemon = dir.resolve(POKE);
        load();
    }

    public static void load() {
        try {
            if(!Files.exists(dir))
                Files.createDirectory(dir);

            SinnohSpawns.getContainer().getAsset(ZONES).get().copyToFile(zones, false, true);
            SinnohSpawns.getContainer().getAsset(POKE).get().copyToFile(pokemon, false, true);
            zonesLoad = HoconConfigurationLoader.builder().setPath(zones).build();
            pokemonLoad = HoconConfigurationLoader.builder().setPath(pokemon).build();
            zonesNode = zonesLoad.load();
            pokemonNode = pokemonLoad.load();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            zonesLoad.save(zonesNode);
            pokemonLoad.save(pokemonNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CommentedConfigurationNode getZonesNode(Object... node) {
        return zonesNode.getNode(node);
    }

    public static CommentedConfigurationNode getPokemonNode(Object... node) {
        return pokemonNode.getNode(node);
    }

    public static CommentedConfigurationNode getZoneRoot() {
        return zonesNode;
    }
}

