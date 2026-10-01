package dev.loomstudios.network.payload;

import dev.loomstudios.LoomStudios;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ProjectNeededS2CPayload(String projectHash) implements CustomPacketPayload {
    public static final Type<ProjectNeededS2CPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(LoomStudios.MOD_ID, "project_needed_s2c")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProjectNeededS2CPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeUtf(payload.projectHash(), 64);
                    },
                    buf -> new ProjectNeededS2CPayload(
                            buf.readUtf(64)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
