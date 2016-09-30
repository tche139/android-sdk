/**
 * Copyright 2016, Optimizely
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.optimizely.ab.android.event_handler;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jdeffibaugh on 7/25/16 for Optimizely.
 *
 * Tests {@link EventDAO}
 */
@RunWith(AndroidJUnit4.class)
public class EventDAOTest {

    EventDAO eventDAO;
    Logger logger;
    Context context;

    @Before
    public void setupEventDAO() {
        logger = mock(Logger.class);
        context = InstrumentationRegistry.getTargetContext();
        eventDAO = EventDAO.getInstance(context, logger);
    }

    @After
    public void tearDownEventDAO() {
        context.deleteDatabase(EventSQLiteOpenHelper.DB_NAME);
    }

    @Test
    public void storeEvent() {
        Event event = mock(Event.class);
        when(event.toString()).thenReturn("http://www.foo.com");
        when(event.getRequestBody()).thenReturn("bar1=baz1");
        assertTrue(eventDAO.storeEvent(event));
        verify(logger).info("Inserted {} into db", event);
    }

    @Test
    public void getEvents() {
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);
        Event event3 = mock(Event.class);

        when(event1.toString()).thenReturn("http://www.foo1.com");
        when(event1.getRequestBody()).thenReturn("bar1=baz1");
        when(event2.toString()).thenReturn("http://www.foo2.com");
        when(event2.getRequestBody()).thenReturn("bar2=baz2");
        when(event3.toString()).thenReturn("http://www.foo3.com");
        when(event3.getRequestBody()).thenReturn("bar3=baz3");

        assertTrue(eventDAO.storeEvent(event1));
        assertTrue(eventDAO.storeEvent(event2));
        assertTrue(eventDAO.storeEvent(event3));

        List<Pair<Long,Event>> events = eventDAO.getEvents();
        assertTrue(events.size() == 3);

        Pair<Long,Event> pair1 = events.get(0);
        Pair<Long,Event> pair2 = events.get(1);
        Pair<Long,Event> pair3 = events.get(2);

        assertEquals(1, pair1.first.longValue());
        assertEquals(2, pair2.first.longValue());
        assertEquals(3, pair3.first.longValue());

        assertEquals("http://www.foo1.com", pair1.second.toString());
        assertEquals("bar1=baz1", pair1.second.getRequestBody());
        assertEquals("http://www.foo2.com", pair2.second.toString());
        assertEquals("bar2=baz2", pair2.second.getRequestBody());
        assertEquals("http://www.foo3.com", pair3.second.toString());
        assertEquals("bar3=baz3", pair3.second.getRequestBody());

        verify(logger).info("Got events from SQLite");
    }

    @Test
    public void removeEventSuccess() {
        Event event = mock(Event.class);
        when(event.toString()).thenReturn("http://www.foo.com");
        when(event.getRequestBody()).thenReturn("bar=baz");
        assertTrue(eventDAO.storeEvent(event));
        assertTrue(eventDAO.removeEvent(1));
        verify(logger).info("Removed event with id {} from db", 1L);
    }

    @Test
    public void removeEventInvalid() {
        Event event = mock(Event.class);
        when(event.toString()).thenReturn("http://www.foo.com");
        when(event.getRequestBody()).thenReturn("bar=baz");
        assertTrue(eventDAO.storeEvent(event));
        assertFalse(eventDAO.removeEvent(2));
        verify(logger).error("Tried to remove an event id {} that does not exist", 2L);
    }
}
