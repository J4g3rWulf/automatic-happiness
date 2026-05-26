package br.recycleapp.domain.map

import br.recycleapp.data.map.MapProvider

/**
 * Contrato para verificar qual provedor de mapa está disponível.
 *
 * A implementação decide se o Google Maps está acessível
 * ou se o app deve recorrer ao mapa reserva (OSM).
 */
interface MapAvailabilityCheckerContract {

    /**
     * Retorna o provedor disponível no momento.
     * Pode usar cache interno para evitar chamadas repetidas à API.
     */
    suspend fun getAvailableProvider(): MapProvider
}