package br.recycleapp.ui.components

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import br.recycleapp.domain.map.RecyclingPoint

/**
 * Wrapper de [RecyclingPoint] compatível com a API de clustering do Google Maps.
 *
 * O [] composable requer que os itens implementem [ClusterItem],
 * que expõe posição, título e snippet para o algoritmo de agrupamento.
 */
data class RecyclingPointClusterItem(
    val point: RecyclingPoint
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(point.latitude, point.longitude)
    override fun getTitle(): String    = point.name
    override fun getSnippet(): String  = point.address
    override fun getZIndex(): Float    = 0f
}