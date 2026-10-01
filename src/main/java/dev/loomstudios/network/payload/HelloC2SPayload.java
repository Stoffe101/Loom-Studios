package dev.loomstudios.network.payload;

import dev.loomstudios.LoomStudios;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HelloC2SPayload(int protocolVersion, String projectHash) implements CustomPacketPayload {
    public static final Type<HelloC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(LoomStudios.MOD_ID, "hello_c2s")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, HelloC2SPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeVarInt(payload.protocolVersion());
                        buf.writeUtf(payload.projectHash(), 64);
                    },
                    buf -> new HelloC2SPayload(
                            buf.readVarInt(),
                            buf.readUtf(64)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
