# GSMA SGP.22 v3.0.0 server name requirements for LPA

The following requirements are taken from section 2.6.6.2 "Use of Server Name indication extension 
for ES9+ and ES11"

## General principle

In addition to its "base" FQDN, the RSP Server SHALL support a specific FQDN for v3 Devices. The 
v3-specific FQDN SHALL be the concatenation of the string "rsp3-" and the "base" UTF-8 encoded 
FQDN. Therefore the RSP Server SHALL take care that neither the resulting label nor the domain 
name in their final encoding violate the length limits of domain names.

- [ ] A v3 Device SHALL include the "server_name" extension defined in RFC 6066 [60] in the 
  ClientHello with the v3-specific FQDN computed from the "base" FQDN known from the RSP Server.

NOTE: This mechanism increases the probability that the LPA supports the TLS
certificate chain selected by the RSP Server, but there is still a risk that it is
not the case and the TLS handshake may fail. In that case the LPA MAY
retry and the RSP Server MAY select a different certificate chain.

## LPA/Device side:
- [ ] The LPA SHALL compute the v3-specific FQDN computed from the "base" FQDN known from the 
  RSP Server (e.g., an address retrieved from the eUICC, an address read from an Activation code).
- [ ] The LPA SHALL perform a DNS resolution using the v3-specific FQDN. In case of DNS lookup 
  failure (e.g., this may happen if the RSP Server is a v2 server), the LPA SHALL revert to the 
  "base" FQDN and proceed with the TLS handshake, which SHOULD include sending the "server_name" 
  extension.
- [ ] If the DNS resolution using the v3-specific FQDN succeeds, the LPA SHALL include the 
  "server_name" extension in the ClientHello message and containing the v3-specific FQDN. If the 
  RSP Server replies with a fatal-level alert, the LPA SHALL retry the TLS handshake without 
  sending the Server Name indication extension.