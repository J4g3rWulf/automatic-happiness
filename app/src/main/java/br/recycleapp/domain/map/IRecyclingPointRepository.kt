package br.recycleapp.domain.map

/**
 * Contrato para busca de pontos de coleta seletiva próximos ao usuário.
 *
 * A implementação decide se usa cache local ou chama a Places API.
 */
interface IRecyclingPointRepository {

    /**
     * Retorna os pontos de coleta próximos à localização informada.
     *
     * Usa cache se o usuário estiver dentro de [cacheRadiusKm] km
     * da última busca e o cache tiver menos de [cacheDays] dias.
     * Caso contrário, chama a Places API.
     *
     * @param latitude   latitude atual do usuário
     * @param longitude  longitude atual do usuário
     * @return lista de pontos de coleta próximos, ou lista vazia em caso de erro
     */
    suspend fun getNearbyPoints(
        latitude: Double,
        longitude: Double
    ): List<RecyclingPoint>
}