package org.simulation.event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventStack {
    List<Event> events;

    public EventStack() {
        events = new ArrayList<>();
    }

    public void addEvent(Event event) {
        events.add(event);
        events.sort(Comparator.comparingDouble(Event::getTime));
    }

    public Event getNextEvent() {
        if (events.isEmpty()) {
            return null;
        }
        events.sort(Comparator.comparingDouble(Event::getTime));
        return events.remove(0);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public List<Event> getEvents() {
        return events;
    }
}
