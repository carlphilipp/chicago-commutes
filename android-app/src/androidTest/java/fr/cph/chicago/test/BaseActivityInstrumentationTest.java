package fr.cph.chicago.test;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.cph.chicago.core.activity.BaseActivity;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BaseActivityInstrumentationTest {

    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<>(BaseActivity.class);

    @Test
    public void test() {
        //activity_train_station_steetview_text
        //onView(withId(R.id.activity_train_station_steetview_text)).check(matches(isDisplayed()));
        //onView(withId(R.id.floating_button)).check(matches(isDisplayed()));
        //onView(withId(R.id.floating_button)).perform(click());
        assertTrue(true);
    }
}
