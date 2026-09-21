package io.pillopl.library.commons.events.publisher;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.commons.events.DomainEvents;

public class StoreAndForwardDomainEventPublisher implements DomainEvents {

  private final DomainEvents eventsPublisher;
  private final EventsStorage eventsStorage;

  public StoreAndForwardDomainEventPublisher(
      DomainEvents eventsPublisher, EventsStorage eventsStorage) {
    this.eventsPublisher = eventsPublisher;
    this.eventsStorage = eventsStorage;
  }

  @Override
  public void publish(DomainEvent event) {
    eventsStorage.save(event);
  }

  @Scheduled(fixedRate = 3000L)
  @Transactional
  public void publishAllPeriodically() {
    List<DomainEvent> domainEvents = eventsStorage.toPublish();
    domainEvents.forEach(eventsPublisher::publish);
    eventsStorage.published(domainEvents);
  }
}
