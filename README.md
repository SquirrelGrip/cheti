[![Build Status](https://travis-ci.com/SquirrelGrip/cheti.svg?branch=develop)](https://travis-ci.com/SquirrelGrip/cheti)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.squirrelgrip/cheti/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.squirrelgrip/cheti)

# Cheti
A generic key generation tool for automating generation of ROOT, Intermediate and End Entity Certificates for use in testing.

## Overview
Using a simple JSON configuration file, we can define a set of X509 Certificates that will be generated and then be used to secure a number of different services. The expiry of the certificates can be adjusted in order for the certificates to be used for a short period of time (usually for the time of the automated tests).

