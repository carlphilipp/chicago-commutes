package fr.cph.chicago.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class TwoErrors {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {false, false, false, false},
            {true, false, false, false},
            {false, true, false, false},
            {false, false, true, false},
            {true, true, false, true},
            {false, true, true, true},
            {true, false, true, true},
            {true, true, true, true},
        });
    }

    private boolean trainError;
    private boolean busError;
    private boolean bikeError;
    private boolean expected;

    public TwoErrors(boolean trainError, boolean busError, boolean bikeError, boolean expected) {
        this.trainError = trainError;
        this.busError = busError;
        this.bikeError = bikeError;
        this.expected = expected;
    }

    @Test
    public void test() {
        assertEquals(expected, Util.INSTANCE.isAtLeastTwoErrors(trainError, busError, bikeError));
    }
}
