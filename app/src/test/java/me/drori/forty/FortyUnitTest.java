package me.drori.forty;

import org.junit.Test;

import java.text.MessageFormat;

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
        String description = MessageFormat.format("{0} {1}\n{2}", text, title, app);
        Notification notification = new Notification(app, title, text, Notification.actions.START, 0);
        Event event = new Event(notification);
        assertEquals(event.getDescription(), description);
    }

    @Test
    public void test_application() throws  Exception {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0} {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Event event = new Event(0, 0, -1, eventTitle, description);
        assertEquals(event.getApplication(), app);
    }

    @Test
    public void test_no_event_in_last_24h() throws Exception {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0} {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Notification notification = new Notification(app, title, text, Notification.actions.START, 0);
        Event event = new Event(0, 0, -1, eventTitle, description);
        Logic logic = new Logic(null, notification);
        assertEquals(logic.getEvent(), event);
    }

    @Test
    public void test_close() throws Exception {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0} {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Event event = new Event(0, 0, 1, eventTitle, description);
        Notification notification = new Notification(app, title, text, Notification.actions.STOP, 1);
        Logic logic = new Logic(event, notification);
        assertEquals(logic.getEvent().getEnd(), 1);
    }

    @Test
    public void test_close() throws Exception {
        String app = "app1";
        String title = "podcast 1";
        String text = "episode 1";
        String eventTitle = MessageFormat.format("{0} {1}", text, title);
        String description = MessageFormat.format("{0}\n{1}", eventTitle, app);
        Event event = new Event(0, 0, 1, eventTitle, description);
        Notification notification = new Notification(app, title, text, Notification.actions.STOP, 1);
        Logic logic = new Logic(event, notification);
        assertEquals(logic.getEvent().getEnd(), 1);
    }

}