package com.example.juntoscontradengue.ui.home;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.juntoscontradengue.databinding.FragmentAdminBinding;

public class AdminFragment extends Fragment {

    private FragmentAdminBinding bindingAdmin;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        bindingAdmin = FragmentAdminBinding.inflate( inflater, container, false );


        return bindingAdmin.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bindingAdmin = null;
    }
}
