package io.pillopl.library.commons.events.publisher;

import org.springframework.context.ApplicationEventPublisher;

import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.commons.events.DomainEvents;

public class JustForwardDomainEventPublisher implements DomainEvents {

  private final ApplicationEventPublisher applicationEventPublisher;

  public JustForwardDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void publish(DomainEvent event) {
    applicationEventPublisher.publishEvent(event);
  }
}
