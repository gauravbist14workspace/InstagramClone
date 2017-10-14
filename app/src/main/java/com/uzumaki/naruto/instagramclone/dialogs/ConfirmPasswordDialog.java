package com.uzumaki.naruto.instagramclone.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.uzumaki.naruto.instagramclone.R;
import com.uzumaki.naruto.instagramclone.interfaces.OnConfirmPasswordListener;

/**
 * Created by Gaurav Bist on 27-07-2017.
 */

public class ConfirmPasswordDialog extends DialogFragment {
    private static final String TAG = "ConfirmPasswordDialog";

    TextView confirm_dialog, cancel_dialog;
    EditText confirm_password;

    private OnConfirmPasswordListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);

        bindViews(view);
        init();

        return view;
    }

    private void bindViews(View view) {
        confirm_password = (EditText) view.findViewById(R.id.confirm_password);

        confirm_dialog = (TextView) view.findViewById(R.id.dialog_confirm);
        cancel_dialog = (TextView) view.findViewById(R.id.dialog_cancel);
    }

    private void init() {
        confirm_dialog.setOnClickListener(onClickListener);
        cancel_dialog.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.dialog_cancel) {
                getDialog().dismiss();
            } else if (id == R.id.dialog_confirm) {
                String password = confirm_password.getText().toString();

                if (!password.equals("")) {
                    listener.onConfirm(password);
                    getDialog().dismiss();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        Activity activity = null;
//        if (context instanceof Activity)
//            activity = (Activity) context;
//
//        try {
//            listener = (OnConfirmPasswordListener) activity;
//        } catch (ClassCastException e) {
//
//        }

        try {
            listener = (OnConfirmPasswordListener) getTargetFragment();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException " + e.getMessage());
        }
    }
}
