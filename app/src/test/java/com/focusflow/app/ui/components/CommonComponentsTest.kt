package com.focusflow.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.focusflow.app.ui.theme.FocusFlowTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class CommonComponentsTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `empty state renders text and invokes action`() {
        var clicked = false
        compose.setContent {
            FocusFlowTheme {
                EmptyState(
                    icon = Icons.Rounded.CheckCircle,
                    title = "Nothing here",
                    message = "Add your first item.",
                    actionLabel = "Add item",
                    onAction = { clicked = true },
                )
            }
        }

        compose.onNodeWithText("Nothing here").assertIsDisplayed()
        compose.onNodeWithText("Add your first item.").assertIsDisplayed()
        compose.onNodeWithText("Add item").performClick()
        assertTrue(clicked)
    }
}
