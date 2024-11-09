package it.roberto.orto.bluetooth

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.Flow

interface BluetoothDeviceManager {
    fun scanDevice(): Flow<List<BluetoothDevice>>
}