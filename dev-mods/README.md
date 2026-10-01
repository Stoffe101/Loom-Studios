# Optional development mods

JARs in this folder are loaded by the Loom Studios development runtime via Gradle/Loom `modLocalRuntime`.

They are **optional test integrations**, not Loom Studios dependencies, and are not bundled in the produced mod JAR.

Current test set:

- `sodium-fabric-0.8.7+mc1.21.11.jar`
- `sodium-extra-fabric-0.8.3+mc1.21.11.jar`
- `iris-fabric-1.10.7+mc1.21.11.jar`
- `skinlayers3d-fabric-1.11.3-mc1.21.11.jar`

Copy any or all of those JARs here, then launch **Loom Studios - Client** in IntelliJ or run `gradlew runClient`.

The JAR files are gitignored intentionally.
