package dev.loomstudios.network.payload;

import dev.loomstudios.LoomStudios;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record EquippedStateS2CPayload(UUID playerId, String projectHash)
        implements CustomPacketPayload {
    public static final Type<EquippedStateS2CPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(LoomStudios.MOD_ID, "equipped_state_s2c")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EquippedStateS2CPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeUUID(payload.playerId());
                        buf.writeUtf(payload.projectHash(), 64);
                    },
                    buf -> new EquippedStateS2CPayload(
                            buf.readUUID(),
                            buf.readUtf(64)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
