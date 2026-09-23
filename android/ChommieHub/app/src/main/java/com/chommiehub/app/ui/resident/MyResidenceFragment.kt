package com.chommiehub.app.ui.resident

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.AccommodationRepository
import com.chommiehub.app.data.repository.ResidentServicesRepository
import com.chommiehub.app.ui.student.CurrentStudent
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Displays the resident's assigned accommodation and room information.
 */
class MyResidenceFragment : Fragment(R.layout.fragment_my_residence) {

    private lateinit var viewModel: ResidentHomeViewModel
    private lateinit var mapView: MapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Configuration.getInstance().userAgentValue = requireContext().packageName

        val progressBar = view.findViewById<ProgressBar>(R.id.progressMyResidence)
        val content = view.findViewById<View>(R.id.myResidenceContent)
        val tvError = view.findViewById<TextView>(R.id.tvMyResidenceError)
        val tvName = view.findViewById<TextView>(R.id.tvMyResidenceName)
        val tvAddress = view.findViewById<TextView>(R.id.tvMyResidenceAddress)
        val tvDescription = view.findViewById<TextView>(R.id.tvMyResidenceDescription)
        val tvPhone = view.findViewById<TextView>(R.id.tvMyResidencePhone)
        val tvAmenities = view.findViewById<TextView>(R.id.tvMyResidenceAmenities)
        val tvRoom = view.findViewById<TextView>(R.id.tvMyResidenceRoom)
        val tvPrice = view.findViewById<TextView>(R.id.tvMyResidencePrice)
        val tvCapacity = view.findViewById<TextView>(R.id.tvMyResidenceCapacity)

        mapView = view.findViewById(R.id.mapMyResidence)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val residentRepository = ResidentServicesRepository(ApiClient.api)
        val accommodationRepository = AccommodationRepository(ApiClient.api)

        viewModel = ViewModelProvider(
            this,
            ResidentHomeViewModel.Factory(residentRepository, accommodationRepository)
        )[ResidentHomeViewModel::class.java]

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ResidentHomeUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    content.visibility = View.GONE
                    tvError.visibility = View.GONE
                }

                is ResidentHomeUiState.Success -> {
                    progressBar.visibility = View.GONE
                    content.visibility = View.VISIBLE
                    tvError.visibility = View.GONE

                    tvName.text = state.accommodation.name
                    tvAddress.text = state.accommodation.address
                    tvDescription.text = state.accommodation.description
                    tvPhone.text = state.accommodation.phoneNumber
                    tvAmenities.text = state.accommodation.amenities.joinToString("\n") { amenity -> "• $amenity" }
                    tvRoom.text = state.room.roomType
                    tvPrice.text = "R${state.room.price.toInt()} / month"
                    tvCapacity.text = "Capacity: ${state.room.capacity}"

                    val location = GeoPoint(state.accommodation.latitude, state.accommodation.longitude)

                    mapView.controller.setZoom(15.0)
                    mapView.controller.setCenter(location)

                    val marker = Marker(mapView)
                    marker.position = location
                    marker.title = state.accommodation.name

                    mapView.overlays.clear()
                    mapView.overlays.add(marker)
                    mapView.invalidate()
                }

                is ResidentHomeUiState.Error -> {
                    progressBar.visibility = View.GONE
                    content.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                }
            }
        }

        viewModel.load(CurrentStudent.ID)
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