package br.recycleapp.data.model

import androidx.compose.ui.graphics.Color

enum class Material(
    val labelPt: String,
    val color: Color,
    val drawableRes: String
) {
    VIDRO(
        labelPt     = "Vidro",
        color       = Color(0xFF43A047),
        drawableRes = "ic_green_trashh"
    ),
    PAPEL(
        labelPt     = "Papel",
        color       = Color(0xFF1E88E5),
        drawableRes = "ic_blue_trashh"
    ),
    PLASTICO(
        labelPt     = "Plástico",
        color       = Color(0xFFE53935),
        drawableRes = "ic_red_trashh"
    ),
    METAL(
        labelPt     = "Metal",
        color       = Color(0xFFFDD835),
        drawableRes = "ic_yellow_trashh"
    )
}