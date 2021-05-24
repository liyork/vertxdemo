package com.wolf.inaction.simpleeventloop;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Description:
 * Created on 2021/5/24 9:11 AM
 *
 * @author 李超
 * @version 0.0.1
 */
public class EventLoop {
  private final ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue<Event>();

  private final ConcurrentHashMap<String, Consumer<Object>> handlers = new ConcurrentHashMap<String, Consumer<Object>>();

  // 放入处理事件的handler
  public EventLoop on(String key, Consumer<Object> handler) {
    handlers.put(key, handler);
    return this;
  }

  // 放入事件
  public void dispatch(Event event) {
    events.add(event);
  }

  public void stop() {
    Thread.currentThread().interrupt();
  }

  public void run() {
    while (!(events.isEmpty() && Thread.interrupted())) {
      if (!events.isEmpty()) {
        Event event = events.poll();
        if (handlers.containsKey(event.key)) {
          handlers.get(event.key).accept(event.date);
        } else {
          System.err.println("No handler for key " + event.key);
        }
      }
    }
  }

  public static final class Event {
    private final String key;
    private final Object date;

    public Event(String key, Object date) {
      this.key = key;
      this.date = date;
    }
  }

}


