package io.pillopl.library.commons.events.publisher;

import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.commons.events.DomainEvents;
import org.springframework.context.ApplicationEventPublisher;

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
