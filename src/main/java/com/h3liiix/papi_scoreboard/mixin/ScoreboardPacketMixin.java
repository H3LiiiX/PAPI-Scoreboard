package com.h3liiix.papi_scoreboard.mixin;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ScoreboardPacketMixin {

    @ModifyVariable(
            method = { "method_52280", "method_14364" },
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            remap = false
    )
    private Packet<?> modifyScoreboardPacket(Packet<?> packet) {
        if (packet instanceof ClientboundSetScorePacket scorePacket) {
            
            if ((Object) this instanceof ServerGamePacketListenerImpl gameListener) {
                ServerPlayer player = gameListener.player;
                if (player == null) return packet;

                Component textToParse = scorePacket.display().orElse(Component.literal(scorePacket.owner()));

                if (textToParse.getString().contains("%")) {
                    
                    PlaceholderContext context = PlaceholderContext.of(player);
                    Component parsedComponent = Placeholders.parseText(textToParse, context);

                    return new ClientboundSetScorePacket(
                            scorePacket.owner(),
                            scorePacket.objectiveName(),
                            scorePacket.score(),
                            Optional.of(parsedComponent),
                            scorePacket.numberFormat()
                    );
                }
            }
        }
        return packet;
    }
}