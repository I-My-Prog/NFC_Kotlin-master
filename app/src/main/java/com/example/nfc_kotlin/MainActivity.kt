package com.example.nfc_kotlin

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings.ACTION_NFC_SETTINGS
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.nfc_kotlin.utils.Utils
import com.example.nfc_kotlin.parser.NdefMessageParser
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log;

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    // launch our application when a new Tag or Card will be scanned
    private var pendingIntent: PendingIntent? = null
    // display the data read
    private var text: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text = findViewById<View>(R.id.text) as TextView
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, "No NFC", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, this.javaClass)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
    }

    override fun onResume() {
        super.onResume()

        val nfcAdapterRefCopy = nfcAdapter
        if (nfcAdapterRefCopy != null) {
            if (!nfcAdapterRefCopy.isEnabled())
                showNFCSettings()
                //NFC is disabled -> goto NFC setting

            nfcAdapterRefCopy.enableForegroundDispatch(this, pendingIntent, null, null)
            //Read Card, only when app is foreground -> scan NFC card
        }
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        resolveIntent(intent)
    }

    private fun showNFCSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show()
        val intent = Intent(ACTION_NFC_SETTINGS)
        startActivity(intent)
    }

    /**
     * Tag data is converted to string to display
     *
     * @return the data dumped from this tag in String format
     */
    private fun dumpTagData(tag: Tag): String {
        val sb = StringBuilder()
        val id = tag.getId()
        sb.append("ID (hex): ").append(Utils.toHex(id)).append('\n')

        sb.delete(sb.length - 2, sb.length)

        return sb.toString()
    }

    private fun resolveIntent(intent: Intent) {
        val action = intent.action

        if (NfcAdapter.ACTION_TAG_DISCOVERED == action
            || NfcAdapter.ACTION_TECH_DISCOVERED == action
            || NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

            if (rawMsgs != null) {
                Log.i("NFC", "Size:" + rawMsgs.size);
                val ndefMessages: Array<NdefMessage> = Array(rawMsgs.size, {i -> rawMsgs[i] as NdefMessage});
                displayNfcMessages(ndefMessages)
            } else {
                val empty = ByteArray(0)
                val id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
                val tag = intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as Tag
                val payload = dumpTagData(tag).toByteArray()
                val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload)
                val emptyMsg = NdefMessage(arrayOf(record))
                val emptyNdefMessages: Array<NdefMessage> = arrayOf(emptyMsg);
                displayNfcMessages(emptyNdefMessages)
            }
        }
    }


    private fun displayNfcMessages(msgs: Array<NdefMessage>?) {
        if (msgs == null || msgs.isEmpty())
            return

        val builder = StringBuilder()
        val records = NdefMessageParser.parse(msgs[0])
        val size = records.size

        for (i in 0 until size) {
            val record = records[i]
            val str = record.str()
            builder.append(str).append("\n")
        }

        text?.setText(builder.toString())
    }
}