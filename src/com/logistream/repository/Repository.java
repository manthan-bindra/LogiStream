package com.logistream.repository;

import com.logistream.model.Entity;

import java.util.List;
import java.util.Set;

/** INTERFACE: the contract for storing entities during the current application run. */
public interface Repository<T extends Entity> {
    void save(T entity);                     // create OR update

    T get(String id);                        // read (null when missing)

    List<T> listAll();

    boolean delete(String id);               // delete

    boolean exists(String id);

    int count();

    Set<String> allIds();

    void reload();                           // re-read the file (used after restore)
}
