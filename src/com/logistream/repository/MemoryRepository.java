package com.logistream.repository;

import com.logistream.model.Entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/** Generic in-memory repository. Data lasts for the current application run only. */
public class MemoryRepository<T extends Entity> implements Repository<T> {
    private final Map<String, T> store = new LinkedHashMap<String, T>();
    @Override public synchronized void save(T entity) { store.put(entity.getId(), entity); }
    @Override public synchronized T get(String id) { return store.get(id); }
    @Override public synchronized List<T> listAll() { return new ArrayList<T>(store.values()); }
    @Override public synchronized boolean delete(String id) { return store.remove(id) != null; }
    @Override public synchronized boolean exists(String id) { return store.containsKey(id); }
    @Override public synchronized int count() { return store.size(); }
    @Override public synchronized Set<String> allIds() { return new HashSet<String>(store.keySet()); }
    @Override public synchronized void reload() { /* No disk storage in Phase 1. */ }

    public static String nextId(String prefix, Collection<String> existingIds) {
        int max = 0;
        for (String id : existingIds) {
            if (id.startsWith(prefix)) {
                try { max = Math.max(max, Integer.parseInt(id.substring(prefix.length()))); }
                catch (NumberFormatException ignored) { }
            }
        }
        return prefix + String.format("%04d", max + 1);
    }
}
