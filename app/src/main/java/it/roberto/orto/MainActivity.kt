package it.roberto.orto

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import it.roberto.orto.bluetooth.GenericBluetoothManager
import it.roberto.orto.presentation.theme.OrtoTheme

class MainActivity : ComponentActivity() {

    private  val bluetoothDeviceManager = GenericBluetoothManager(context = this)

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrtoTheme {

                val list by bluetoothDeviceManager.scanDevice().collectAsState(initial = emptyList())

                LazyColumn(){
                    items(list){
                        ListItem(headlineText = { Text(text = it.uuids.toString()) })
                    }
                }

            }
        }
    }
}
