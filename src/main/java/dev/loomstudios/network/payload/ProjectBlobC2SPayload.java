package dev.loomstudios.network.payload;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.project.LoomProjectCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ProjectBlobC2SPayload(String projectHash, byte[] data)
        implements CustomPacketPayload {
    public static final Type<ProjectBlobC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(LoomStudios.MOD_ID, "project_blob_c2s")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProjectBlobC2SPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeUtf(payload.projectHash(), 64);
                        if (payload.data().length > LoomProjectCodec.MAX_SERIALIZED_BYTES) {
                            throw new IllegalArgumentException("Project blob too large");
                        }
                        buf.writeByteArray(payload.data());
                    },
                    buf -> new ProjectBlobC2SPayload(
                            buf.readUtf(64),
                            buf.readByteArray(LoomProjectCodec.MAX_SERIALIZED_BYTES)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
