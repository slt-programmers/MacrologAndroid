package com.csl.macrologandroid;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.csl.macrologandroid.ui.login.LoginActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class LoginInstrumentedTest {


    @Before
    public void setup() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
    }

    @Test
    public void loginLayoutTest() {
        onView(withId(R.id.login_layout)).check(matches(isDisplayed()));
    }

    @Test
    public void enterCredentialsTest() {
        ViewInteraction userTextField = onView(withId(R.id.user_field));
        ViewInteraction passwordTextField = onView(withId(R.id.password_field));
        ViewInteraction loginButton = onView(withId(R.id.login_button));
        ViewInteraction userError = onView(withId(R.id.user_email_error));
        ViewInteraction passwordError = onView(withId(R.id.password_error));

        // fields are displayed, errors are not
        userTextField.check(matches(isDisplayed()));
        passwordTextField.check(matches(isDisplayed()));
        loginButton.check(matches(isDisplayed()));
        userError.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
        passwordError.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

        // errors are displayed after clicking button without entering credentials
        loginButton.perform(ViewActions.click());
        userError.check(matches(isDisplayed()));
        passwordError.check(matches(isDisplayed()));

        // credentials are entered, button is clicked
        userTextField.perform(ViewActions.typeText("CarmenScholte"));
        passwordTextField.perform(ViewActions.typeText("thisIsAPassword123"));
        loginButton.perform(ViewActions.click());

        // TODO mock http call to not get 401
//        onView(withId(R.id.main_layout)).check(matches(isDisplayed()));
    }
}