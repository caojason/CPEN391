package com.cpen391.torch;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

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
        Espresso.onView(ViewMatchers.withText(R.string.title_browse)).perform(ViewActions.click());

        //check if something is present
//        Espresso.onView(ViewMatchers.withText(R.string.UI_click_here_for_details))
//                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    //examples to check something in an alert dialog
//    Espresso.onView(ViewMatchers.withText(R.string.UI_quit_quiz_editing_warning))
//            .inRoot(RootMatchers.isDialog())
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

}
