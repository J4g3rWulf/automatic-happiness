package br.recycleapp.domain.map

/**
 * Representa um ponto de coleta seletiva.
 *
 * @param id        identificador único
 * @param name      nome do local
 * @param address   endereço formatado
 * @param latitude  latitude
 * @param longitude longitude
 * @param materials materiais aceitos (vazio = recicláveis em geral)
 * @param type      tipo do ponto — [PointType.PEV] ou [PointType.ECOPONTO]
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
 * [PEV]      Ponto de Entrega Voluntária — aceita recicláveis (plástico, vidro, papel, metal).
 * [ECOPONTO] Ecoponto da Comlurb — aceita lixo domiciliar, entulho e bens inservíveis.
 */
enum class PointType {
    PEV,
    ECOPONTO
}