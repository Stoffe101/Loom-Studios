package dev.loomstudios.client.project;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.project.LoomProject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory Recent Projects/library index.
 */
public final class ProjectLibraryIndex {
    private static List<ProjectDescriptor> entries = List.of();
    private static int rejectedFiles;
    private static UUID selectedProjectId;
    private record Cached(java.nio.file.attribute.FileTime modified,long size,LoomProject project,ProjectDescriptor descriptor){}
    private static final java.util.Map<Path,Cached> cache=new java.util.HashMap<>();
    public static LoomProject load(ProjectDescriptor descriptor)throws IOException{
        var attributes=java.nio.file.Files.readAttributes(descriptor.projectPath(),java.nio.file.attribute.BasicFileAttributes.class);
        Cached c=cache.get(descriptor.projectPath());
        if(c!=null&&c.modified().equals(attributes.lastModifiedTime())&&c.size()==attributes.size())return c.project();
        return LocalProjectLibrary.load(descriptor.projectPath());
    }

    private ProjectLibraryIndex() {
    }

    public static void refresh() {
        List<ProjectDescriptor> next = new ArrayList<>();
        int rejected = 0;

        try {
            for (Path path : LocalProjectLibrary.list()) {
                try {
                    var attributes=java.nio.file.Files.readAttributes(path,java.nio.file.attribute.BasicFileAttributes.class);
                    Cached cached=cache.get(path);
                    if(cached!=null&&cached.modified().equals(attributes.lastModifiedTime())&&cached.size()==attributes.size()&&java.nio.file.Files.isRegularFile(cached.descriptor().thumbnailPath())){next.add(cached.descriptor());continue;}
                    LoomProject project = LocalProjectLibrary.load(path);
                    String hash = project.hash();
                    Path thumbnail = ProjectThumbnailCache.ensure(project, hash);

                    next.add(new ProjectDescriptor(
                            project.projectId(),
                            project.name(),
                            project.metadata().createdAtEpochMillis(),
                            project.metadata().modifiedAtEpochMillis(),
                            hash,
                            path,
                            thumbnail
                    ));
                    cache.put(path,new Cached(attributes.lastModifiedTime(),attributes.size(),project,next.getLast()));
                } catch (IOException | IllegalArgumentException e) {
                    rejected++;
                    LoomStudios.LOGGER.warn(
                            "Skipping unreadable Loom project {}",
                            path,
                            e
                    );
                }
            }
        } catch (IOException e) {
            LoomStudios.LOGGER.warn("Failed to scan Loom project library", e);
        }

        next.sort(
                Comparator.comparingLong(ProjectDescriptor::modifiedAtEpochMillis)
                        .reversed()
                        .thenComparing(ProjectDescriptor::name)
        );

        entries = List.copyOf(next);
        cache.keySet().retainAll(next.stream().map(ProjectDescriptor::projectPath).toList());
        rejectedFiles = rejected;

        if (selectedProjectId != null && find(selectedProjectId).isEmpty()) {
            selectedProjectId = null;
        }

        if (selectedProjectId == null && !entries.isEmpty()) {
            selectedProjectId = entries.getFirst().projectId();
        }
    }

    public static List<ProjectDescriptor> entries() {
        return entries;
    }

    public static Optional<ProjectDescriptor> find(UUID projectId) {
        return entries.stream()
                .filter(entry -> entry.projectId().equals(projectId))
                .findFirst();
    }

    public static int rejectedFiles() {
        return rejectedFiles;
    }

    public static Optional<ProjectDescriptor> selected() {
        return selectedProjectId == null
                ? Optional.empty()
                : find(selectedProjectId);
    }

    public static void select(UUID projectId) {
        if (find(projectId).isEmpty()) {
            throw new IllegalArgumentException(
                    "Project is not present in the current Loom library index"
            );
        }

        selectedProjectId = projectId;
    }

    public static void clearSelection() {
        selectedProjectId = null;
    }
}
