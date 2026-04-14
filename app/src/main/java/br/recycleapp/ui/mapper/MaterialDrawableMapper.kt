package br.recycleapp.ui.mapper

import androidx.annotation.DrawableRes
import br.recycleapp.R

/**
 * Mapeia o nome do material (conforme gravado no Firestore) para seu drawable individual,
 * exibido no carrossel de materiais do bottom sheet de pontos de coleta.
 */
object MaterialDrawableMapper {

    @DrawableRes
    fun fromName(material: String): Int = when (material.trim()) {
        "Vidro"             -> R.drawable.ic_material_vidro
        "Plástico"          -> R.drawable.ic_material_plastico
        "Papel"             -> R.drawable.ic_material_papel
        "Metal"             -> R.drawable.ic_material_metal
        "Óleo vegetal"      -> R.drawable.ic_material_oleo_vegetal
        "Pilhas e baterias" -> R.drawable.ic_material_pilhas_baterias
        "Eletrônicos"           -> R.drawable.ic_material_eletronicos
        "Bens inservíveis"  -> R.drawable.ic_material_bens_inserviveis
        "Entulho"           -> R.drawable.ic_material_entulho
        "Pneus"             -> R.drawable.ic_material_pneus
        "Galhadas"          -> R.drawable.ic_material_galhadas
        "Orgânico"          -> R.drawable.ic_material_organico
        "Lixo domiciliar"   -> R.drawable.ic_material_lixo_domiciliar
        else                -> R.drawable.ic_material_unknown
    }
}
