package com.logistream.concurrent;

import java.util.LinkedList;

/**
 * PRODUCER-CONSUMER buffer using wait() / notifyAll().
 * Driver threads (producers) put delivery events in; the notifier thread (consumer) takes them out.
 * put() waits while the buffer is full; take() waits while it is empty.
 */
public class EventBuffer {
    private final LinkedList<String> queue = new LinkedList<String>();
    private final int capacity;

    public EventBuffer(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void put(String message) throws InterruptedException {
        while (queue.size() == capacity) {      // guarded block: re-check after every wake-up
            wait();
        }
        queue.addLast(message);
        notifyAll();                            // wake a waiting consumer
    }

    public synchronized String take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        String message = queue.removeFirst();
        notifyAll();                            // wake waiting producers (space is free now)
        return message;
    }
}
