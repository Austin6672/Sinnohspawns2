package eternia.managers;

import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import eternia.SinnohSpawns;
import eternia.configuration.ConfigManager;
import eternia.utilities.Utilities;
import eternia.utilities.WorldInfo;
import net.minecraft.nbt.NBTTagCompound;


import java.util.Random;

public class PixelmonManager {

    private boolean isPokemon = false;
    private EntityPixelmon pokemon;
    private int X, Y, Z;
    private String alias;
    private static final Random RAND = new Random();
    private int[] evs = {0,0,0,0,0,0};
    private int[] ivs = {0,0,0,0,0,0};

    public PixelmonManager(String pokeName, int x, int y, int z) {
        alias = pokeName;
        X = x;
        Y = y;
        Z = z;
    }

    public EntityPixelmon doSpawn() {
        EntityPixelmon pokemon = getPokemon();
        if (pokemon != null) {
            worldSpawn();
            return pokemon;
        }
        return null;
    }

    public EntityPixelmon getPokemon() {
        neededSetup();
        if (isPokemon) {
            addStats();
            setValues();
            addTextures();
            return pokemon;
        }
        return null;
    }

    private void neededSetup() {
        String name = ConfigManager.getPokemonNode(alias, "Pokemon-Name").getString();
        if(name != null && EntityPixelmon.getLocalizedName(name) != null) {
            isPokemon = true;
            pokemon = (EntityPixelmon) PixelmonEntityList.createEntityByName(name, (net.minecraft.world.World) WorldInfo.WORLD);
            String spec = ConfigManager.getPokemonNode(alias, "Spec").getString();
            if (spec != null) {
                new PokemonSpec(spec).apply(pokemon);
            }
            int levelLow = ConfigManager.getPokemonNode(alias, "Level-Low").getInt();
            int levelHigh = ConfigManager.getPokemonNode(alias, "Level-High").getInt();
            pokemon.getPokemonData().setLevel(RAND.nextInt(levelHigh - levelLow) + levelLow);

            if (ConfigManager.getPokemonNode(alias, "Unbreedable").getBoolean()) {
                PokemonSpec unbreed = PokemonSpec.from("unbreedable");
                unbreed.apply(pokemon);
            }

        } else {
            AdvancedSpawns.getLogger().warn(name + " is not a valid pokemon name! - [" + alias + "]");
        }
    }

    //adds ivs to each pokemon
    private void addIVs() {
        pokemon.getPokemonData().getIVs().set(StatsType.Attack, ivs[0]);
        pokemon.getPokemonData().getIVs().set(StatsType.Defence, ivs[1]);
        pokemon.getPokemonData().getIVs().set(StatsType.Speed, ivs[2]);
        pokemon.getPokemonData().getIVs().set(StatsType.HP, ivs[3]);
        pokemon.getPokemonData().getIVs().set(StatsType.SpecialAttack, ivs[4]);
        pokemon.getPokemonData().getIVs().set(StatsType.SpecialDefence, ivs[5]);
    }

    //adds evs to each pokemon
    private void addEVs() {
        pokemon.getPokemonData().getEVs().set(StatsType.Attack, evs[0]);
        pokemon.getPokemonData().getEVs().set(StatsType.Defence, evs[1]);
        pokemon.getPokemonData().getEVs().set(StatsType.Speed, evs[2]);
        pokemon.getPokemonData().getEVs().set(StatsType.HP, evs[3]);
        pokemon.getPokemonData().getEVs().set(StatsType.SpecialAttack, evs[4]);
        pokemon.getPokemonData().getEVs().set(StatsType.SpecialDefence, evs[5]);
    }

    //adds base stats to the pokemon such as nicknames, abilities, and natures
    private void addStats() {
        if(!ConfigManager.getPokemonNode(alias, "Nickname").isVirtual()) {
            String nickname = ConfigManager.getPokemonNode(alias, "Nickname").getString();
            nickname = nickname.replace('&', '\u00A7');
            pokemon.getPokemonData().setNickname(nickname + pokemon.getName() + '\u00A7' + "r");
        }

        if(!ConfigManager.getPokemonNode(alias, "Nature").isVirtual()) {
            String nature = ConfigManager.getPokemonNode(alias, "Nature").getString();
            pokemon.getPokemonData().setNature(EnumNature.natureFromString(nature));
        }

        if(!ConfigManager.getPokemonNode(alias, "Ability").isVirtual()) {
            String ability = ConfigManager.getPokemonNode(alias, "Ability").getString();
            pokemon.getPokemonData().setAbility(ability);
        }
    }

    private void addTextures() {
        if(!ConfigManager.getPokemonNode(alias, "Texture").isVirtual()) {
            NBTTagCompound nbt = new NBTTagCompound();
            pokemon.writeToNBT(nbt).setString("CustomTexture", ConfigManager.getPokemonNode(alias, "Texture").getString());
            pokemon.readFromNBT(nbt);
        }
    }

    private void worldSpawn() {
        pokemon.setPositionAndRotation(X, Y, Z, 0, 0);
        WorldInfo.WORLD.spawnEntity((Entity) pokemon);
        addParticles();
    }

    //adds particles to pokemon if needed
    private void addParticles() {
        if(!ConfigManager.getPokemonNode(alias, "Particle").isVirtual()) {
            String particle = ConfigManager.getPokemonNode(alias, "Particle").getString();
            String entityUUID = pokemon.getUniqueID().toString();
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "ep set " + WorldInfo.WORLD_UUID.toString() + " " + entityUUID + " " + particle);
        }
    }

    //sets up evs and ivs from config or random/0 if not valid
    private void setValues() {
        for(int i = 0; i < 6; i++) {
            if(!ConfigManager.getPokemonNode(alias, "IVs", Utilities.arr[i]).isVirtual()) {
                ivs[i] = ConfigManager.getPokemonNode(alias, "IVs", Utilities.arr[i]).getInt();
            } else {
                ivs[i] = RAND.nextInt(30) + 1;
            }

            if(!ConfigManager.getPokemonNode(alias, "EVs", Utilities.arr[i]).isVirtual()) {
                evs[i] = ConfigManager.getPokemonNode(alias, "EVs", Utilities.arr[i]).getInt();
            } else {
                evs[i] = 0;
            }
        }
        addEVs();
        addIVs();
    }
}
