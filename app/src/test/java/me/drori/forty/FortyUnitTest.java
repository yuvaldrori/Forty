package me.drori.forty;

import org.junit.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class FortyUnitTest {
    @Test
    public void test_description() {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String description = MessageFormat.format("{0}, {1}\n{2}", text, title, app);
        Notification notification = new Notification(app, title, text, 0);
        Event event = new Event(notification);
        assertEquals(event.getDescription(), description);
    }

    @Test
    public void test_application() {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0}, {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Event event = new Event(0, 0, -1, true, eventTitle, description);
        assertEquals(event.getApplication(), app);
    }

    @Test
    public void test_event_exists() {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0}, {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Notification notification = new Notification(app, title, text, 0);
        Event event = new Event(0, 0, -1, true, eventTitle, description);
        List<Event> events = new ArrayList<>();
        events.add(event);
        Logic logic = new Logic(events, notification);
        assertEquals(logic.getEvent(), null);
    }

    @Test
    public void test_event_new() {
        String app = "app2";
        String title = "podcast 1";
        String text = "episode ";
        String eventTitle = MessageFormat.format("{0}, {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        String app1 = "app1";
        String title1 = "podcast 1";
        String text1 = "episode 1";
        String eventTitle1 = MessageFormat.format("{0}, {1}", text1, title1);
        String description1 = MessageFormat.format("{0}\n{1}", eventTitle1, app1);
        String app2 = "app1";
        String title2 = "podcast 1";
        String text2 = "episode 2";
        Notification notification = new Notification(app2, title2, text2, 0);
        Event event = new Event(0, 0, -1, true, eventTitle, description);
        Event event1 = new Event(0, 0, -1, true, eventTitle1, description1);
        List<Event> events = new ArrayList<>();
        events.add(event);
        events.add(event1);
        Logic logic = new Logic(events, notification);
        assertEquals(logic.getEvent(), new Event(notification));
    }

}