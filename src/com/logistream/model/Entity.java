package com.logistream.model;

import java.util.Objects;

/**
 * ABSTRACTION: root of every stored object. Each entity has an ID and
 * knows how to turn itself into one line of text for the data files.
 */
public abstract class Entity {
    private final String id;

    protected Entity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /** One pipe-separated line used for file storage. */
    public abstract String toRecord();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((Entity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
