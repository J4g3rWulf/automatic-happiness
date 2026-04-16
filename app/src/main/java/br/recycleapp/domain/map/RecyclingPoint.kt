package br.recycleapp.domain.map

import androidx.annotation.DrawableRes
import br.recycleapp.R

/**
 * Representa um ponto de coleta seletiva.
 *
 * @param id               identificador único (kebab-case)
 * @param name             nome do local
 * @param subtitle         descrição curta do tipo/programa (ex: "Ecoponto Light Recicla")
 * @param address          endereço formatado: "Rua X, 123 — Bairro"
 * @param reference        referência complementar opcional (ex: "ao lado da UPA")
 * @param latitude         latitude decimal
 * @param longitude        longitude decimal
 * @param materials        materiais aceitos
 * @param type             tipo do ponto — ver [PointType]
 * @param scheduleWeekdays horário dias úteis (ex: "Seg–Sex 08h–17h")
 * @param scheduleSaturday horário sábado (ex: "Sáb 08h–12h")
 * @param scheduleSunday   horário domingo
 * @param benefitsProgram  nome do programa de benefícios (ex: "EcoCLIN")
 * @param benefits         lista de benefícios do programa
 */
data class RecyclingPoint(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val address: String,
    val reference: String = "",
    val latitude: Double,
    val longitude: Double,
    val materials: List<String> = emptyList(),
    val type: PointType = PointType.UNKNOWN,
    val scheduleWeekdays: String = "",
    val scheduleSaturday: String = "",
    val scheduleSunday: String = "",
    val benefitsProgram: String = "",
    val benefits: List<String> = emptyList(),
)

/**
 * Tipo do ponto de coleta.
 *
 * Tipo Fallback:
 * [UNKNOWN]                  resultados genéricos da Places API sem tipo definido.
 *
 * Tipos explícitos (schema v2):
 * [PEV_COMLURB]              Ponto de Entrega Voluntária da Comlurb.
 * [ECOPONTO_COMLURB]         Ecoponto da Comlurb (entulho, bens inservíveis).
 * [ECOPONTO_LIGHT]           Ecoponto Light Recicla (recicláveis + óleo vegetal).
 * [PEV_NITEROI]              PUD — Ponto de Entrega Voluntária de Niterói.
 * [ECOPONTO_NITEROI]         Ecoponto da CLIN (Niterói).
 * [ECOPONTO_SAO_GONCALO]     Ecoponto de São Gonçalo.
 * [ECOPONTO_DUQUE_DE_CAXIAS] Ecoponto de Duque de Caxias.
 * [PEV_ANGRA_DOS_REIS]       PEV de Angra dos Reis.
 * [ECOPONTO_ANGRA_DOS_REIS]  Ecoponto de Angra dos Reis.
 */
enum class PointType {
    // Fallback para resultados genéricos da Places API sem tipo definido
    UNKNOWN,
    // Explícitos
    PEV_COMLURB,
    ECOPONTO_COMLURB,
    ECOPONTO_LIGHT,
    PEV_NITEROI,
    ECOPONTO_NITEROI,
    ECOPONTO_SAO_GONCALO,
    ECOPONTO_DUQUE_DE_CAXIAS,
    PEV_ANGRA_DOS_REIS,
    ECOPONTO_ANGRA_DOS_REIS,
}

/**
 * Retorna o label de exibição no filtro do mapa.
 * Tipos legados compartilham label com seus equivalentes explícitos.
 */
fun PointType.toFilterLabel(): String = when (this) {
    PointType.UNKNOWN                  -> "Outros"
    PointType.PEV_COMLURB              -> "PEVs Comlurb"
    PointType.ECOPONTO_COMLURB         -> "Ecopontos Comlurb"
    PointType.ECOPONTO_LIGHT           -> "Ecopontos Light Recicla"
    PointType.PEV_NITEROI              -> "PEVs Niterói"
    PointType.ECOPONTO_NITEROI         -> "Ecopontos Niterói"
    PointType.ECOPONTO_SAO_GONCALO     -> "Ecopontos São Gonçalo"
    PointType.ECOPONTO_DUQUE_DE_CAXIAS -> "Ecopontos Duque de Caxias"
    PointType.PEV_ANGRA_DOS_REIS       -> "PEVs Angra dos Reis"
    PointType.ECOPONTO_ANGRA_DOS_REIS  -> "Ecopontos Angra dos Reis"
}

/**
 * Retorna o drawable do pin correspondente ao tipo do ponto.
 * Cada município tem seu próprio pin para diferenciação visual no mapa.
 */
@DrawableRes
fun PointType.toPinDrawable(): Int = when (this) {
    PointType.UNKNOWN                  -> R.drawable.pin_unknown
    PointType.PEV_COMLURB              -> R.drawable.pin_pev_comlurb
    PointType.ECOPONTO_COMLURB         -> R.drawable.pin_ecoponto_comlurb
    PointType.ECOPONTO_LIGHT           -> R.drawable.pin_ecoponto_light
    PointType.ECOPONTO_NITEROI         -> R.drawable.pin_ecoponto_clin_niteroi
    PointType.PEV_NITEROI              -> R.drawable.pin_pev_niteroi_puds
    PointType.ECOPONTO_SAO_GONCALO     -> R.drawable.pin_ecoponto_sao_goncalo
    PointType.ECOPONTO_DUQUE_DE_CAXIAS -> R.drawable.pin_ecoponto_duque_de_caxias
    PointType.PEV_ANGRA_DOS_REIS,
    PointType.ECOPONTO_ANGRA_DOS_REIS  -> R.drawable.pin_ecopontos_pevs_angra
}