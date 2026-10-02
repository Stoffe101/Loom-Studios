package dev.loomstudios.project;

public enum AnimationChannel {
    CAPE("cape", "Cape"),
    ELYTRA("elytra", "Elytra");

    private final String id;
    private final String displayName;

    AnimationChannel(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public static AnimationChannel fromId(String id) {
        for (AnimationChannel value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        throw new IllegalArgumentException(
                "Unknown animation channel " + id
        );
    }
}
