package fr.cph.chicago.util;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

// FIXME remove ignore
@Ignore
public class UtilTest {

    @Test
    public void testGenerateViewId() {
        // When
        int actual = Util.INSTANCE.generateViewId();

        // Then
        assertNotEquals(0, actual);
    }

    @Test
    public void testGenerateViewIdUnique() {
        // When
        int actual1 = Util.INSTANCE.generateViewId();
        int actual2 = Util.INSTANCE.generateViewId();

        // Then
        assertNotEquals(actual1, actual2);
    }
}
