package com.cpen391.torch;

import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class MainMenuTest {

    //test all three main menu fragments in this file

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testSwitchTabs() {
        //click the browse button
       Espresso.onView(visibleMatch(ViewMatchers.withText(R.string.title_browse)))
               .perform(ViewActions.click());

        //check if the list view is present
       Espresso.onView(first(ViewMatchers.withText(R.string.UI_click_here_for_details)))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

       Espresso.onView(visibleMatch(ViewMatchers.withText(R.string.title_favorite))).perform(ViewActions.click());

       Assert.assertTrue(true);
    }

    @Test
    public void testBrowseFragmentSwitchMapView() {
        //go to the browse fragment
        Espresso.onView(visibleMatch(ViewMatchers.withText(R.string.title_browse)))
                .perform(ViewActions.click());

        //check that initially the switch fab has map as background
        Espresso.onView(ViewMatchers.withId(R.id.switch_map_fab))
                .check(ViewAssertions.matches(ViewMatchers.isClickable()));

        //click on the fab
        Espresso.onView(ViewMatchers.withId(R.id.switch_map_fab))
                .perform(ViewActions.click());

        //check that map is presented and the switch fav has list view as background
        Espresso.onView(ViewMatchers.withId(R.id.map))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //click the button again and check that it has went back to list view
        Espresso.onView(ViewMatchers.withId(R.id.switch_map_fab))
                .perform(ViewActions.click());
        Espresso.onView(first(ViewMatchers.withText(R.string.UI_click_here_for_details)))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

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

    @Test
    public void testBrowseFragmentGoToDetails() {
        //go to the browse fragment
        Espresso.onView(visibleMatch(ViewMatchers.withText(R.string.title_browse))).perform(ViewActions.click());

        //click on the first detail
        Espresso.onView(first(ViewMatchers.withText(R.string.UI_click_here_for_details)))
                .perform(ViewActions.click());

        //check that we are in details view
        Espresso.onView(ViewMatchers.withId(R.id.details_scroll_view))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

}
