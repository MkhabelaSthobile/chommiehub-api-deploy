package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.AccommodationRepository
import com.chommiehub.app.ui.student.adapter.RoomAdapter
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Displays accommodation information, room availability, map information and
 * weather-aware residence information returned through the ChommieHub API.
 */
class AccommodationDetailFragment : Fragment(R.layout.fragment_accommodation_detail) {

    companion object {
        private const val ARG_ACCOMMODATION_ID = "accommodationId"

        fun newInstance(accommodationId: String): AccommodationDetailFragment {
            return AccommodationDetailFragment().apply {
                arguments = Bundle().apply { putString(ARG_ACCOMMODATION_ID, accommodationId) }
            }
        }
    }

    private lateinit var viewModel: AccommodationDetailViewModel
    private lateinit var mapView: MapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().userAgentValue = requireContext().packageName

        val accommodationId = requireArguments().getString(ARG_ACCOMMODATION_ID) ?: throw IllegalStateException("AccommodationDetailFragment requires an accommodationId argument.")

        val progressBar = view.findViewById<ProgressBar>(R.id.progressDetail)
        val tvError = view.findViewById<TextView>(R.id.tvDetailError)
        val scrollDetail = view.findViewById<View>(R.id.scrollDetail)
        val textName = view.findViewById<TextView>(R.id.tvDetailName)
        val textAddress = view.findViewById<TextView>(R.id.tvDetailLocation)
        val textDescription = view.findViewById<TextView>(R.id.tvDetailDescription)
        val textAmenities = view.findViewById<TextView>(R.id.tvDetailAmenities)
        val recyclerRooms = view.findViewById<RecyclerView>(R.id.recyclerRooms)
        val btnApplyNow = view.findViewById<Button>(R.id.btnApplyNow)
        val weatherCard = view.findViewById<LinearLayout>(R.id.weatherCard)
        val tvWeatherSummary = view.findViewById<TextView>(R.id.tvWeatherSummary)
        val tvWeatherAdvisory = view.findViewById<TextView>(R.id.tvWeatherAdvisory)

        recyclerRooms.layoutManager = LinearLayoutManager(requireContext())

        mapView = view.findViewById(R.id.mapAccommodation)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        btnApplyNow.isEnabled = false

        btnApplyNow.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.studentContainer, ApplicationFormFragment.newInstance(accommodationId))
                .addToBackStack(null)
                .commit()
        }

        val repository = AccommodationRepository(ApiClient.api)
        viewModel = ViewModelProvider(this, AccommodationDetailViewModel.Factory(repository))[AccommodationDetailViewModel::class.java]

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                AccommodationDetailUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                    scrollDetail.visibility = View.GONE
                }

                is AccommodationDetailUiState.Success -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                    scrollDetail.visibility = View.VISIBLE
                    btnApplyNow.isEnabled = true

                    textName.text = state.accommodation.name
                    textAddress.text = state.accommodation.address
                    textDescription.text = state.accommodation.description
                    textAmenities.text = state.accommodation.amenities.joinToString("\n")

                    val point = GeoPoint(state.accommodation.latitude, state.accommodation.longitude)

                    mapView.controller.setZoom(15.0)
                    mapView.controller.setCenter(point)

                    val marker = Marker(mapView)
                    marker.position = point
                    marker.title = state.accommodation.name

                    mapView.overlays.clear()
                    mapView.overlays.add(marker)
                    mapView.invalidate()

                    recyclerRooms.adapter = RoomAdapter(state.rooms) { room ->
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.studentContainer, RoomDetailFragment.newInstance(room.id))
                            .addToBackStack(null)
                            .commit()
                    }

                    if (state.weather != null) {
                        weatherCard.visibility = View.VISIBLE
                        tvWeatherSummary.text = "${state.weather.summary} • ${"%.1f".format(state.weather.temperatureC)}°C • Wind ${"%.0f".format(state.weather.windSpeedKph)} km/h"
                        tvWeatherAdvisory.text = state.weather.outdoorMaintenanceAdvisory
                    } else {
                        weatherCard.visibility = View.GONE
                    }
                }

                is AccommodationDetailUiState.Error -> {
                    progressBar.visibility = View.GONE
                    scrollDetail.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                }
            }
        }

        viewModel.loadDetail(accommodationId)
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