package com.demo.newvpn.conf

object LocalConfig {
    const val email=""
    const val url=""

    const val localServer="""[
    {
        "pwd":"123456",
        "account":"chacha20-ietf-poly1305",
        "port":100,
        "country":"Japan",
        "city":"Tokyo",
        "ip":"100.223.52.0"
    },
    {
        "pwd":"123456",
        "account":"chacha20-ietf-poly1305",
        "port":100,
        "country":"UnitedStates",
        "city":"NewYork",
        "ip":"100.223.52.1"
    }
]"""
}