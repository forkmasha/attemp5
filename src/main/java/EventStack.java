import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventStack {
    List<Event> events;

    public EventStack() {
        events = new ArrayList<>();
    }

    public void addEvent(Event event) {
        events.add(event);
        Collections.sort(events, (e1, e2) -> Double.compare(e1.getTime(), e2.getTime()));
    }

    public Event getNextEvent() {
        if (events.isEmpty()) {
            return null;
        }
        Collections.sort(events, (e1, e2) -> Double.compare(e1.getTime(), e2.getTime()));
        return events.remove(0);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }
}