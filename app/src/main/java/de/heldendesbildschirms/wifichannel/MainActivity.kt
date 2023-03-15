package de.heldendesbildschirms.wifichannel

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*

class WiFiNetwork(val ssid: String, val channel: Int, val level: Int, val channelWidth: Int) {
    override fun toString(): String {
        return "$ssid (Kanal: $channel, Signalstärke: $level dBm, Kanalbreite: $channelWidth MHz)"
    }
}

class MyWiFiNetwork(val ssid: String, val channel24GHz: Int, val level24GHz: Int, val channel5GHz: Int, val level5GHz: Int, val channelWidth24GHz: Int, val channelWidth5GHz: Int) {
    override fun toString(): String {
        return "$ssid (Kanal: $channel24GHz, Signalstärke: $level24GHz dBm, Kanal 5GHz: $channel5GHz, Signalstärke: $level5GHz dBm Kanalbreite 2,4 GHz: $channelWidth24GHz Kanalbreite 5GHz: $channelWidth5GHz MHz)"
    }
}

class Channel_24_GHz {
    val channels = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
    val frequency = mutableMapOf<Int, Int>()
}

class Channel24GHz {
    val channels = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
    val frequency = mutableMapOf<Int, Int>()

    init {
        for (channel in channels) {
            frequency[channel] = 0
        }
    }

    fun countChannels(scanResults: List<ScanResult>) {
        for (result in scanResults) {
            if (result.frequency >= 2400 && result.frequency <= 2483) {
                val channel = convertFrequencyToChannel(result.frequency)
                frequency[channel] = frequency[channel]!! + 1
            }
        }
    }

    private fun convertFrequencyToChannel(frequency: Int): Int {
        return when {
            frequency >= 2412 && frequency <= 2484 -> {
                (frequency - 2412) / 5 + 1
            }
            frequency >= 5170 && frequency <= 5825 -> {
                (frequency - 5170) / 5 + 34
            }
            else -> -1
        }
    }
}

class MainActivity : AppCompatActivity() {
    //private var currentChannel: Int? = null


    val channels245GHz = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ,14, 36, 38, 40, 42, 44, 46, 50, 52, 54, 56, 58, 60, 62, 64, 100, 102, 104, 106, 108, 110, 112, 114, 116, 118, 120, 122, 124, 126, 128, 130, 132, 134, 136, 138, 140)
    val channelFrequency = mutableMapOf<Int, Float>()

    var MywifiSelect = "";
    val all_scan_channelFrequency = mutableMapOf<Int, Float>()
    //var scanSelect = 0

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestWifiPermission()


        /*val networks:WiFiNetwork
        val currentselectWiFi:MyWiFiNetwork

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // WiFi-Scan starten
        wifiManager.startScan()

        // BroadcastReceiver für WiFi-Scan-Ergebnisse
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    // Scanergebnisse sind verfügbar, Code hier ausführen
                    val scanResults = wifiManager.scanResults
                    val networks = scanResults.map {
                        WiFiNetwork(it.SSID, frequencyToChannel(it.frequency), it.level)
                    }
                    // Hier können Sie Ihre Logik implementieren, die auf die Scanergebnisse reagiert
                }
            }
        }

// Registrieren Sie den BroadcastReceiver für WiFi-Scan-Ergebnisse
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)*/

        for (channel in channels245GHz) {
            channelFrequency[channel] = 0f
        }

        val scan = mutableListOf(channelFrequency)
        scan_wifi()

        val importance_Channel = mutableListOf<Float>()
        importance_Channel.add(0f)

        var spinner_scan_pos = 0;
        val itemsscan = mutableListOf<String>()

        val button_remove = findViewById<Button>(R.id.button_remove_scan)
        button_remove.setOnClickListener {
            if (spinner_scan_pos != 0 && spinner_scan_pos <= scan.lastIndex) {
                scan.remove(scan[spinner_scan_pos])
                itemsscan.removeAt(spinner_scan_pos)
                val spinner_select_scan_val = findViewById<Spinner>(R.id.spinner_select_scan)
                Toast.makeText(this, spinner_scan_pos.toString() + "remove", Toast.LENGTH_SHORT).show()
                val historyscanlist = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsscan)
                historyscanlist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner_select_scan_val.adapter = historyscanlist
                importance_Channel.removeAt(spinner_scan_pos)
             } else {
                Toast.makeText(this,  "you cant remove this", Toast.LENGTH_SHORT).show()
            }
        }

        val button = findViewById<Button>(R.id.button_scan)
        button.setOnClickListener {
            scan.add(scan_wifi())

            val seekBar_importance_Channel_val = findViewById<SeekBar>(R.id.seekBar_importance_Channel)
            //importance_Channel[scan.lastIndex] = seekBar_importance_Channel_val.progress.toFloat()
            importance_Channel.add(seekBar_importance_Channel_val.progress.toFloat())
            Log.d("String", importance_Channel.toString())

            Toast.makeText(this, "Scanned", Toast.LENGTH_SHORT).show()

           //Log.d("Listsize", (scan.lastIndex).toString())

            for (channel in channels245GHz) {
                all_scan_channelFrequency[channel] = 0f
            }

            var count = 0
            for (current_scan in scan) {
               //Log.d("String", (count).toString())
                for (result in current_scan) {
                    //Log.d("scanlist", result.key.toString())
                    //all_scan_channelFrequency[result.key] = (all_scan_channelFrequency[result.key]!! + result.value)!!
                    //all_scan_channelFrequency[result.key] = (all_scan_channelFrequency[result.key]!! + result.value)!! / 100 * importance_Channel[count]!!
                    //all_scan_channelFrequency[result.key] = (all_scan_channelFrequency[result.key]!! + result.value)!! * (importance_Channel[count] / 100)!!
                    all_scan_channelFrequency[result.key] = (all_scan_channelFrequency[result.key]!!) + result.value!! * (importance_Channel[count] / 100)!!
                //all_scan_channelFrequency[result.key] =+  result.value!!
                    //current_scan[result.key] = 1f
                    //if (scan.size == count) {
                        //all_scan_channelFrequency[result.key] = all_scan_channelFrequency[result.key]!! / scan.lastIndex
                    //}
                }
                count++
            }

            /*var count2 = 0
            for (channel in channels245GHz) {
                all_scan_channelFrequency[channel] = all_scan_channelFrequency[channel]!! / (scan.lastIndex.toFloat() / importance_Channel[count].toFloat())!!
                all_scan_channelFrequency[channel] = all_scan_channelFrequency[channel]!! / scan.lastIndex.toFloat()!!
                count2++
            }*/

            var machedurschschnitt = 0f;
            for (result in importance_Channel) {
                machedurschschnitt += result.toFloat()
            }
            machedurschschnitt /= 100
            Log.d("String", (machedurschschnitt).toString())
            for (channel in channels245GHz) {
                all_scan_channelFrequency[channel] = all_scan_channelFrequency[channel]!! / machedurschschnitt!!
                //all_scan_channelFrequency[channel] = all_scan_channelFrequency[channel]!! / scan.lastIndex.toFloat()!!
            }

            print_list(scan,scan.lastIndex)

            //val itemsscan = mutableListOf<String>()

            /*var i = 0;
            for (result in scan) {
                Log.d("String", result.toString())

                if (i != 0) {
                    itemsscan.add(i.toString())
                }else {
                    itemsscan.add("select history")
                }
                i++
            }*/

            itemsscan.clear()
            for (i in scan.indices) {
                //Log.d("String", scan[i].toString())
                itemsscan.add(if (i == 0) "select history" else i.toString())
            }
            itemsscan.add("all scans")

            val spinner_select_scan_val = findViewById<Spinner>(R.id.spinner_select_scan)

            val historyscanlist = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemsscan)
            historyscanlist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner_select_scan_val.adapter = historyscanlist

            spinner_select_scan_val.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    //if (!isReloadingList) {
                    //Toast.makeText(this@MainActivity, "Selected item: " + networks[position].ssid, Toast.LENGTH_SHORT).show()
                    if (position != 0) {
                        if (position != scan.lastIndex+1) {
                            print_list(scan, position)
                        } else {
                            print_all_scan_list(all_scan_channelFrequency)
                        }
                    }
                    spinner_scan_pos = position
                    //print_list(all_scan_channelFrequency)
                    //}
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        }
    }

    /*private fun calculateInterferedChannels(currentChannel: Int): Int {
        var interferedChannels = 0
        for (i in 1..14) {
            if (i == currentChannel) continue
            if (Math.abs(i - currentChannel) <= 4) {
                interferedChan
                nels++
            }
        }
        return interferedChannels
    }*/

    //private var isReloadingList = false
    private fun scan_wifi(): MutableMap<Int, Float> {
        val channelFrequencyInt = mutableMapOf<Int, Int>()
        var channelFrequency = mutableMapOf<Int, Float>()

        for (channel in channels245GHz) {
            channelFrequency[channel] = 0f
        }

        for (channel in channels245GHz) {
            channelFrequencyInt[channel] = 0
        }

        val spinner_select_wifi_var = findViewById<Spinner>(R.id.spinner_select_wifi)

        val networks:WiFiNetwork
        val currentselectWiFi:MyWiFiNetwork

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (wifiManager.isWifiEnabled) {
            val wifiInfo = wifiManager.connectionInfo

            /*wifiConfigurations
            for (configuration in wifiConfigurations) {
                if (configuration.frequency >= 2400 && configuration.frequency <= 2500) {
                    Log.d("WiFi Network", "Supports 2.4 GHz")
                } else if (configuration.frequency >= 5000 && configuration.frequency <= 6000) {
                    Log.d("WiFi Network", "Supports 5 GHz")
                }
            }

            for (configuration in wifiConfigurations) {
                if (configuration.SSID == wifiInfo.ssid) {
                    val frequency = configuration.frequency
                    if (frequency >= 2400 && frequency <= 2500) {
                        Log.d("WifiInfo", "Supports 2.4 GHz")
                    } else if (frequency >= 4900 && frequency <= 5900) {
                        Log.d("WifiInfo", "Supports 5 GHz")
                    }
                }
            }*/

            // WiFi-Scan starten
            try {
                wifiManager.startScan()
            } catch (e: Exception) {
                // Handle exception
            }

            // BroadcastReceiver für WiFi-Scan-Ergebnisse
            val wifiScanReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                        // Scanergebnisse sind verfügbar, Code hier ausführen
                        val scanResults = wifiManager.scanResults
                        val networks = scanResults.map {
                            WiFiNetwork(it.SSID, frequencyToChannel(it.frequency), it.level, conventchannelWidthtoMHz(it.channelWidth))
                        }
                        // Hier können Sie Ihre Logik implementieren, die auf die Scanergebnisse reagiert
                    }
                }
            }
            /*
            //if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val scanResults = wifiManager.scanResults
                val networks = scanResults.map {
                    WiFiNetwork(it.SSID, frequencyToChannel(it.frequency), it.level, conventchannelWidthtoMHz(it.channelWidth))
                }*/
            //var MywifiSelect = "";
        //}
        }
        return channelFrequency
    }

    private fun print_all_scan_list(scan: MutableMap<Int, Float>) {
        val items = mutableListOf<String>()

        //for (result in all_scan_channelFrequency) {
        for (result in scan) {
            //Log.d("String", result.toString())
            items.add(result.toString())
        }

        val listView = findViewById<ListView>(R.id.channel_list)
        val channelAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = channelAdapter
    }

    private fun print_list(scan: MutableList<MutableMap<Int, Float>>, indexofscan: Int) {
        val items = mutableListOf<String>()

        //for (result in all_scan_channelFrequency) {
        for (result in scan[indexofscan]) {
            //Log.d("String", result.toString())
            items.add(result.toString())
        }

        val listView = findViewById<ListView>(R.id.channel_list)
        val channelAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = channelAdapter
    }

    private fun ananlyse_channel(currentselectWiFi: MyWiFiNetwork,  networks: List<WiFiNetwork>): MutableMap<Int, Float> {

        //val channelFrequency = FloatArray(channels24GHz.size) { 0.0f }

        val channelFrequencyInt = mutableMapOf<Int, Int>()
        val channelFrequency = mutableMapOf<Int, Float>()

        for (channel in channels245GHz) {
            channelFrequency[channel] = 0f
        }

        for (channel in channels245GHz) {
            channelFrequencyInt[channel] = 0
        }

        for (result in networks) {
            //Log.d("String", result.channel.toString())

            if (result.channel >= 1 && result.channel <= 140 && result.ssid.toString() != currentselectWiFi.ssid.toString()) {
                val channel = result.channel
                //Log.d("String", channel.toString())
                //Log.d("String", result.ssid.toString())
                //Log.d("String",currentChannel.ssid.toString())
                //Log.d("String", (100 - ((result.level+ 90) / (100 + 90)) * 100).toFloat().toString())
                /*val rssi = -70
                val minimumRSSI = -90
                val maximumRSSI = -20
                Log.d("String", (((rssi - minimumRSSI).toFloat() / (maximumRSSI. - minimumRSSI).toFloat()) * 100).toString())*/

                //if (result.level <= -20) {
                //channelFrequency[channel] = channelFrequency[channel]!! + ((result.level - -90).toFloat() / (-50 - -90).toFloat()) * 100
                //channelFrequency[channel] = channelFrequency[channel]!! + ((result.level - -90).toFloat() / (-20 - -90).toFloat()) * 100
                //channelFrequency[channel] = channelFrequency[channel]!! + ((result.level - -150).toFloat() / (-20 - -150).toFloat()) * 100

                //Log.d("result.level", result.level.toString())
                //Log.d("level24GHz", currentselectWiFi.level24GHz.toString())
                //Log.d("level5GHz", currentselectWiFi.level5GHz.toString())
                //Log.d("channel", channel.toString())

                try {
                    if (result.channel >= 1 && result.channel <= 14 && result.level > -90 && currentselectWiFi.level24GHz > -90) {
                        channelFrequency[channel] =
                            channelFrequency[channel]!! + ((result.level!! - -90).toFloat()!! / (currentselectWiFi.level24GHz!! - -90).toFloat())!! * 100

                        var frequencysize = (frequencyToChannel(channelTofrequency(channel)+result.channelWidth) - channel) / 2
                        //Log.d("frequencysize", frequencysize.toString())
                        val resultchennels = calculateInterferedChannels(channel, frequencysize)
                        if (resultchennels != null) {
                            for (channels in resultchennels) {
                                if (channel != channels)
                                {
                                    channelFrequency[channels] = channelFrequency[channels]!! + channelFrequency[channel]!!
                                }
                            }
                        }
                    }

                    if (result.channel >= 36 && result.channel <= 140 && result.level > -90 && (currentselectWiFi.level5GHz > -90)) {
                        channelFrequency[channel] =
                            channelFrequency[channel]!! + ((result.level!! - -90).toFloat()!! / (currentselectWiFi.level5GHz!! - -90).toFloat())!! * 100

                        //var maxfrequency = frequencyToChannel(channelTofrequency(channel)+currentselectWiFi.channelWidth5GHz/2)
                        //var minfrequency =  frequencyToChannel(channelTofrequency(channel)-currentselectWiFi.channelWidth5GHz/2)
                        var frequencysize = (frequencyToChannel(channelTofrequency(channel)+result.channelWidth) - channel) / 2

                        val resultchennels = calculateInterferedChannels(channel, frequencysize)
                        //Log.d("frequencysize", frequencysize.toString())
                        if (resultchennels != null) {
                            for (channels in resultchennels) {
                                if (channel != channels)
                                {
                                    channelFrequency[channels] = channelFrequency[channels]!! + channelFrequency[channel]!!
                                }
                            }
                        }
                    }
                    channelFrequencyInt[channel] = channelFrequencyInt[channel]!! + 1
                }  catch (e: Exception) {
                    Log.d("Error", "Null ist ein Wixer")
                }

                //}
            }
        }

        /*for (result in channelFrequencyInt) {

            if (1 <= result.value) {
                //Log.d("String", result.key.toString())
                //Log.d("String", calculateInterferedChannels(result.key, 4).toString())

                //var channel_now = result.value
                val resultchennels = calculateInterferedChannels(result.key, 4)
                if (resultchennels != null) {
                    for (channel in resultchennels) {
                        if (result.key != channel)
                        {
                            channelFrequency[channel] = channelFrequency[channel]!! + channelFrequency[result.key]!!
                        }
                    }
                }
            }
        }*/
        return channelFrequency
    }

    private fun check_has_24GHz_or_5GHz(wifiInfo: WiFiNetwork, networks: List<WiFiNetwork>): MyWiFiNetwork {

        var channel24GHz_temp = 0;
        var channel5GHz_temp = 0;
        var level24GHz_temp = -20;
        var level5GHz_temp = -20;
        var channelWidth_24GHz = 0;
        var channelWidth_5GHz = 0;
        Log.d("Interfered Channels",  wifiInfo.ssid.toString())
        for (network in networks) {
            Log.d("Interfered Channels",  network.ssid.toString())
        if (network.channel >= 1 && network.channel <= 14 && network.ssid.toString() == wifiInfo.ssid.toString().trim('"')) {
            channel24GHz_temp =  network.channel
            level24GHz_temp =  network.level
            channelWidth_24GHz = network.channelWidth
        }
    //} //else {
       // if ((wifiInfo.channel) >= 1 && (wifiInfo.channel) <= 14){
            if (network.channel >= 36 && network.channel <= 140 && network.ssid.toString() == wifiInfo.ssid.toString().trim('"')) {
                channel5GHz_temp = network.channel
                level5GHz_temp = network.level
                channelWidth_5GHz = network.channelWidth
            /*channel24GHz_temp = (wifiInfo.channel)
                level24GHz_temp = wifiInfo.level.toInt()
                channel5GHz_temp = network.channel
                level5GHz_temp = network.level
            } else {
                channel24GHz_temp = (wifiInfo.channel)
                level24GHz_temp = wifiInfo.level.toInt()*/
            }
       // }
    //}
    }
        return MyWiFiNetwork(wifiInfo.ssid.toString().trim('"'), channel24GHz_temp, level24GHz_temp, channel5GHz_temp, level5GHz_temp, channelWidth_24GHz,channelWidth_5GHz)
    }

    private fun calculateInterferedChannels(
        //currentChannel5GHz: Int,
        currentChannel24andGHz: Int, size: Int,
    ): List<Int>? {
        val allowedChannels = channels245GHz
        val interferedChannels24GHz: MutableList<Int> = ArrayList()
        val interferedChannels5GHz: MutableList<Int> = ArrayList()

        // Berechne gestörte Kanäle für 2,4 GHz und 5 GHz
        for (i in currentChannel24andGHz - size..currentChannel24andGHz + size) {
            if (i in allowedChannels) {
                interferedChannels24GHz.add(i)
            }
        }

        // Berechne gestörte Kanäle für 2,4 GHz
        /*for (i in currentChannel24andGHz - size..currentChannel24andGHz + size) {
            if (i >= 1 && i <= 14) {
                interferedChannels24GHz.add(i)
            }
        }*/

        /*// Berechne gestörte Kanäle für 5 GHz
        for (i in currentChannel24andGHz - size..currentChannel24andGHz + size) {
            if (i >= 36 && i <= 140) {
                interferedChannels5GHz.add(i)
            }
        }*/

        // Füge beide Listen zusammen
        val interferedChannels: MutableList<Int> = ArrayList()
        interferedChannels.addAll(interferedChannels24GHz)
        interferedChannels.addAll(interferedChannels5GHz)
        return interferedChannels
    }


    private fun frequencyToChannel(frequency: Int): Int {
        return when {
            frequency >= 2412 && frequency <= 2484 -> {
                (frequency - 2412) / 5 + 1
            }
            frequency >= 5170 && frequency <= 5825 -> {
                (frequency - 5170) / 5 + 34
            }
            else -> -1
        }
    }

    private fun channelTofrequency(channel: Int): Int {
        return when {
            channel >= 1 && channel <= 14 -> {
                return 2407 + (channel) * 5
            }
            channel >= 36 && channel <= 140 -> {
                return 5000 + (channel) * 5
            }
            else -> -1
        }
    }

    private fun conventchannelWidthtoMHz(channelWidth: Int): Int {
         var channelWidthMHz = 0
        when (channelWidth)  {
            0 -> {
                 channelWidthMHz = 20 }
            1 -> {
                channelWidthMHz = 40 }
            2 -> {
                channelWidthMHz = 80 }
            3 -> {
                channelWidthMHz = 160 }
            else -> {channelWidth+1 * 20 }
        }

        return channelWidthMHz
    }

    private val REQUEST_CODE_ACCESS_WIFI_STATE = 1
    private val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    fun requestWifiPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_WIFI_STATE),
                REQUEST_CODE_ACCESS_WIFI_STATE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_ACCESS_WIFI_STATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Do something with the WiFi state
                } else {
                    // Handle the case when the permission was denied
                }
                return
            }
            else -> {
            }
        }
    }
}