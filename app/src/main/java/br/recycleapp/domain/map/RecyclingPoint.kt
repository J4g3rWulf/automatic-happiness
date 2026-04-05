package br.recycleapp.domain.map

/**
 * Representa um ponto de coleta seletiva retornado pela Places API
 * ou pelo cache local.
 *
 * @param id         identificador único do lugar (Google Place ID)
 * @param name       nome do estabelecimento
 * @param address    endereço formatado
 * @param latitude   latitude da localização
 * @param longitude  longitude da localização
 * @param materials  lista de materiais aceitos no local (pode ser vazia
 *                   se a API não retornar essa informação)
 */
data class RecyclingPoint(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val materials: List<String> = emptyList()
)