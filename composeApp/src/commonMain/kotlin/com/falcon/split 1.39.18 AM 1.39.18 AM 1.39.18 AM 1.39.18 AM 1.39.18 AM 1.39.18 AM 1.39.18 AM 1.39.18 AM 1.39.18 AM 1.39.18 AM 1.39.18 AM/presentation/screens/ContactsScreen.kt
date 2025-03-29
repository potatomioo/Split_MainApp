import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.falcon.split.contact.Contact
import com.falcon.split.contact.ContactManager

@Composable
fun ContactPicker(
    contactManager: ContactManager,
    onContactPicked: (Contact?) -> Unit
) {
    var showPermissionRequest by remember { mutableStateOf(false) }

    if (!contactManager.hasPermission()) {
        if (!showPermissionRequest) {
            showPermissionRequest = true
            contactManager.requestPermission { granted ->
                if (granted) {
                    contactManager.pickContact(onContactPicked)
                }
            }
        }
    } else {
        contactManager.pickContact(onContactPicked)
    }
}