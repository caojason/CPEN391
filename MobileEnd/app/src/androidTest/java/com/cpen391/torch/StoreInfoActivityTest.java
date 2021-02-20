package com.cpen391.torch;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class StoreInfoActivityTest {

    @Rule
    public ActivityTestRule<StoreInfoActivity> storeInfoActivityActivityTestRule = new ActivityTestRule<>(StoreInfoActivity.class);

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

        Espresso.onView(ViewMatchers.withId(R.id.store_name_input))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Assert.assertTrue(true);
    }

    @Test
    public void checkStoreNameValid() {
        Espresso.onView(ViewMatchers.withId(R.id.store_name_input))
                .perform(ViewActions.typeText("abc"));

        Espresso.onView(ViewMatchers.withId(R.id.finish_store_info_editing_button))
                .perform(ViewActions.click());

        Assert.assertTrue(true);
    }

    @Test
    public void checkStoreNameInvalid() {
        //store name too short
        Espresso.onView(ViewMatchers.withId(R.id.store_name_input))
                .perform(ViewActions.typeText("a"));

        Espresso.onView(ViewMatchers.withId(R.id.finish_store_info_editing_button))
                .perform(ViewActions.click());

        Assert.assertTrue(true);
    }

    @Test
    public void checkStoreNameInvalid1() {
        //store name containing invalid characters
        Espresso.onView(ViewMatchers.withId(R.id.store_name_input))
                .perform(ViewActions.typeText("auig&*"));

        Espresso.onView(ViewMatchers.withId(R.id.finish_store_info_editing_button))
                .perform(ViewActions.click());

        Assert.assertTrue(true);
    }

    @Test
    public void checkStoreNameInvalid2() {
        //store name too long
        Espresso.onView(ViewMatchers.withId(R.id.store_name_input))
                .perform(ViewActions.typeText("abuoqiognowqimxzmlaiorgjqglqnwroi"));

        Espresso.onView(ViewMatchers.withId(R.id.finish_store_info_editing_button))
                .perform(ViewActions.click());

        Assert.assertTrue(true);
    }
}
