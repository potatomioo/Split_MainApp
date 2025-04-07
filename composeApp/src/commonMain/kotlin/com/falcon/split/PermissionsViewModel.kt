package com.falcon.split

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import kotlinx.coroutines.launch

class PermissionsViewModel(
    private val controller: PermissionsController
): ViewModel() {

    var notificationPermissionState by mutableStateOf(PermissionState.NotDetermined)
        private set
    var contactPermissionState by mutableStateOf(PermissionState.NotDetermined)
        private set

    init {
        viewModelScope.launch {
            notificationPermissionState = controller.getPermissionState(Permission.REMOTE_NOTIFICATION)
            contactPermissionState = controller.getPermissionState(Permission.CONTACTS)
        }
    }

    fun provideOrRequestNotificationPermission() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.REMOTE_NOTIFICATION)
                this@PermissionsViewModel.notificationPermissionState = PermissionState.Granted
            } catch (e: DeniedAlwaysException) {
                this@PermissionsViewModel.notificationPermissionState = PermissionState.DeniedAlways
            } catch (e: DeniedException) {
                this@PermissionsViewModel.notificationPermissionState = PermissionState.Denied
            } catch (e: RequestCanceledException) {
                this@PermissionsViewModel.notificationPermissionState = PermissionState.NotGranted
                e.printStackTrace()
            }
        }
    }
    fun provideOrRequestContactPermission() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.CONTACTS)
                this@PermissionsViewModel.contactPermissionState = PermissionState.Granted
            } catch (e: DeniedAlwaysException) {
                this@PermissionsViewModel.contactPermissionState = PermissionState.Granted // TODO: FIx this
            } catch (e: DeniedException) {
                this@PermissionsViewModel.contactPermissionState = PermissionState.Denied
            } catch (e: RequestCanceledException) {
                this@PermissionsViewModel.contactPermissionState = PermissionState.NotGranted
                e.printStackTrace()
            }
        }
    }

}