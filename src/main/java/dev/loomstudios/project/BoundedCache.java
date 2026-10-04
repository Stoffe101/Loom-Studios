package dev.loomstudios.project;

import java.util.LinkedHashMap;
import java.util.function.ToLongFunction;

/** Access ordered, byte and entry bounded cache. All access occurs on the owning game thread. */
public final class BoundedCache<K, V> {
    private final LinkedHashMap<K, V> values = new LinkedHashMap<>(16, .75f, true);
    private final long limit;
    private final int entries;
    private final ToLongFunction<V> size;
    private long bytes;

    public BoundedCache(long limit, int entries, ToLongFunction<V> size) {
        this.limit = limit;
        this.entries = entries;
        this.size = size;
    }

    public V get(K key) {
        return values.get(key);
    }

    public boolean containsKey(K key) {
        return values.containsKey(key);
    }

    public void put(K key, V value) {
        long weight = size.applyAsLong(value);
        if (weight > limit)
            throw new IllegalArgumentException("Project exceeds cache memory budget");
        V old = values.remove(key);
        if (old != null) bytes -= size.applyAsLong(old);
        values.put(key, value);
        bytes += weight;
        while (bytes > limit || values.size() > entries) {
            var it = values.entrySet().iterator();
            var e = it.next();
            bytes -= size.applyAsLong(e.getValue());
            it.remove();
        }
    }

    public void clear() {
        values.clear();
        bytes = 0;
    }

    public long retainedBytes() {
        return bytes;
    }

    public int size() {
        return values.size();
    }
}
