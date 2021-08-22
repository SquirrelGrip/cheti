package com.github.squirrelgrip.cheti.configuration

enum class AltSubjectType {
    /**
    *      otherName                       [0]     OtherName,
    *      rfc822Name                      [1]     IA5String,
    *      dNSName                         [2]     IA5String,
    *      x400Address                     [3]     ORAddress,
    *      directoryName                   [4]     Name,
    *      ediPartyName                    [5]     EDIPartyName,
    *      uniformResourceIdentifier       [6]     IA5String,
    *      iPAddress                       [7]     OCTET STRING,
    *      registeredID                    [8]     OBJECT IDENTIFIER}
    **/
    Other,
    RFC822Name,
    DNSName,
    X400Address,
    DirectoryName,
    EDIPartyName,
    UniformResourceIdentifier,
    IPAddress,
    registerID
}
