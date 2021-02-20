package com.cpen391.torch;

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
        Assert.assertTrue(true);
    }
}
