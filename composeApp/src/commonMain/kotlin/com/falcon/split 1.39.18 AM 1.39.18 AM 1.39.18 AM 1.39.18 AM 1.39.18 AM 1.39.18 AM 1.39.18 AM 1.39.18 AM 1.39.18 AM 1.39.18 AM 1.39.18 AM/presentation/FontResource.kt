package com.falcon.split.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.nunito_bold_1
import split.composeapp.generated.resources.nunito_extrabold_1
import split.composeapp.generated.resources.nunito_extralight
import split.composeapp.generated.resources.nunito_light_1
import split.composeapp.generated.resources.nunito_regular_1
import split.composeapp.generated.resources.nunito_semibold_1

object AppFontFamily {
    @Composable
    fun nunitoFamily() = FontFamily(
        Font(Res.font.nunito_regular_1, weight = FontWeight.Normal),
        Font(Res.font.nunito_light_1, weight = FontWeight.Light),
        Font(Res.font.nunito_bold_1, weight = FontWeight.Bold),
        Font(Res.font.nunito_extrabold_1, weight = FontWeight.ExtraBold),
        Font(Res.font.nunito_extralight, weight = FontWeight.ExtraLight),
        Font(Res.font.nunito_semibold_1, weight = FontWeight.SemiBold)
    )
}