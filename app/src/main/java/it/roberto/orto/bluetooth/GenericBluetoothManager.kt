package it.roberto.orto.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.companion.BluetoothDeviceFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

class GenericBluetoothManager(
    private val context: Context
) : BluetoothDeviceManager {

    private val bluetoothManager: BluetoothManager? = context.getSystemService()


    @RequiresPermission(
        allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT]
    )
    override fun scanDevice(): Flow<List<BluetoothDevice>> = callbackFlow {
        val bluetoothAdapter = bluetoothManager?.adapter
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return@callbackFlow
        }

        var currentList = bluetoothAdapter.bondedDevices.toList()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when(intent?.action){
                    BluetoothDevice.ACTION_FOUND->{
                        val device:BluetoothDevice?= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE,BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                        if (device != null){
                            currentList=currentList+device
                            trySend(currentList)
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED->{
                        close()
                    }
                }
            }

        }
        val intentFilter= IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) // terminata
        }
        ContextCompat.registerReceiver(context,receiver,intentFilter,ContextCompat.RECEIVER_EXPORTED)
        trySend(currentList)
        bluetoothAdapter.startDiscovery()
        awaitClose{
            bluetoothAdapter.cancelDiscovery()
            context.unregisterReceiver(receiver)
        }

    }.distinctUntilChanged().debounce(2000)



}

