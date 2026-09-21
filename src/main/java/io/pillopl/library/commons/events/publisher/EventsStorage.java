package io.pillopl.library.commons.events.publisher;

import java.util.List;

import io.pillopl.library.commons.events.DomainEvent;

public interface EventsStorage {

  void save(DomainEvent event);

  List<DomainEvent> toPublish();

  void published(List<DomainEvent> events);
}
