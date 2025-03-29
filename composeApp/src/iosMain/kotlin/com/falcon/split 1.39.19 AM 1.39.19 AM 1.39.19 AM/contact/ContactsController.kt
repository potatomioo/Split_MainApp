// shared/src/iosMain/kotlin/com/falcon/split/contact/IosContactManager.kt
package com.falcon.split.contact

import platform.UIKit.*

class IosContactManager(
    private val viewController: UIViewController
) : ContactManager {

    override fun hasPermission(): Boolean {
//        return CNContactStore().authorizationStatusForEntityType(CNEntityType.CNEntityTypeContacts) ==
//                CNAuthorizationStatus.CNAuthorizationStatusAuthorized
        return false
    }

    override fun requestPermission(onResult: (Boolean) -> Unit) {
//        CNContactStore().requestAccessForEntityType(CNEntityType.CNEntityTypeContacts) { granted, _ ->
//            onResult(granted)
//        }
    }

    override fun pickContact(onContactPicked: (Contact?) -> Unit) {
//        if (!hasPermission()) {
//            onContactPicked(null)
//            return
//        }
//
//        val contactPicker = CNContactPickerViewController()
//        contactPicker.delegate = object : NSObject(), CNContactPickerDelegateProtocol {
//            override fun contactPicker(picker: CNContactPickerViewController, didSelectContact contact: CNContact) {
//                val name = "${contact.givenName} ${contact.familyName}"
//                val number = contact.phoneNumbers.firstOrNull()?.let {
//                    (it.value as CNPhoneNumber).stringValue
//                } ?: ""
//
//                if (name.isNotEmpty() && number.isNotEmpty()) {
//                    onContactPicked(ContactInfo(name, getFormattedPhoneNumber(number)))
//                } else {
//                    onContactPicked(null)
//                }
//
//                viewController.dismissViewControllerAnimated(true, null)
//            }
//        }
//
//        viewController.presentViewController(contactPicker, true, null)
    }

    private fun getFormattedPhoneNumber(number: String): String {
        val digitsOnly = number.filter { it.isDigit() }
        return when {
            digitsOnly.length == 12 -> digitsOnly.substring(2)
            else -> digitsOnly
        }
    }
}