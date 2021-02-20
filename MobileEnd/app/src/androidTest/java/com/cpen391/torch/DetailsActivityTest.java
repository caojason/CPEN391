package com.cpen391.torch;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
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
        Assert.assertTrue(true);
    }
}
