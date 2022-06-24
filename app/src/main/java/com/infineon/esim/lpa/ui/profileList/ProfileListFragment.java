/*
 * THE SOURCE CODE AND ITS RELATED DOCUMENTATION IS PROVIDED "AS IS". INFINEON
 * TECHNOLOGIES MAKES NO OTHER WARRANTY OF ANY KIND,WHETHER EXPRESS,IMPLIED OR,
 * STATUTORY AND DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF MERCHANTABILITY,
 * SATISFACTORY QUALITY, NON INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * THE SOURCE CODE AND DOCUMENTATION MAY INCLUDE ERRORS. INFINEON TECHNOLOGIES
 * RESERVES THE RIGHT TO INCORPORATE MODIFICATIONS TO THE SOURCE CODE IN LATER
 * REVISIONS OF IT, AND TO MAKE IMPROVEMENTS OR CHANGES IN THE DOCUMENTATION OR
 * THE PRODUCTS OR TECHNOLOGIES DESCRIBED THEREIN AT ANY TIME.
 *
 * INFINEON TECHNOLOGIES SHALL NOT BE LIABLE FOR ANY DIRECT, INDIRECT OR
 * CONSEQUENTIAL DAMAGE OR LIABILITY ARISING FROM YOUR USE OF THE SOURCE CODE OR
 * ANY DOCUMENTATION, INCLUDING BUT NOT LIMITED TO, LOST REVENUES, DATA OR
 * PROFITS, DAMAGES OF ANY SPECIAL, INCIDENTAL OR CONSEQUENTIAL NATURE, PUNITIVE
 * DAMAGES, LOSS OF PROPERTY OR LOSS OF PROFITS ARISING OUT OF OR IN CONNECTION
 * WITH THIS AGREEMENT, OR BEING UNUSABLE, EVEN IF ADVISED OF THE POSSIBILITY OR
 * PROBABILITY OF SUCH DAMAGES AND WHETHER A CLAIM FOR SUCH DAMAGE IS BASED UPON
 * WARRANTY, CONTRACT, TORT, NEGLIGENCE OR OTHERWISE.
 *
 * (C)Copyright INFINEON TECHNOLOGIES All rights reserved
 */

package com.infineon.esim.lpa.ui.profileList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.core.dtos.profile.ProfileList;
import com.infineon.esim.lpa.core.dtos.profile.ProfileMetadata;
import com.infineon.esim.lpa.ui.dialog.ConfirmationDialog;
import com.infineon.esim.lpa.ui.generic.AsyncActionStatus;
import com.infineon.esim.util.Log;

public class ProfileListFragment extends Fragment {
    private static final String TAG = ProfileListFragment.class.getName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listViewSelectedProfile;
    private ListView listViewAvailableProfile;

    private ProfileListViewModel viewModel;

    private ProfileList profileList = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileListViewModel.class);
        viewModel.getProfileListLiveData().observe(getViewLifecycleOwner(), profileListObserver);
        viewModel.getActionStatusLiveData().observe(getViewLifecycleOwner(), actionStatusObserver);

        // Set Swipe Refresh Layout
        swipeRefreshLayout = view.findViewById(R.id.profile_list_container);
        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshProfileList());

        listViewSelectedProfile = view.findViewById(R.id.list_selected_profile);
        listViewSelectedProfile.setOnItemClickListener(null);
        listViewAvailableProfile = view.findViewById(R.id.list_available_profiles);
        listViewAvailableProfile.setOnItemClickListener(availableProfilesOnItemClickListener);
    }

    public void setEnabledProfile() {
        ProfileListAdapter adapter = new ProfileListAdapter(
                getContext(),
                R.layout.profile_item,
                profileList.getEnabledProfile());
        // Bind data to the ListView
        listViewSelectedProfile.setAdapter(adapter);
    }

    public void setAvailableProfiles() {
        // Set ListAdapter to convert from ArrayList<ProfileMetadata> to View
        ProfileListAdapter adapter = new ProfileListAdapter(
                getContext(),
                R.layout.profile_item,
                profileList.getDisabledProfiles());

        // Bind data to the ListView
        listViewAvailableProfile.setAdapter(adapter);
    }

    final Observer<ProfileList> profileListObserver = profileList -> {
        Log.debug(TAG, "Observed change in profile list.");

        this.profileList = profileList;
        setEnabledProfile();
        setAvailableProfiles();
    };

    final AdapterView.OnItemClickListener availableProfilesOnItemClickListener = (parent, view, position, id) -> {

        ProfileMetadata currentEnabledProfile = null;
        if ((profileList.getEnabledProfile() != null) && (profileList.getEnabledProfile().size() > 0)) {
            currentEnabledProfile = profileList.getEnabledProfile().get(0);
        }

        if (profileList.getDisabledProfiles() != null) {
            ProfileMetadata newEnabledProfile = profileList.getDisabledProfiles().get(position);

            ConfirmationDialog.showEnableProfileConfirmationDialog(
                    getActivity(),
                    currentEnabledProfile,
                    newEnabledProfile);
        }
    };


    final Observer<AsyncActionStatus> actionStatusObserver = actionStatus -> {
        Log.debug(TAG, "Observed that action status changed: " + actionStatus.getActionStatus());

        switch(actionStatus.getActionStatus()) {
            case GET_PROFILE_LIST_STARTED:
                // Start showing the refreshing symbol ("sand clock")
                swipeRefreshLayout.setRefreshing(true);
                break;
            case GET_PROFILE_LIST_FINISHED:
                // Stop showing the refreshing symbol ("sand clock")
                swipeRefreshLayout.setRefreshing(false);
                break;
        }
    };
}
