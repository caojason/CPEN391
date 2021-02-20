package com.cpen391.torch;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class LetterActivityTest {

    //follow the instructions in MainMenuTest
    @Rule
    public ActivityTestRule<LetterActivity> letterActivityActivityTestRule = new ActivityTestRule<>(LetterActivity.class);

    @Test
    public void testSwitchTabs() {
        Espresso.onView(ViewMatchers.withText("Your Email")).perform(ViewActions.typeText("a1479839090@gmail.com"));
        Espresso.onView(ViewMatchers.withText("Subject")).perform(ViewActions.typeText("abc"));
        Espresso.onView(ViewMatchers.withText("Request Message")).perform(ViewActions.typeText("This is test"));
        //click the browse button
        Espresso.onView(ViewMatchers.withText("FINISH")).perform(ViewActions.click());

        //check if something is present
        Espresso.onView(ViewMatchers.withText("Warning"))
           .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }
}
