package com.logistream.concurrent;
/** Consumer thread: prints notifications produced by delivery workers. */
public class NotificationWorker extends Thread {
 public static final String STOP="__STOP__"; private final EventBuffer buffer;
 public NotificationWorker(EventBuffer buffer){super("Notifier");this.buffer=buffer;}
 public void run(){try{while(true){String message=buffer.take();if(message.equals(STOP))break;System.out.println("  [SMS] "+message);}}catch(InterruptedException e){Thread.currentThread().interrupt();}}
}
