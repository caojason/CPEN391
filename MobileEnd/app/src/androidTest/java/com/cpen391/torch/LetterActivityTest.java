package com.cpen391.torch;

import android.app.Activity;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LetterActivityTest {

    //follow the instructions in MainMenuTest
    @Rule
    public ActivityTestRule<LetterActivity> letterActivityActivityTestRule = new ActivityTestRule<>(LetterActivity.class);
    private Activity currActivity;

    @Before
    public void setup() {
        currActivity = letterActivityActivityTestRule.getActivity();
    }

    @Test
    public void testLetterWritingWithFullInfo() {
        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail)).perform(ViewActions.typeText("a1479839090@gmail.com"));
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withId(R.id.editTextSubject)).perform(ViewActions.typeText("abc"));
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withId(R.id.editTextMessage)).perform(ViewActions.typeText("This is test"));
        Espresso.pressBack();

        Espresso.onView(ViewMatchers.withId(R.id.submitEmailButton)).perform(ViewActions.click());

        //check if something is present
        Espresso.onView(ViewMatchers.withText(R.string.UI_warning))
           .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testBackPressed() {
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withText(R.string.UI_warning))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        //click no should not do anything, we should remain in this letter activity
        Espresso.onView(ViewMatchers.withText(R.string.NO))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testNoSubject() {
        //input nothing, submit directly
        Espresso.onView(ViewMatchers.withId(R.id.submitEmailButton)).perform(ViewActions.click());

        //we should get a warning and remain in this activity
        Espresso.onView(ViewMatchers.withText(R.string.UI_fill_subject))
                .inRoot(RootMatchers.withDecorView(Matchers.not(currActivity.getWindow().getDecorView())))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testNoContent() {
        //only input the subject
        Espresso.onView(ViewMatchers.withId(R.id.editTextSubject)).perform(ViewActions.typeText("abc"));
        Espresso.pressBack();

        Espresso.onView(ViewMatchers.withId(R.id.submitEmailButton)).perform(ViewActions.click());

        //we should get a warning and remain in this activity
        Espresso.onView(ViewMatchers.withText(R.string.UI_fill_justification))
                .inRoot(RootMatchers.withDecorView(Matchers.not(currActivity.getWindow().getDecorView()))) //check the toast message
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testInvalidEmail() {

        //input subject and justification, but no email
        Espresso.onView(ViewMatchers.withId(R.id.editTextSubject)).perform(ViewActions.typeText("abc"));
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withId(R.id.editTextMessage)).perform(ViewActions.typeText("This is test"));
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withId(R.id.submitEmailButton)).perform(ViewActions.click());

        //we should get a warning and remain in this activity
        Espresso.onView(ViewMatchers.withText(R.string.UI_email_invalid))
                .inRoot(RootMatchers.withDecorView(Matchers.not(currActivity.getWindow().getDecorView()))) //check the toast message
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void testInvalidEmail1() {

        //input subject and justification, but invalid email
        Espresso.onView(ViewMatchers.withId(R.id.editTextSubject)).perform(ViewActions.typeText("abc"));
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withId(R.id.editTextMessage)).perform(ViewActions.typeText("This is test"));
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail)).perform(ViewActions.typeText("a"));
        Espresso.pressBack();
        Espresso.onView(ViewMatchers.withId(R.id.submitEmailButton)).perform(ViewActions.click());

        //we should get a warning and remain in this activity
        Espresso.onView(ViewMatchers.withText(R.string.UI_email_invalid))
                .inRoot(RootMatchers.withDecorView(Matchers.not(currActivity.getWindow().getDecorView()))) //check the toast message
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }
}
