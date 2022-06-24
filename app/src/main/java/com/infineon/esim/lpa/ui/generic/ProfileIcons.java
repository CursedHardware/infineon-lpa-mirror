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

package com.infineon.esim.lpa.ui.generic;

import com.infineon.esim.lpa.R;

import java.util.HashMap;
import java.util.Map;

public class ProfileIcons {
    private static final Map<String, Integer> IMAGE_MAP = new HashMap<>();

    static {
        IMAGE_MAP.put("Transatel", R.drawable.profile_transatel);
        IMAGE_MAP.put("Ubigi", R.drawable.profile_ubigi);
        IMAGE_MAP.put("lemon", R.drawable.profile_lemon);
        IMAGE_MAP.put("Telefon", R.drawable.profile_telefon);
        IMAGE_MAP.put("Jodafone", R.drawable.profile_jodafone);
        IMAGE_MAP.put("Hydrogen", R.drawable.profile_hydrogen);
        IMAGE_MAP.put("GSMA", R.drawable.profile_gsma);
    }

    public static Integer lookupProfileImage(String name) {
        for(String profileName : IMAGE_MAP.keySet()) {
            if(name.contains(profileName)) {
                return IMAGE_MAP.get(profileName);
            }
        }

        return R.drawable.profile_default;
    }
}
