package dev.loomstudios.network.payload;

import dev.loomstudios.LoomStudios;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ProjectRequestC2SPayload(String projectHash) implements CustomPacketPayload {
    public static final Type<ProjectRequestC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(LoomStudios.MOD_ID, "project_request_c2s")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProjectRequestC2SPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeUtf(payload.projectHash(), 64);
                    },
                    buf -> new ProjectRequestC2SPayload(
                            buf.readUtf(64)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
