package com.example.model

enum class CommandCategory(val label: String) {
    SYSTEM("System & Architecture"),
    FILES("Files & Storage"),
    NETWORK("Networking"),
    PROCESSES("Processes & Performance"),
    DEVELOPER("Android & Shell Tools")
}

data class QuickCommand(
    val id: String,
    val title: String,
    val command: String,
    val category: CommandCategory,
    val description: String,
    val executeImmediately: Boolean = true
) {
    companion object {
        val PRESETS = listOf(
            // System
            QuickCommand(
                id = "uname",
                title = "Kernel & ARM64 Architecture",
                command = "uname -a",
                category = CommandCategory.SYSTEM,
                description = "Display Linux kernel version and CPU architecture (aarch64 / arm64)"
            ),
            QuickCommand(
                id = "whoami",
                title = "Current User & UID",
                command = "id && whoami",
                category = CommandCategory.SYSTEM,
                description = "Show effective UID, GID, and app sandbox username"
            ),
            QuickCommand(
                id = "uptime",
                title = "System Uptime & Load",
                command = "uptime",
                category = CommandCategory.SYSTEM,
                description = "Show device runtime duration and load average"
            ),
            QuickCommand(
                id = "getprop_os",
                title = "Android Build & Device Info",
                command = "getprop ro.build.version.release && getprop ro.product.model && getprop ro.product.cpu.abi",
                category = CommandCategory.SYSTEM,
                description = "Query Android OS release, device model, and native CPU ABI"
            ),

            // Files & Storage
            QuickCommand(
                id = "ls_la",
                title = "List Directory (Detailed)",
                command = "ls -la",
                category = CommandCategory.FILES,
                description = "List all files including hidden files, permissions, sizes, and dates"
            ),
            QuickCommand(
                id = "pwd",
                title = "Print Working Directory",
                command = "pwd",
                category = CommandCategory.FILES,
                description = "Print current terminal working directory path"
            ),
            QuickCommand(
                id = "df_h",
                title = "Storage & Mount Partitions",
                command = "df -h",
                category = CommandCategory.FILES,
                description = "Show available disk space, used percentage, and mount points"
            ),
            QuickCommand(
                id = "tree_depth",
                title = "Directory Tree (Files)",
                command = "find . -maxdepth 2 -not -path '*/.*'",
                category = CommandCategory.FILES,
                description = "Show directory tree structure up to depth 2"
            ),

            // Networking
            QuickCommand(
                id = "ip_addr",
                title = "IP Addresses & Interfaces",
                command = "ip addr show || ifconfig",
                category = CommandCategory.NETWORK,
                description = "Display active network interfaces (wlan0, rmnet, loopback) and IP configuration"
            ),
            QuickCommand(
                id = "ping_dns",
                title = "Ping DNS (Google 8.8.8.8)",
                command = "ping -c 4 8.8.8.8",
                category = CommandCategory.NETWORK,
                description = "Send 4 ICMP packets to test internet latency and packet loss"
            ),
            QuickCommand(
                id = "netstat",
                title = "Active Sockets & Connections",
                command = "netstat -tuln || ss -tuln",
                category = CommandCategory.NETWORK,
                description = "List open ports and established TCP/UDP network connections"
            ),

            // Processes
            QuickCommand(
                id = "top_cpu",
                title = "Top Processes (CPU & RAM)",
                command = "top -n 1 -b -m 10",
                category = CommandCategory.PROCESSES,
                description = "Snapshot of top 10 resource-consuming processes"
            ),
            QuickCommand(
                id = "ps_ef",
                title = "Process Snapshot (ps)",
                command = "ps -ef | head -n 25",
                category = CommandCategory.PROCESSES,
                description = "List first 25 running processes with PID, PPID, and command"
            ),
            QuickCommand(
                id = "meminfo",
                title = "Detailed Memory (meminfo)",
                command = "cat /proc/meminfo | head -n 15",
                category = CommandCategory.PROCESSES,
                description = "Read kernel memory stats (MemTotal, MemFree, MemAvailable, Buffers)"
            ),
            QuickCommand(
                id = "cpuinfo",
                title = "CPU Hardware Info (cpuinfo)",
                command = "cat /proc/cpuinfo | head -n 25",
                category = CommandCategory.PROCESSES,
                description = "Read processor hardware, features (fp, asimd, aes, crc32), and core count"
            ),

            // Developer Tools
            QuickCommand(
                id = "env",
                title = "Environment Variables",
                command = "env | sort",
                category = CommandCategory.DEVELOPER,
                description = "Print all exported environment variables (PATH, HOME, SHELL, TERM)"
            ),
            QuickCommand(
                id = "logcat_recent",
                title = "Recent System Logcat",
                command = "logcat -d -t 15",
                category = CommandCategory.DEVELOPER,
                description = "Dump the last 15 lines from Android logcat buffer"
            ),
            QuickCommand(
                id = "toybox_tools",
                title = "Available Native Shell Commands",
                command = "which toybox > /dev/null && toybox --help || ls /system/bin",
                category = CommandCategory.DEVELOPER,
                description = "List all native command-line binaries and toybox tools installed in the system"
            )
        )
    }
}
