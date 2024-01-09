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

package com.infineon.esim.lpa.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;

import com.infineon.esim.lpa.R;
import com.infineon.esim.lpa.core.dtos.profile.ProfileMetadata;
import com.infineon.esim.lpa.data.DataModel;


public class ConfirmationDialog {

    public static void showEnableProfileConfirmationDialog(Activity parent,
                                                                    ProfileMetadata currentEnabledProfile,
                                                                    ProfileMetadata newEnabledProfile) {
        int heading;
        int buttonText;
        String bodyStr;

        String newProfileName = newEnabledProfile.getNickname();
        if (newEnabledProfile.getNickname() == null) {
            newProfileName = newEnabledProfile.getName();
        }

        if (currentEnabledProfile == null) {
            heading = R.string.profile_action_dialogue_enable_profile_heading;
            buttonText = R.string.profile_details_button_enable_text;
            String bodyFormat = parent.getString(R.string.profile_action_dialogue_enable_profile_body);
            bodyStr = String.format(bodyFormat, newProfileName);
        } else {
            heading = R.string.profile_action_dialogue_switch_profile_heading;
            buttonText = R.string.profile_details_button_switch_text;
            String currentProfileName = currentEnabledProfile.getNickname();
            if (currentEnabledProfile.getNickname() == null) {
                currentProfileName = currentEnabledProfile.getName();
            }
            String bodyFormat = parent.getString(R.string.profile_action_dialogue_switch_profile_body);
            bodyStr = String.format(bodyFormat, newProfileName, currentProfileName, currentProfileName);
        }

        new AlertDialog.Builder(parent)
                .setTitle(heading)
                .setCancelable(true)
                .setMessage(bodyStr)
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss())
                .setPositiveButton(buttonText, (dialog, id) -> {
                    dialog.dismiss();
                    DataModel.getInstance().enableProfile(newEnabledProfile);
                })
                .show();
    }

    public static void showDeleteProfileConfirmationDialog(Activity parent,
                                                           ProfileMetadata deleteProfile) {

        new AlertDialog.Builder(parent)
                .setTitle(R.string.profile_action_dialogue_delete_profile_heading)
                .setCancelable(true)
                .setMessage(R.string.profile_action_dialogue_disable_delete_profile_body)
                .setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss())
                .setPositiveButton(R.string.profile_details_button_delete_text, (dialog, id) -> {
                    dialog.dismiss();
                    DataModel.getInstance().deleteProfile(deleteProfile);
                })
        .show();
    }
}
