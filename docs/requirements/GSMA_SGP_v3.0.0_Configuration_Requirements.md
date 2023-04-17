# GSMA SGP.22 v3.0.0 configuration requirements for LPA

The following requirements are taken from "Annex M Configuration for RSP Server, LPA and EUICC
(Normative)"

An LPA stating conformance to this version of this specification SHALL:
- [X] Set the LpaRspCapability.crlStaplingV3Support bit to '1'.
  - See [LpaInformation.java](/core/src/main/java/com/infineon/esim/lpa/core/dtos/LpaInformation.java)
- [X] Set the LpaRspCapability.certChainV3Support bit to '1'.
  - See [LpaInformation.java](/core/src/main/java/com/infineon/esim/lpa/core/dtos/LpaInformation.java)
- [X] Set the LpaRspCapability.signedSmdsResponseV3Support bit to '1' if there is any SM-DS address configured in the Device or eUICC.
  - See [LpaInformation.java](/core/src/main/java/com/infineon/esim/lpa/core/dtos/LpaInformation.java)
- [X] Set the DeviceInfo.lpaSvn to v3.0.0.
  - See [DeviceInformation.java](/core/src/main/java/com/infineon/esim/lpa/core/dtos/DeviceInformation.java)
- [X] Set the euiccFormFactorType in DeviceInfo.
  - See [DeviceInformation.java](/core/src/main/java/com/infineon/esim/lpa/core/dtos/DeviceInformation.java)
- [X] Indicate the followings in Terminal Capability:
  - Metadata update alerting support
  - Enterprise Capable Device
  - See [ISO7816Channel.java](/app/src/main/java/com/infineon/esim/lpa/euicc/base/generic/ISO7816Channel.java)