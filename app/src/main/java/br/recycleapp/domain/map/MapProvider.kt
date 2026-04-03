package br.recycleapp.domain.map

/**
 * Representa o provedor de mapa ativo no momento.
 *
 * [GOOGLE] mapa principal - requer API Key e conexão com os servidores do Google.
 * [OSM]    mapa reserva   - OpenStreetMap, sem API Key, sem custos.
 */
enum class MapProvider {
    GOOGLE,
    OSM
}