package com.falcon.split.contact

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat

class AndroidContactManager(private val activity: Activity) : ContactManager {
    private val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    private var permissionCallback: ((Boolean) -> Unit)? = null
    private var contactPickCallback: ((Contact?) -> Unit)? = null

    // Cache of contacts
    private var contactsCache: List<Contact>? = null

    override fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requestPermission(onResult: (Boolean) -> Unit) {
        permissionCallback = onResult
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_CONTACTS),
            PERMISSIONS_REQUEST_READ_CONTACTS
        )
    }

    override fun pickContact(onContactPicked: (Contact?) -> Unit) {
        contactPickCallback = onContactPicked
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        activity.startActivityForResult(intent, CONTACT_PICKER_RESULT)
    }

    @SuppressLint("Range")
    override fun getAllContacts(): List<Contact>? {
        // Return cached contacts if available
        if (contactsCache != null) {
            return contactsCache
        }

        if (!hasPermission()) {
            return null
        }

        val contacts = mutableListOf<Contact>()
        val contentResolver = activity.contentResolver

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        ) ?: return null

        try {
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    )

                    val name = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    ) ?: "Unknown"

                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        ) ?: continue

                        try {
                            while (phoneCursor.moveToNext()) {
                                val phoneNumber = phoneCursor.getString(
                                    phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                )

                                // Add the contact
                                contacts.add(Contact(name, normalizePhoneNumber(phoneNumber)))
                                break // Just take the first phone number for now
                            }
                        } finally {
                            phoneCursor.close()
                        }
                    }
                }
            }
        } finally {
            cursor.close()
        }

        // Cache the contacts
        contactsCache = contacts
        return contacts
    }

    override fun getContactByNumber(phoneNumber: String): Contact? {
        val contacts = getAllContacts() ?: return null
        val normalizedNumber = normalizePhoneNumber(phoneNumber)

        return contacts.find {
            normalizePhoneNumber(it.contactNumber) == normalizedNumber
        }
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                val granted = grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                permissionCallback?.invoke(granted)
                permissionCallback = null

                // Clear cache if permissions were revoked
                if (!granted) {
                    contactsCache = null
                }
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CONTACT_PICKER_RESULT -> {
                if (resultCode == Activity.RESULT_OK) {
                    parseContactResult(data)
                } else {
                    contactPickCallback?.invoke(null)
                    contactPickCallback = null
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun parseContactResult(data: Intent?) {
        if (data == null) {
            contactPickCallback?.invoke(null)
            contactPickCallback = null
            return
        }

        val contactUri = data.data ?: run {
            contactPickCallback?.invoke(null)
            contactPickCallback = null
            return
        }

        val contentResolver = activity.contentResolver
        val cursor = contentResolver.query(contactUri, null, null, null, null) ?: run {
            contactPickCallback?.invoke(null)
            contactPickCallback = null
            return
        }

        try {
            if (cursor.moveToFirst()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) ?: "Unknown"

                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    ) ?: run {
                        contactPickCallback?.invoke(null)
                        contactPickCallback = null
                        return
                    }

                    try {
                        if (phoneCursor.moveToFirst()) {
                            val phoneNumber = phoneCursor.getString(
                                phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                            contactPickCallback?.invoke(Contact(name, phoneNumber))
                        } else {
                            contactPickCallback?.invoke(null)
                        }
                    } finally {
                        phoneCursor.close()
                    }
                } else {
                    contactPickCallback?.invoke(null)
                }
            } else {
                contactPickCallback?.invoke(null)
            }
        } finally {
            cursor.close()
            contactPickCallback = null
        }
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Strip all non-digit characters
        return phoneNumber.replace(Regex("[^0-9]"), "")
    }

    companion object {
        const val CONTACT_PICKER_RESULT = 1001
    }
}