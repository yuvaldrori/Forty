package me.drori.forty;

import org.junit.Test;

import java.text.MessageFormat;
import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class FortyUnitTest {
    @Test
    public void test_description() throws Exception {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String description = MessageFormat.format("{0}, {1}\n{2}", text, title, app);
        Notification notification = new Notification(app, title, text, Notification.actions.START, 0);
        Event event = new Event(notification);
        assertEquals(event.getDescription(), description);
    }

    @Test
    public void test_application() throws  Exception {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0}, {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Event event = new Event(0, 0, -1, eventTitle, description);
        assertEquals(event.getApplication(), app);
    }

    @Test
    public void test_no_event_in_last_24h() throws Exception {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0}, {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Notification notification = new Notification(app, title, text, Notification.actions.START, 0);
        Event event = new Event(0, 0, -1, eventTitle, description);
        Logic logic = new Logic(null, notification);
        assertEquals(logic.getEvents().get(0), event);
    }

    @Test
    public void test_close() throws Exception {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0}, {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Event event = new Event(0, 0, 1, eventTitle, description);
        Notification notification = new Notification(app, title, text, Notification.actions.STOP, 1);
        Logic logic = new Logic(event, notification);
        assertEquals(logic.getEvents().get(0).getEnd(), 1);
    }

    @Test
    public void test_restart() throws Exception {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0}, {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Event event = new Event(0, 1, 1, eventTitle, description);
        Notification notification = new Notification(app, title, text, Notification.actions.START, 3);
        Logic logic = new Logic(event, notification);
        assertEquals(logic.getEvents().get(0).getEnd(), 0);
    }

    @Test
    public void test_start() throws Exception {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0}, {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Event event = new Event(0, 1, 1, eventTitle, description);
        long newTime = Logic.SEPARATE_EVENT + 1;
        Event newEvent = new Event(newTime, newTime, Event.NEW_EVENT, eventTitle, description);
        Notification notification = new Notification(app, title, text, Notification.actions.START, newTime);
        Logic logic = new Logic(event, notification);
        assertEquals(logic.getEvents().get(0), newEvent);
    }

    @Test
    public void test_new_episode() throws Exception {
        String app = "app1";
        String title1 = "podcast 1";
        String text1 = "episode 1";
        String eventTitle1 = MessageFormat.format("{0}, {1}", text1, title1);
        String description1 = MessageFormat.format("{0}\n{1}", eventTitle1, app);
        String title2 = "podcast 2";
        String text2 = "episode 2";
        String eventTitle2 = MessageFormat.format("{0}, {1}", text2, title2);
        String description2 = MessageFormat.format("{0}\n{1}", eventTitle2, app);
        Event event1 = new Event(0, 0, 1, eventTitle1, description1);
        Notification notification = new Notification(app, title2, text2, Notification.actions.START, 2);
        Event event2 = new Event(2, 2, 2, eventTitle2, description2);
        Logic logic = new Logic(event1, notification);
        List<Event> events = logic.getEvents();
        assertEquals(events.size(), 2);
        event1.setEnd(2);
        assertEquals(events.get(0), event1);
        assertEquals(events.get(1), event2);
    }

}