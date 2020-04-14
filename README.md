[![Build Status](https://travis-ci.com/SquirrelGrip/cheti.svg?branch=develop)](https://travis-ci.com/SquirrelGrip/cheti)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.squirrelgrip/cheti/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.squirrelgrip/cheti)

# Cheti
A generic key generation tool for automating generation of ROOT, Intermediate and End Entity Certificates for use in testing.

## Overview
Using a simple JSON configuration file, we can define a set of X509 Certificates that will be generated and then be used to secure a number of different services. The expiry of the certificates can be adjusted in order for the certificates to be used for a short period of time (usually for the time of the automated tests).

## Features
+ Create self signed Root CA Certificate for Signing.
+ Create an Intermediate Signing Certificates for Signing.
+ Create Server certificates with altSubject extension.
+ Create basic Client certificates with altSubject extension.
+ Create a JKS and PKCS12 KeyStore containing the trust chain.
+ Write password to file.

## How it works
A JSON configuration file contains the details of each certificates, the chains and finally the keystores that are to be created. 
This JSON file is loaded by Cheti and ...
+ validates the configuration
+ generates the keypair for each certificate
+ uses the keypair to create each certificate, signing the certificate as required
+ Writes the private key in PEM format to a key file
+ Writes the certificate in PEM format to a crt file
+ Arranges the certificates into Chains
+ Adds the chains to the KeyStore
+ Writes a pwd file containing the password

### Example Configuration File
```
{
  "common": {
    "location": "target/certs",
    "generate": "ONCE",
    "validFor": "1D"
  },
  "certificates": [
    {
      "name": "RootCA",
      "type": "ROOT",
      "generate": "ONCE",
      "location": "target/certs",
      "validFor": "1D",
      "subject": {
        "cn": "RootCA",
        "ou": "SquirrelGrip",
        "o": "Github",
        "st": "Singapore",
        "l": "Singapore",
        "c": "SG"
      }
    },
    {
      "name": "SigningCA",
      "type": "SIGNING",
      "generate": "ONCE",
      "location": "target/certs",
      "issuer": "RootCA",
      "validFor": "1D",
      "subject": {
        "cn": "SigningCA",
        "ou": "SquirrelGrip",
        "o": "Github",
        "st": "Singapore",
        "l": "Singapore",
        "c": "SG"
      }
    },
    {
      "name": "Server",
      "type": "SERVER",
      "generate": "ALWAYS",
      "location": "target/certs",
      "issuer": "SigningCA",
      "validFor": "1D",
      "subject": {
        "cn": "Server"
      },
      "extensions": {
        "altSubject": {
          "critical": false,
          "value": [
            {
              "type": "IPAddress",
              "value": "${IP_ADDRESS}"
            },
            {
              "type": "DNSName",
              "value": "${HOSTNAME}"
            }
          ]
        }
      }
    },
    {
      "name": "Client",
      "type": "CLIENT",
      "generate": "ALWAYS",
      "location": "target/certs",
      "issuer": "SigningCA",
      "validFor": "1D",
      "subject": {
        "cn": "Client"
      }
    }
  ],
  "keystores": [
    {
      "name": "keystore.jks",
      "type": "JKS",
      "password": "pass:password",
      "location": "target/certs",
      "chains": [
        "Server",
        "Client"
      ]
    },
    {
      "name": "keystore.p12",
      "type": "PKCS12",
      "password": "pass:password",
      "location": "target/certs",
      "chains": [
        "Server",
        "Client"
      ]
    }
  ],
  "chains": [
    {
      "alias": "Server",
      "chain": [
        "Server",
        "SigningCA",
        "RootCA"
      ]
    },
    {
      "alias": "Client",
      "chain": [
        "Client",
        "SigningCA",
        "RootCA"
      ]
    }
  ]

}
```

