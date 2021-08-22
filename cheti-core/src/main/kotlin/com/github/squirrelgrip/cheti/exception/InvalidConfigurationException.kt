package com.github.squirrelgrip.cheti.exception

class InvalidConfigurationException(message: String) : Exception(message) {
    constructor(): this("Invalid Configuration")

}
