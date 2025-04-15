package com.falcon.split.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

 open class SplitDimensions {
    open val dp0 @Composable get() = 0.dp
    open val dp0_point_5 @Composable get() = 0.5.dp
    open val dp1 @Composable get() = 1.dp
    open val dp2 @Composable get() = 2.dp
    open val dp3 @Composable get() = 3.dp
    open val dp4 @Composable get() = 4.dp
    open val dp5 @Composable get() = 5.dp
    open val dp6 @Composable get() = 6.dp
    open val dp7 @Composable get() = 7.dp
    open val dp8 @Composable get() = 8.dp
    open val dp9 @Composable get() = 9.dp
    open val dp10 @Composable get() = 10.dp
    open val dp11 @Composable get() = 11.dp
    open val dp12 @Composable get() = 12.dp
    open val dp13 @Composable get() = 13.dp
    open val dp14 @Composable get() = 14.dp
    open val dp15 @Composable get() = 15.dp
    open val dp16 @Composable get() = 16.dp
    open val dp17 @Composable get() = 17.dp
    open val dp18 @Composable get() = 18.dp
    open val dp19 @Composable get() = 19.dp
    open val dp20 @Composable get() = 20.dp
    open val dp21 @Composable get() = 21.dp
    open val dp22 @Composable get() = 22.dp
    open val dp23 @Composable get() = 23.dp
    open val dp24 @Composable get() = 24.dp
    open val dp25 @Composable get() = 25.dp
    open val dp26 @Composable get() = 26.dp
    open val dp27 @Composable get() = 27.dp
    open val dp28 @Composable get() = 28.dp
    open val dp29 @Composable get() = 29.dp
    open val dp30 @Composable get() = 30.dp
    open val dp32 @Composable get() = 32.dp
    open val dp34 @Composable get() = 34.dp
    open val dp35 @Composable get() = 35.dp
    open val dp36 @Composable get() = 36.dp
    open val dp40 @Composable get() = 40.dp
    open val dp42 @Composable get() = 42.dp
    open val dp44 @Composable get() = 44.dp
    open val dp45 @Composable get() = 45.dp
    open val dp48 @Composable get() = 48.dp
    open val dp50 @Composable get() = 50.dp
    open val dp52 @Composable get() = 52.dp
    open val dp54 @Composable get() = 54.dp
    open val dp56 @Composable get() = 56.dp
    open val dp60 @Composable get() = 60.dp
    open val dp64 @Composable get() = 64.dp
    open val dp68 @Composable get() = 68.dp
    open val dp78 @Composable get() = 78.dp
    open val dp80 @Composable get() = 80.dp
    open val dp70 @Composable get() = 70.dp
    open val dp75 @Composable get() = 75.dp
    open val dp84 @Composable get() = 84.dp
    open val dp90 @Composable get() = 90.dp
    open val dp92 @Composable get() = 92.dp
    open val dp96 @Composable get() = 96.dp
    open val dp100 @Composable get() = 100.dp
    open val dp108 @Composable get() = 108.dp
    open val dp112 @Composable get() = 112.dp
    open val dp120 @Composable get() = 120.dp
    open val dp130 @Composable get() = 130.dp
    open val dp136 @Composable get() = 136.dp
    open val dp140 @Composable get() = 140.dp
    open val dp148 @Composable get() = 148.dp
    open val dp160 @Composable get() = 160.dp
    open val dp165 @Composable get() = 165.dp
    open val dp150 @Composable get() = 150.dp
    open val dp156 @Composable get() = 156.dp
    open val dp158 @Composable get() = 158.dp
    open val dp162 @Composable get() = 162.dp
    open val dp180 @Composable get() = 180.dp
    open val dp190 @Composable get() = 190.dp
    open val dp200 @Composable get() = 200.dp
    open val dp210 @Composable get() = 210.dp
    open val dp228 @Composable get() = 228.dp
    open val dp230 @Composable get() = 230.dp
    open val dp236 @Composable get() = 236.dp
    open val dp240 @Composable get() = 240.dp
    open val dp248 @Composable get() = 248.dp
    open val dp264 @Composable get() = 264.dp
    open val dp270 @Composable get() = 270.dp
    open val dp280 @Composable get() = 280.dp
    open val dp268 @Composable get() = 268.dp
    open val dp300 @Composable get() = 300.dp
    open val dp330 @Composable get() = 330.dp
    open val dp352 @Composable get() = 352.dp
    open val dp360 @Composable get() = 360.dp
    open val dp390 @Composable get() = 390.dp
    open val dp400 @Composable get() = 400.dp
    open val dp450 @Composable get()=450.dp
    open val dp475 @Composable get() = 475.dp
    open val dp500 @Composable get() = 500.dp
}

 object SplitDimens : SplitDimensions()

 object Sw600Dimens : SplitDimensions()

 val LocalDimensions = staticCompositionLocalOf<SplitDimensions> { SplitDimens }

val lDimens @Composable get() = LocalDimensions.current


/*
   USAGE in Composables:
    val dp = lDimens.dp5
    OR
    val dp = LocalDimensions.current.dp5
 */