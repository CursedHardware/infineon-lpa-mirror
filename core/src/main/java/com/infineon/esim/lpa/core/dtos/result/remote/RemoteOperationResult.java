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

package com.infineon.esim.lpa.core.dtos.result.remote;

public class RemoteOperationResult {
    private final Boolean success;
    private final String genericError;
    private final RemoteError remoteError;

    public RemoteOperationResult() {
        this.success = true;
        this.genericError = null;
        this.remoteError = null;
    }

    public RemoteOperationResult(RemoteError remoteError) {
        this.success = false;
        this.genericError = null;
        this.remoteError = remoteError;
    }

    public RemoteOperationResult(String genericError) {
        this.success = false;
        this.genericError = genericError;
        this.remoteError = null;
    }

    public Boolean getSuccess() {
        return success;
    }

    public RemoteError getRemoteError() {
        return remoteError;
    }

    public String getErrorDetails() {
        if(genericError != null) {
            return genericError;
        }

        if (remoteError != null) {
            StringBuilder sb = new StringBuilder();

            if (remoteError.getStatus() != null) sb.append(String.format("Status: %1$s\n",remoteError.getStatus()));
            if (remoteError.getSubjectCode() != null) sb.append(String.format("Subject Code: %1$s\n",remoteError.getSubjectCode()));
            if (remoteError.getReasonCode() != null) sb.append(String.format("Reason Code: %1$s\n",remoteError.getReasonCode()));
            if (remoteError.getSubjectIdentifier() != null) sb.append(String.format("SubjectIdentifier: %1$s\n",remoteError.getSubjectIdentifier()));
            if (remoteError.getMessage() != null) sb.append(String.format("Message: %1$s",remoteError.getMessage()));

            return sb.toString();
        }

        return null;
    }
}
