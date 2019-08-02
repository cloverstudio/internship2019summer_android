package com.example.summerschoolapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.summerschoolapp.R;

// TODO @Matko
// there should be no hardcoded values like strings
// probably should add an ability to add custom title and error description
public class ErrorDialog extends DialogFragment {

    public static ErrorDialog CreateInstance(String title, String description) {
        return new ErrorDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Error!")
                .setIcon(getResources().getDrawable(R.drawable.log_in_error_icon))
                .setMessage("Nešto je pošlo po krivom")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().finish();
                    }
                });
        return builder.create();
    }
}
