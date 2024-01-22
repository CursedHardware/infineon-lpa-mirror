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

import com.beanit.jasn1.ber.types.BerBitString;
import com.beanit.jasn1.ber.types.BerInteger;
import com.beanit.jasn1.ber.types.BerOctetString;
import com.gsma.sgp.messages.rspdefinitions.CertificationDataObject;
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
    private final String extCardResource;
    private final String uiccCapability;
    private final String ts102241Version;
    private final String globalplatformVersion;
    private final String rspCapability;
    private final String sasAcreditationNumber;
    private final List<String> pkiIdsForSign;
    private final List<String> pkiIdsForVerify;
    private final String euiccCategory;
    private final String forbiddenProfilePolicyRules;
    private final String ppVersion;
    private final String certificationDataObject;
    private final String treProperties;
    private final String treProductReference;

    public EuiccInfo(EUICCInfo2 euiccInfo2) {
        this(null, euiccInfo2);
    }

    public EuiccInfo(String eid, EUICCInfo2 euiccInfo2) {
        this.eid = eid;

        this.profileVersion = versionTypeToString(euiccInfo2.getProfileVersion());
        this.svn = versionTypeToString(euiccInfo2.getSvn());
        this.euiccFirmwareVer = versionTypeToString(euiccInfo2.getEuiccFirmwareVer());
        this.ts102241Version = versionTypeToString(euiccInfo2.getTs102241Version());
        this.globalplatformVersion = versionTypeToString(euiccInfo2.getGlobalplatformVersion());
        this.ppVersion = versionTypeToString(euiccInfo2.getPpVersion());

        this.uiccCapability = bitStringToString(euiccInfo2.getUiccCapability());
        this.rspCapability = bitStringToString(euiccInfo2.getRspCapability());
        this.forbiddenProfilePolicyRules = bitStringToString(euiccInfo2.getForbiddenProfilePolicyRules());
        this.treProperties = bitStringToString(euiccInfo2.getTreProperties());

        this.extCardResource = octetStringToString(euiccInfo2.getExtCardResource());
        this.sasAcreditationNumber = octetStringToString(euiccInfo2.getSasAcreditationNumber());
        this.treProductReference = octetStringToString(euiccInfo2.getTreProductReference());

        this.pkiIdsForVerify = euiccPkiIdList(euiccInfo2.getEuiccCiPKIdListForVerification());
        this.pkiIdsForSign = euiccPkiIdList(euiccInfo2.getEuiccCiPKIdListForSigning());

        this.certificationDataObject = certificationDataObject(euiccInfo2.getCertificationDataObject());

        this.euiccCategory = euiccCategory(euiccInfo2.getEuiccCategory());
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

    public String getExtCardResource() {
        return extCardResource;
    }

    public String getUiccCapability() {
        return uiccCapability;
    }

    public String getTs102241Version() {
        return ts102241Version;
    }

    public String getGlobalplatformVersion() {
        return globalplatformVersion;
    }

    public String getRspCapability() {
        return rspCapability;
    }

    public List<String> getPkiIdsForVerify() {
        return pkiIdsForVerify;
    }

    public List<String> getPkiIdsForSign() {
        return pkiIdsForSign;
    }

    public String getEuiccCategory() {
        return euiccCategory;
    }

    public String getForbiddenProfilePolicyRules() {
        return forbiddenProfilePolicyRules;
    }

    public String getPpVersion() {
        return ppVersion;
    }

    public String getSasAcreditationNumber() {
        return sasAcreditationNumber;
    }

    public String getCertificationDataObject() {
        return certificationDataObject;
    }

    public String getTreProperties() {
        return treProperties;
    }

    public String getTreProductReference() {
        return treProductReference;
    }

    private static String versionTypeToString(VersionType versionType) {
        if(versionType != null) {
            String vts = versionType.toString();
            Log.debug(TAG, "Raw version number: \"" + vts + "\"." );
            if (vts.length() == 6) {
                int major = Integer.parseInt(vts.substring(0, 2),16);
                int middle = Integer.parseInt(vts.substring(2, 4),16);
                int minor = Integer.parseInt(vts.substring(4, 6),16);

                return major + "." + middle + "." + minor;
            }
        }

        return "N/A";
    }

    private static String bitStringToString(BerBitString string) {
        if (string != null) {
            return string.toString();
        }

        return "N/A";
    }

    private static String octetStringToString(BerOctetString string) {
        if (string != null) {
            return string.toString();
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

    private static String certificationDataObject(CertificationDataObject certData) {
        if (certData != null) {
            StringBuilder sb = new StringBuilder();

            sb.append(certData);
            sb.delete(0, 2);

            int i;
            while ((i = sb.indexOf("\t")) != -1) {
                sb.deleteCharAt(i);
            }

            return sb.subSequence(0, sb.length() - 2).toString();
        }
        return "N/A";
    }

    private static String euiccCategory(BerInteger euiccCategory) {
        if (euiccCategory != null) {
            switch (euiccCategory.byteValue()) {
                case 0:
                    return "other";
                case 1:
                    return "basicEuicc";
                case 2:
                    return "mediumEuicc";
                case 3:
                    return "contactlessEuicc";
            }
        }
        return "N/A";
    }

}
