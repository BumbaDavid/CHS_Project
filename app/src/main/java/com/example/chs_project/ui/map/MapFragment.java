package com.example.chs_project.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.chs_project.R;
import com.example.chs_project.databinding.FragmentMapBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;

    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private RouteViewModel routeViewModel;

    private LatLng startLocationLng;
    private LatLng endLocationLng;
    private GoogleMap googleMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MapViewModel mapViewModel =
                new ViewModelProvider(this).get(MapViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            if (fineLocationGranted != null && fineLocationGranted) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(getContext(),
                        "Location permission is needed to use this feature. Please allow access to location services to proceed.", Toast.LENGTH_LONG)
                        .show();

            }
        });


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        AutocompleteSupportFragment startLocationAutocomplete = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.startLocation);

        AutocompleteSupportFragment endLocationAutocomplete = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.endLocation);

        if (startLocationAutocomplete !=null && endLocationAutocomplete != null) {
            endLocationAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));
            startLocationAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));

            startLocationAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    startLocationLng = place.getLatLng();
                }

                @Override
                public void onError(@NonNull Status status) {
                    // Handle the error
                }
            });
            endLocationAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    endLocationLng = place.getLatLng();
                }

                @Override
                public void onError(@NonNull Status status) {
                    // Handle the error
                }
            });
        }

        routeViewModel = new ViewModelProvider(this).get(RouteViewModel.class);
        routeViewModel.getRoutePoints().observe(getViewLifecycleOwner(), this::drawRouteOnMap);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.calculateRouteButton.setOnClickListener(v-> {
            if(startLocationLng != null && endLocationLng != null) {
                routeViewModel.fetchRoute(startLocationLng,endLocationLng);
                moveCameraToLocation(startLocationLng);
            } else {
                Toast.makeText(getContext(), "Please select both start and end locations", Toast.LENGTH_LONG).show();
            }




        });

        binding.currentLocationButton.setOnClickListener(v -> fetchCurrentLocation());

        Button startRouteButton = binding.startRouteButton;
        startRouteButton.setOnClickListener(v -> {
            startNavigation();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

    }

    private void moveCameraToLocation(LatLng location) {
        if(googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15));
        }
    }
    private void drawRouteOnMap(List<LatLng> points) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(points);
        polylineOptions.width(12).color(Color.BLUE).geodesic(true);
        googleMap.addPolyline(polylineOptions);
    }

    private void fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            geocoder.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 1, new Geocoder.GeocodeListener() {
                                @Override
                                public void onGeocode(@NonNull List<Address> addresses) {
                                    if(!addresses.isEmpty()) {
                                        Address address = addresses.get(0);
                                        if(getActivity() != null){
                                            getActivity().runOnUiThread(() ->{
                                                AutocompleteSupportFragment startLocationAutocomplete =
                                                        (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.startLocation);
                                                if(startLocationAutocomplete != null) {
                                                    startLocationAutocomplete.setText(address.getAddressLine(0));
                                                    startLocationLng = currentLatLng;
                                                }
                                            });
                                        }
                                    }
                                }
                            });

                        }

                    });
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            });
        }
    }

    private void startNavigation() {
        if (startLocationLng != null && endLocationLng != null) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + endLocationLng.latitude + "," + endLocationLng.longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (getActivity() != null && mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
            }

        }

    }
}

