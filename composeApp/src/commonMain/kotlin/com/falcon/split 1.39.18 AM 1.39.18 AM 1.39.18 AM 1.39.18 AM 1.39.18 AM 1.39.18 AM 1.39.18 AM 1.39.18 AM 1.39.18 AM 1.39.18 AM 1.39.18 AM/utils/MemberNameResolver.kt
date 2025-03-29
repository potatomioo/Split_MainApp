package com.falcon.split.utils

import com.falcon.split.contact.Contact
import com.falcon.split.contact.ContactManager
import com.falcon.split.data.network.models_app.GroupMember

class MemberNameResolver(private val contactManager: ContactManager?) {

    fun resolveDisplayName(member: GroupMember): String {
        // 1. Try to find in user's contacts by matching last 10 digits
        if (contactManager != null) {
            val contacts = contactManager.getAllContacts()
            if (contacts != null) {
                // Get the stored phone number (already normalized to last 10 digits)
                val memberPhone = member.phoneNumber

                // Find matching contact by comparing last 10 digits
                val matchingContact = contacts.find { contact ->
                    val contactLastDigits = extractLast10Digits(contact.contactNumber)
                    contactLastDigits == memberPhone
                }

                if (matchingContact != null) {
                    return matchingContact.contactName
                }
            }
        }

        // 2. Use registered name if available
        if (!member.name.isNullOrBlank()) {
            return member.name
        }

        // 3. Fall back to phone number
        return formatPhoneNumber(member.phoneNumber)
    }
    private fun formatPhoneNumber(phoneNumber: String): String {
        // Simple formatting logic - you can enhance this as needed
        return if (phoneNumber.length == 10) {
            "${phoneNumber.substring(0, 3)}-${phoneNumber.substring(3, 6)}-${phoneNumber.substring(6)}"
        } else {
            phoneNumber
        }
    }

    /**
     * Extracts the last 10 digits from a phone number
     */
    private fun extractLast10Digits(phoneNumber: String): String {
        // Remove all non-digit characters
        val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")

        // Take the last 10 digits or the entire string if less than 10 digits
        return if (digitsOnly.length > 10) {
            digitsOnly.substring(digitsOnly.length - 10)
        } else {
            digitsOnly
        }
    }
}
private fun normalizePhoneNumber(phoneNumber: String): String {
    // Strip all non-digit characters
    return phoneNumber.replace(Regex("[^0-9]"), "")
}