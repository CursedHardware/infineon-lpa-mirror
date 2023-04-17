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

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.infineon.esim.lpa.Application;
import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.core.dtos.profile.ProfileMetadata;
import com.infineon.esim.lpa.ui.generic.ProfileIcons;
import com.infineon.esim.lpa.ui.profileDetails.ProfileDetailsActivity;
import com.infineon.esim.util.Log;

import java.util.List;

final public class ProfileListAdapter extends ArrayAdapter<ProfileMetadata> {
    private static final String TAG = ProfileListAdapter.class.getName();

    private final List<ProfileMetadata> profileList;
    private final int resource;

    public ProfileListAdapter(Context context, int resource, List<ProfileMetadata> profiles) {
        super(context, resource, profiles);
        this.resource = resource;
        this.profileList = profiles;

        // Log profile list
        logProfileList();
    }

    private void logProfileList() {

        for(int i = 0; i < profileList.size(); i++) {
            Log.verbose(TAG, "position " + i);
            Log.verbose(TAG, "status " + profileList.get(i).getState());
            Log.verbose(TAG, "nickname " + profileList.get(i).getNickname());
            Log.verbose(TAG, "name " + profileList.get(i).getName());
            Log.verbose(TAG, "iccid " + profileList.get(i).getIccid());
        }
    }

    // Hold views of the ListView to improve its scrolling performance
    static class ListViewElementViewHolder {
        public TextView provider; // provide name
        public TextView status; // profile status (enabled/disabled)
        public ImageView profileIcon; // profile icon
        public ImageView preferenceIcon; // preference icon (gear)
        public int position;
    }

    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Log.verbose(TAG, "Getting view: position " + position +".");

        // Check if old view can be reused (!=null) or has to be created
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, parent, false);
        }

        // Create new ListViewElementViewHolder populate with the current element
        ListViewElementViewHolder listViewElementViewHolder = new ListViewElementViewHolder();
        listViewElementViewHolder.position = position;
        listViewElementViewHolder.status = convertView.findViewById(R.id.text_profile_list_item_status);
        listViewElementViewHolder.provider = convertView.findViewById(R.id.text_provider);
        listViewElementViewHolder.profileIcon = convertView.findViewById(R.id.image_profile_icon);
        listViewElementViewHolder.preferenceIcon = convertView.findViewById(R.id.image_profile_details_icon);

        // Set convertView to new content according to position in list
        listViewElementViewHolder.status.setText(profileList.get(position).getState());
        listViewElementViewHolder.provider.setText(profileList.get(position).getNickname());

        if(profileList.get(position).getIcon() != null) {
            listViewElementViewHolder.profileIcon.setImageIcon(profileList.get(position).getIcon());
        } else {
            listViewElementViewHolder.profileIcon.setImageResource(ProfileIcons.lookupProfileImage(profileList.get(position).getName()));
        }

        // Set tag for enabling settings menu click in onClick method
        convertView.setTag(listViewElementViewHolder);
        listViewElementViewHolder.status.setTag(listViewElementViewHolder);
        listViewElementViewHolder.provider.setTag(listViewElementViewHolder);
        listViewElementViewHolder.profileIcon.setTag(listViewElementViewHolder);
        listViewElementViewHolder.preferenceIcon.setTag(listViewElementViewHolder);
        listViewElementViewHolder.preferenceIcon.setOnClickListener(profileImageOnClickListener);

        return convertView;
    }

    final View.OnClickListener profileImageOnClickListener = view -> {
        // Get profile metadata from (image)View via tag
        ImageView imageView = (ImageView) view;
        ListViewElementViewHolder listViewElementViewHolder = (ListViewElementViewHolder) imageView.getTag();
        ProfileMetadata profileMetadata = super.getItem(listViewElementViewHolder.position);

        // Send profile metadata to the new intent
        Context context = super.getContext();
        Intent intent = new Intent(context, ProfileDetailsActivity.class);
        intent.putExtra(Application.INTENT_EXTRA_PROFILE_METADATA, profileMetadata);
        context.startActivity(intent);
    };
}

