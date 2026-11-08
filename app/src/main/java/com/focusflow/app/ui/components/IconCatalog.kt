package com.focusflow.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SportsBasketball
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.ui.graphics.vector.ImageVector

object IconCatalog {
    private val map: Map<String, ImageVector> = mapOf(
        "code" to Icons.Rounded.Code,
        "translate" to Icons.Rounded.Translate,
        "school" to Icons.Rounded.School,
        "psychology" to Icons.Rounded.Psychology,
        "fitness" to Icons.Rounded.FitnessCenter,
        "palette" to Icons.Rounded.Palette,
        "category" to Icons.Rounded.Category,
        "water" to Icons.Rounded.WaterDrop,
        "book" to Icons.Rounded.MenuBook,
        "sleep" to Icons.Rounded.Bedtime,
        "walk" to Icons.Rounded.DirectionsWalk,
        "meditation" to Icons.Rounded.SelfImprovement,
        "coffee" to Icons.Rounded.Coffee,
        "music" to Icons.Rounded.MusicNote,
        "brush" to Icons.Rounded.Brush,
        "savings" to Icons.Rounded.Savings,
        "food" to Icons.Rounded.Restaurant,
        "sport" to Icons.Rounded.SportsBasketball,
        "plants" to Icons.Rounded.LocalFlorist,
        "pets" to Icons.Rounded.Pets,
        "moon" to Icons.Rounded.DarkMode,
        "leaf" to Icons.Rounded.Eco,
        "bolt" to Icons.Rounded.Bolt,
        "wave" to Icons.Rounded.Waves,
        "star" to Icons.Rounded.Star,
        "cat" to Icons.Rounded.Pets,
    )

    fun icon(key: String): ImageVector = map[key] ?: Icons.Rounded.Category

    val categoryChoices: List<String> = listOf(
        "code", "translate", "school", "psychology", "fitness", "palette",
        "savings", "music", "book", "brush", "category",
    )

    val habitChoices: List<String> = listOf(
        "water", "book", "sleep", "walk", "meditation", "fitness",
        "coffee", "food", "sport", "plants", "pets", "music", "brush", "savings",
    )

    val avatarChoices: List<String> = listOf("moon", "leaf", "bolt", "wave", "star", "cat")
}
