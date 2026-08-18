package com.example.model

data class SystemSpecs(
    val architecture: String = "aarch64 (ARM64)",
    val osVersion: String = "",
    val sdkInt: Int = 0,
    val deviceModel: String = "",
    val manufacturer: String = "",
    val kernelVersion: String = "",
    val cpuCores: Int = 0,
    val totalMemoryFormatted: String = "",
    val availableMemoryFormatted: String = "",
    val internalStorageFreeFormatted: String = "",
    val internalStorageTotalFormatted: String = "",
    val batteryLevel: Int = -1,
    val isCharging: Boolean = false,
    val isNetworkConnected: Boolean = false
)
