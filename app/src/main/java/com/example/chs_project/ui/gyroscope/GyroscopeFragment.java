package com.example.chs_project.ui.gyroscope;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.chs_project.databinding.FragmentGyroscopeBinding;


public class GyroscopeFragment extends Fragment {

    private FragmentGyroscopeBinding binding;
    private GyroscopeViewModel gyroscopeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        gyroscopeViewModel = new ViewModelProvider(this).get(GyroscopeViewModel.class);

        binding = FragmentGyroscopeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        gyroscopeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = getContext();
        if(context != null)
            gyroscopeViewModel.startListeningToSensors(getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        gyroscopeViewModel.stopListeningToSensors();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}