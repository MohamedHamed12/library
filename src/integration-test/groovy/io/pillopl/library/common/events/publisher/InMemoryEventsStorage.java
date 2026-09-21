package io.pillopl.library.common.events.publisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.commons.events.publisher.EventsStorage;

public class InMemoryEventsStorage implements EventsStorage {

  // it's not thread safe, enough for testing
  private final List<DomainEvent> eventList = Collections.synchronizedList(new ArrayList<>());

  @Override
  public synchronized void save(DomainEvent event) {
    eventList.add(event);
  }

  @Override
  public synchronized List<DomainEvent> toPublish() {
    return List.copyOf(eventList);
  }

  @Override
  public synchronized void published(List<DomainEvent> events) {
    eventList.removeAll(events);
  }
}
