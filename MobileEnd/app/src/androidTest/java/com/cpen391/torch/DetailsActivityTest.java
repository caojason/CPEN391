package com.cpen391.torch;

import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DetailsActivityTest {

    //we have to invoke the test from main menu, with preset stores
    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private <T> Matcher<T> visibleMatch(final Matcher<T> matcher) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof TextView && matcher.matches(item)) {
                    TextView textView = (TextView) item;
                    return textView.getVisibility() == View.VISIBLE;
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("should return only the visible item");
            }
        };
    }

    private <T> Matcher<T> first(final Matcher<T> matcher) {
        return new BaseMatcher<T>() {
            boolean isFirst = true;

            @Override
            public boolean matches(final Object item) {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false;
                    return true;
                }

                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("should return first matching item");
            }
        };
    }

    @Before
    public void gotoDetails() {
        //firstly go to the detailed pages
        //click the browse button
        Espresso.onView(visibleMatch(ViewMatchers.withText(R.string.title_browse)))
                .perform(ViewActions.click());

        //check if the list view is present
        Espresso.onView(first(ViewMatchers.withText(R.string.UI_click_here_for_details)))
                .perform(ViewActions.click());


    }

    @Test
    public void testRequestPermission() {
        //click the request permission button
        Espresso.onView(ViewMatchers.withText("Request permission"))
                .perform(ViewActions.scrollTo())
                .perform(ViewActions.click());

        //check if something is present
        Espresso.onView(ViewMatchers.withText("Request permission"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testSwitchDayChart() {
        //click the request permission button

        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};

        for (String day : days) {
            Espresso.onView(ViewMatchers.withText(day))
                    .perform(ViewActions.scrollTo())
                    .perform(ViewActions.click());
        }

        Assert.assertTrue(true);
    }

}
