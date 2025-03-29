package com.falcon.split.contact

interface ContactManager {
    fun hasPermission(): Boolean
    fun requestPermission(onResult: (Boolean) -> Unit)
    fun pickContact(onContactPicked: (Contact?) -> Unit)

    fun getAllContacts(): List<Contact>?
    fun getContactByNumber(phoneNumber: String): Contact?
}

data class Contact(
    val contactName: String,
    val contactNumber: String
)