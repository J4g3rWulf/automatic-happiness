package br.recycleapp.domain.map

/**
 * Representa um ponto de coleta seletiva.
 *
 * @param id        identificador único (kebab-case)
 * @param name      nome do local
 * @param address   endereço formatado para exibição
 * @param latitude  latitude decimal
 * @param longitude longitude decimal
 * @param materials materiais aceitos
 * @param type      tipo do ponto — ver [PointType]
 */
data class RecyclingPoint(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val materials: List<String> = emptyList(),
    val type: PointType = PointType.PEV
)

/**
 * Tipo do ponto de coleta.
 *
 * Tipos legados mantidos para compatibilidade:
 * [PEV]     — fallback genérico para resultados da Places API.
 * [ECOPONTO] — será removido na refatoração do BottomSheet.
 *
 * Tipos explícitos (schema v2):
 * [PEV_COMLURB]             Ponto de Entrega Voluntária da Comlurb.
 * [ECOPONTO_COMLURB]        Ecoponto da Comlurb (entulho, bens inservíveis).
 * [ECOPONTO_LIGHT]          Ecoponto Light Recicla (recicláveis + óleo vegetal).
 * [PEV_NITEROI]             PUD — Ponto de Entrega Voluntária de Niterói.
 * [ECOPONTO_NITEROI]        Ecoponto da CLIN (Niterói).
 * [ECOPONTO_SAO_GONCALO]    Ecoponto de São Gonçalo.
 * [ECOPONTO_DUQUE_DE_CAXIAS] Ecoponto de Duque de Caxias.
 * [PEV_ANGRA_DOS_REIS]      PEV de Angra dos Reis.
 * [ECOPONTO_ANGRA_DOS_REIS] Ecoponto de Angra dos Reis.
 */
enum class PointType {
    // Legados
    PEV,
    ECOPONTO,
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