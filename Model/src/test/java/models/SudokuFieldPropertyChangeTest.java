package models;

import org.junit.jupiter.api.Test;
import sudoku.model.models.SudokuField;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SudokuFieldPropertyChangeTest {

    @Test
    void setValue_shouldNotifyRegisteredListener() {
        SudokuField field = new SudokuField(3);
        List<PropertyChangeEvent> events = new ArrayList<>();

        PropertyChangeListener listener = events::add;
        field.addPropertyChangeListener(listener);

        field.setValue(7);

        assertEquals(1, events.size(), "Exactly one property change event should be fired");

        PropertyChangeEvent event = events.get(0);
        assertEquals("value-changed", event.getPropertyName());
        assertEquals(3, event.getOldValue());
        assertEquals(7, event.getNewValue());
        assertSame(field, event.getSource());
    }

    @Test
    void removePropertyChangeListener_shouldStopNotifications() {
        SudokuField field = new SudokuField(1);
        List<PropertyChangeEvent> events = new ArrayList<>();

        PropertyChangeListener listener = events::add;
        field.addPropertyChangeListener(listener);
        field.removePropertyChangeListener(listener);

        field.setValue(9);

        assertTrue(events.isEmpty(), "No event should be fired after listener removal");
    }
}