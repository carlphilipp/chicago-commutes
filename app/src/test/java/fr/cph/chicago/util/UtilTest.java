package fr.cph.chicago.util;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

public class UtilTest {

    @Test
    public void testGenerateViewId() {
        int actual = Util.generateViewId();
        assertNotEquals(0, actual);
    }
}
