package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.AccommodationRepository
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Displays active ChommieHub accommodations together on an interactive map.
 *
 * Selecting a map marker opens the corresponding accommodation detail screen,
 * allowing map discovery to connect directly into the normal Student journey.
 */
class AccommodationMapFragment : Fragment(R.layout.fragment_accommodation_map) {

    private lateinit var viewModel: AccommodationMapViewModel
    private lateinit var mapView: MapView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var tvCount: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().userAgentValue = requireContext().packageName

        mapView = view.findViewById(R.id.mapAccommodationDiscovery)
        progressBar = view.findViewById(R.id.progressAccommodationMap)
        tvError = view.findViewById(R.id.tvAccommodationMapError)
        tvEmpty = view.findViewById(R.id.tvAccommodationMapEmpty)
        tvCount = view.findViewById(R.id.tvAccommodationMapCount)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val repository = AccommodationRepository(ApiClient.api)
        viewModel = ViewModelProvider(this, AccommodationMapViewModel.Factory(repository))[AccommodationMapViewModel::class.java]

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                AccommodationMapUiState.Loading -> showLoading()
                is AccommodationMapUiState.Success -> showAccommodations(state.accommodations)
                is AccommodationMapUiState.Error -> showError(state.message)
            }
        }

        viewModel.loadAccommodations()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        tvEmpty.visibility = View.GONE
    }

    private fun showAccommodations(accommodations: List<AccommodationResponse>) {
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE

        mapView.overlays.clear()

        if (accommodations.isEmpty()) {
            tvCount.text = "0 residences"
            tvEmpty.visibility = View.VISIBLE
            return
        }

        tvEmpty.visibility = View.GONE
        tvCount.text = "${accommodations.size} residence(s) available"

        accommodations.forEach { accommodation ->
            val point = GeoPoint(accommodation.latitude, accommodation.longitude)
            val marker = Marker(mapView)

            marker.position = point
            marker.title = accommodation.name
            marker.snippet = "${accommodation.address}\nTap to view details"

            marker.setOnMarkerClickListener { selectedMarker, _ ->
                selectedMarker.showInfoWindow()

                parentFragmentManager.beginTransaction()
                    .replace(R.id.studentContainer, AccommodationDetailFragment.newInstance(accommodation.id))
                    .addToBackStack(null)
                    .commit()

                true
            }

            mapView.overlays.add(marker)
        }

        val averageLatitude = accommodations.map { it.latitude }.average()
        val averageLongitude = accommodations.map { it.longitude }.average()

        mapView.controller.setCenter(GeoPoint(averageLatitude, averageLongitude))
        mapView.controller.setZoom(if (accommodations.size == 1) 15.0 else 11.0)
        mapView.invalidate()
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }
}