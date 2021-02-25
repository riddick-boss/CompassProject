package abandonedstudio.app.compassproject.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import abandonedstudio.app.compassproject.R;
import abandonedstudio.app.compassproject.databinding.DestinationDialogBinding;

public class DestinationDialogFragment extends DialogFragment {

    private OnSetDestinationListener listener;

    private DestinationDialogBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DestinationDialogBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(binding.getRoot())
                .setPositiveButton("SET", (dialogInterface, i) -> {
                    if (!binding.latitudeEditText.getText().toString().isEmpty() && !binding.longitudeEditText.getText().toString().isEmpty()){
                        Double lat = Double.valueOf(binding.latitudeEditText.getText().toString());
                        Double lng = Double.valueOf(binding.longitudeEditText.getText().toString());
                        if (coordinatesCorrect(lat, lng)){
                            listener.onCoordinatesEntered(lat, lng);
                        } else {
                            Toast.makeText(requireContext(), R.string.correct_coordinates, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(requireContext(), "Enter destination coordinates!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private Boolean coordinatesCorrect(Double latitude, Double longitude){
        return latitude <= 90 && latitude >= -90 && longitude <= 180 && longitude >= -180;
    }

    public interface OnSetDestinationListener{
        void onCoordinatesEntered(Double latitude, Double longitude);
    }

    public void setOnSetDestinationListener(OnSetDestinationListener listener){
        this.listener = listener;
    }

}
