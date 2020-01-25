package com.example.nfc_kotlin.record

interface ParsedNdefRecord {
    /**
     * Read the data from NFC card
     *
     * @return the String data read from the NFC Card
     */
    fun str(): String
}
