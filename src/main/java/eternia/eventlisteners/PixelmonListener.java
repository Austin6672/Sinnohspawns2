package eternia.eventlisteners;

import com.pixelmonmod.pixelmon.api.events.EvolveEvent;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PixelmonListener {

    private String name, nick;

    @SubscribeEvent
    public void onEvo(EvolveEvent.PreEvolve e) {
        name = e.preEvo.getPokemonName();
        nick = e.preEvo.getNickname();
    }

    @SubscribeEvent
    public void onEvo(EvolveEvent.PostEvolve e) {
        e.pokemon.getPokemonData().setNickname(nick.replace(name, e.pokemon.getPokemonName()));
    }
}
