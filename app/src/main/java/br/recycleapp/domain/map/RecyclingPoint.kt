package br.recycleapp.domain.map

/**
 * Representa um ponto de coleta seletiva.
 *
 * @param id        identificador único — formato: tipo_operador_local (ex: pev_comlurb_bangu)
 * @param name      nome do local
 * @param subtitle  subtítulo exibido no card — livre por pin (ex: "Ponto de Entrega Voluntária Comlurb")
 * @param address   endereço formatado (vazio → linha oculta no card)
 * @param latitude  latitude
 * @param longitude longitude
 * @param materials materiais aceitos — cada item tem imagem própria no carrossel
 * @param type      tipo do ponto — controla pin no mapa e filtro; não gera texto automático
 * @param schedule  horário de funcionamento (vazio → linha oculta no card)
 * @param benefit   benefício oferecido ao usuário (vazio → linha oculta no card)
 */
data class RecyclingPoint(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val materials: List<String> = emptyList(),
    val type: PointType = PointType.PEV_COMLURB,
    val schedule: String = "",
    val benefit: String = ""
)

/**
 * Tipo do ponto de coleta.
 *
 * Controla exclusivamente:
 *   - a imagem do pin exibida no mapa
 *   - o toggle de filtro correspondente
 *
 * O texto do subtítulo do card vem do campo [RecyclingPoint.subtitle],
 * não do tipo — o que permite subtítulos diferentes para pins do mesmo tipo.
 *
 * [PEV_COMLURB]    Ponto de Entrega Voluntária da Comlurb.
 * [ECOPONTO_COMLURB] Ecoponto da Comlurb.
 * [ECOPONTO_LIGHT] Ecoponto Light Recicla.
 */
enum class PointType {
    PEV_COMLURB,
    ECOPONTO_COMLURB,
    ECOPONTO_LIGHT
}