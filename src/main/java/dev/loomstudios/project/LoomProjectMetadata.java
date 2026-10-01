package dev.loomstudios.project;

/**
 * User-facing project timestamps stored inside the editable .loom source.
 */
public record LoomProjectMetadata(
        long createdAtEpochMillis,
        long modifiedAtEpochMillis
) {
    public LoomProjectMetadata {
        if (createdAtEpochMillis < 0L) {
            throw new IllegalArgumentException("Created timestamp cannot be negative");
        }

        if (modifiedAtEpochMillis < createdAtEpochMillis) {
            throw new IllegalArgumentException(
                    "Modified timestamp cannot be earlier than created timestamp"
            );
        }
    }

    public LoomProjectMetadata touch(long nowEpochMillis) {
        if (nowEpochMillis < this.modifiedAtEpochMillis) {
            nowEpochMillis = this.modifiedAtEpochMillis;
        }

        return new LoomProjectMetadata(this.createdAtEpochMillis, nowEpochMillis);
    }

    public static LoomProjectMetadata now(long nowEpochMillis) {
        return new LoomProjectMetadata(nowEpochMillis, nowEpochMillis);
    }
}
