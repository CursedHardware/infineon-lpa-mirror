# Infineon Android LPA (Local Profile Assistant)

The Infineon Android LPA is an example implementation of an LPA according to GSMA SGP.22 v2.4.0 for 
the Android platform. It shall demonstrate the user experience of an eSIM and serve as example for 
OEM integration.

This software is developed for and tested with the following eUICC:
* [Infineon OPTIGA™ Connect Consumer (OC1120)](https://www.infineon.com/cms/en/product/security-smart-card-solutions/optiga-embedded-security-solutions/optiga-connect/optiga-connect-consumer/)

To test this LPA, please feel free to download a profile from the Infineon Test SM-DP+ profile server:
* [Infineon Test SM-DP+ profile server](https://softwaretools.infineon.com/projects/create/esim)

## Features
This software supports the following features:
* LPA according to GSMA SGP.22 v2.4.0
    * Listing of installed profiles
    * Profile download via GSMA Live or GSMA SGP.26 SM-DP+ profile servers
        * Confirmation code handling
    * Switching/Enabling/Disabling of profiles
    * Deleting of profiles
* Support for two reader types:
    * Secure Element Reader: Reads an internal (soldered) or SIM slot inserted eSIM
    * Identiv USB Reader: Reads from an external Identiv USB reader (e.g. Identiv SCR3500)
* Display of eUICC information
    * EID of the eUICC
    * PKI IDs available in the eUICC
    * GSMA SGP.22 version supported by the eUICC
    * TCA eUICC Profile Package version supported by the eUICC

## Implementation information
### Development environment
* Android Studio Chipmunk 2021.2.1

### Project structure
* com.infineon.esim.lpa
    * Android implementation with LPA user interface
* com.infineon.esim.lpa.core
    * Core LPA logic
* com.gsma.sgp.messages
    * Java classes of GSMA SGP.22 messages (ASN1 schema)
* com.infineon.esim.util
    * Utility classes

### Software Dependencies
The Infineon Android LPA is based on the following dependencies:
* Android CCID Library v1.2 for Identiv USB smartcard readers
    * This library serves as an interface between Android platform with USB host support and
      Identiv CCID compliant USB smartcard readers. Android application developers will
      integrate this library as part of their Android application to communicate with Identiv’s
      CCID readers.
    * Automatically downloaded via Gradle task before build
* Other dependencies see in app/build.gradle

## Authors

Michael Spähn (IFAG CSS M CS AE)\
Application Engineering\
michael.spaehn@infineon.com

Tan Siow Kiat (IFAP CSS SMD AP TM SAE ITS)\
Systems Application Engineering IoT Systems\
siowkiat.tan@infineon.com

Infineon Technologies AG