package abandonedstudio.app.compassproject.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
                        Float lat = Float.valueOf(binding.latitudeEditText.getText().toString());
                        Float lng = Float.valueOf(binding.longitudeEditText.getText().toString());
                        listener.onCoordinatesEntered(lat, lng);
                        Toast.makeText(requireContext(), "Destination set", Toast.LENGTH_SHORT).show();
//                        getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_RQ);
                    }
                    else{
                        Toast.makeText(requireContext(), "Enter destination coordinates!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface OnSetDestinationListener{
        void onCoordinatesEntered(Float latitude, Float longitude);
    }

    public void setOnSetDestinationListener(OnSetDestinationListener listener){
        this.listener = listener;
    }

}
