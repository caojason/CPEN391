package com.cpen391.torch;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class DetailsActivityTest {

    //follow the instructions in MainMenuTest
    @Rule
    public ActivityTestRule<StoreInfoActivity> detailsActivityActivityTestRule = new ActivityTestRule<>(StoreInfoActivity.class);

    @Test
    public void testSwitchTabs() {
        //click the browse button
        Espresso.onView(ViewMatchers.withText("Request permission")).perform(ViewActions.click());

        //check if something is present
        Espresso.onView(ViewMatchers.withText("Request permission"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));


    }
}
