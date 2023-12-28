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

package com.infineon.esim.lpa.core.dtos;

import com.beanit.jasn1.ber.types.BerOctetString;
import com.gsma.sgp.messages.rspdefinitions.PprIds;
import com.gsma.sgp.messages.pkix1implicit88.SubjectKeyIdentifier;
import com.gsma.sgp.messages.rspdefinitions.EUICCInfo2;
import com.gsma.sgp.messages.rspdefinitions.VersionType;
import com.infineon.esim.util.Log;

import java.util.ArrayList;
import java.util.List;

public class EuiccInfo {
    private static final String TAG = EuiccInfo.class.getName();

    private String eid;
    private final String profileVersion;
    private final String svn;
    private final String euiccFirmwareVer;
    private final String globalplatformVersion;
    private final String sasAcreditationNumber;
    private final List<String> pkiIdsForSign;
    private final List<String> pkiIdsForVerify;
    private final PprIds forbiddenProfilePolicyRules;
    private final BerOctetString extCardResource;

    public EuiccInfo(EUICCInfo2 euiccInfo2) {
        this(null, euiccInfo2);
    }

    public EuiccInfo(String eid, EUICCInfo2 euiccInfo2) {
        this.eid = eid;
        this.profileVersion = versionTypeToString(euiccInfo2.getProfileVersion());
        this.svn = versionTypeToString(euiccInfo2.getSvn());
        this.euiccFirmwareVer = versionTypeToString(euiccInfo2.getEuiccFirmwareVer());
        this.globalplatformVersion = versionTypeToString(euiccInfo2.getGlobalplatformVersion());

        this.sasAcreditationNumber = euiccInfo2.getSasAcreditationNumber().toString();

        this.pkiIdsForSign = euiccPkiIdList(euiccInfo2.getEuiccCiPKIdListForSigning());
        this.pkiIdsForVerify = euiccPkiIdList(euiccInfo2.getEuiccCiPKIdListForVerification());

        this.forbiddenProfilePolicyRules = euiccInfo2.getForbiddenProfilePolicyRules();

        this.extCardResource = euiccInfo2.getExtCardResource();
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getEid() {
        return eid;
    }

    public String getProfileVersion() {
        return profileVersion;
    }

    public String getSvn() {
        return svn;
    }

    public String getEuiccFirmwareVer() {
        return euiccFirmwareVer;
    }


    public String getGlobalplatformVersion() {
        return globalplatformVersion;
    }

    public String getSasAcreditationNumber() {
        return sasAcreditationNumber;
    }

    public List<String> getPkiIdsForSign() {
        return pkiIdsForSign;
    }

    public List<String> getPkiIdsForVerify() {
        return pkiIdsForVerify;
    }

    public String getPkiIdsForSignAsString(){
        StringBuilder sb = new StringBuilder();
        if (!pkiIdsForSign.isEmpty()) {
            for(String pkiId : pkiIdsForSign) {
                sb.append(pkiId);
                sb.append("\n");
            }
        } else {
            return "Not Found";
        }
        return sb.subSequence(0, sb.length() - 1).toString();
    }

    public String getPkiIdsForVerifyAsString(){
        StringBuilder sb = new StringBuilder();
        if (!pkiIdsForVerify.isEmpty()) {
            for(String pkiId : pkiIdsForVerify) {
                sb.append(pkiId);
                sb.append("\n");
            }
        } else {
            return "Not Found";
        }
        return sb.subSequence(0, sb.length() - 1).toString();
    }

    public String getForbiddenProfilePolicyRules() {
        return forbiddenProfilePolicyRules.toString();
    }

    public String getExtCardResource() {
        return extCardResource.toString();
    }

    private static String versionTypeToString(VersionType versionType) {
        if(versionType != null) {
            String vts = versionType.toString();
            Log.debug(TAG, "Raw version number: \"" + vts + "\"." );
            if (vts.length() == 6) {
                int major = Integer.parseInt(vts.substring(0, 2));
                int middle = Integer.parseInt(vts.substring(2, 4));
                int minor = Integer.parseInt(vts.substring(4, 6));

                return major + "." + middle + "." + minor;
            }
        }

        return "N/A";
    }

    private static List<String> euiccPkiIdList(EUICCInfo2.EuiccCiPKIdListForSigning pkiIdListIn) {
        List<String> pkiIdList = new ArrayList<>();

        for(SubjectKeyIdentifier ski : pkiIdListIn.getSubjectKeyIdentifier()) {
            pkiIdList.add(ski.toString());
        }

        return pkiIdList;
    }

    private static List<String> euiccPkiIdList(EUICCInfo2.EuiccCiPKIdListForVerification pkiIdListIn) {
        List<String> pkiIdList = new ArrayList<>();

        for(SubjectKeyIdentifier ski : pkiIdListIn.getSubjectKeyIdentifier()) {
            pkiIdList.add(ski.toString());
        }

        return pkiIdList;
    }


}
