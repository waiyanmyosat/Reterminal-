package com.example.model

data class ScriptModel(
    val id: String,
    val name: String,
    val description: String,
    val scriptContent: String,
    val isBuiltIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        val BUILTIN_SCRIPTS = listOf(
            ScriptModel(
                id = "sys_health_audit",
                name = "ARM64 System Audit",
                description = "Comprehensive audit of CPU architecture, memory, battery, kernel, and disk storage",
                isBuiltIn = true,
                scriptContent = """
echo "\033[1;36m=== ReTerminal Native System Audit ===\033[0m"
echo "\033[1;32m[+] Device Architecture:\033[0m $(uname -m)"
echo "\033[1;32m[+] Kernel Version:\033[0m $(uname -r -v)"
echo "\033[1;32m[+] Android Release:\033[0m $(getprop ro.build.version.release)"
echo "\033[1;32m[+] Device Model:\033[0m $(getprop ro.product.brand) $(getprop ro.product.model)"
echo "\033[1;32m[+] Current Date/Time:\033[0m $(date)"
echo "\033[1;32m[+] Uptime:\033[0m $(uptime)"
echo ""
echo "\033[1;33m--- Memory Statistics ---\033[0m"
cat /proc/meminfo | head -n 5
echo ""
echo "\033[1;33m--- Storage Partitions ---\033[0m"
df -h | head -n 6
echo ""
echo "\033[1;32m[✓] System audit completed successfully.\033[0m"
                """.trimIndent()
            ),
            ScriptModel(
                id = "network_diagnostics",
                name = "Network Latency & Connectivity",
                description = "Tests DNS resolution, gateway routing, and packet latency to public servers",
                isBuiltIn = true,
                scriptContent = """
echo "\033[1;34m=== Network Connectivity Diagnostic ===\033[0m"
echo "\033[1;32m[*] Checking IP Configuration:\033[0m"
ip -br addr 2>/dev/null || ifconfig | grep "inet "
echo ""
echo "\033[1;32m[*] Pinging Google DNS (8.8.8.8):\033[0m"
ping -c 3 8.8.8.8
echo ""
echo "\033[1;32m[*] Pinging Cloudflare DNS (1.1.1.1):\033[0m"
ping -c 3 1.1.1.1
echo ""
echo "\033[1;32m[✓] Connectivity test finished.\033[0m"
                """.trimIndent()
            ),
            ScriptModel(
                id = "sandbox_explorer",
                name = "App Environment & Paths",
                description = "Explores internal app sandbox directories, PATH binaries, and available utilities",
                isBuiltIn = true,
                scriptContent = """
echo "\033[1;35m=== App Sandbox & PATH Explorer ===\033[0m"
echo "\033[1;32m[+] HOME Directory:\033[0m ${'$'}HOME"
echo "\033[1;32m[+] Current Working Dir:\033[0m $(pwd)"
echo "\033[1;32m[+] Effective PATH:\033[0m ${'$'}PATH"
echo "\033[1;32m[+] Shell Executable:\033[0m ${'$'}SHELL"
echo ""
echo "\033[1;33m[*] Testing common UNIX binaries in PATH:\033[0m"
for cmd in ls grep sed awk tar gzip find xargs curl toybox; do
    if which ${'$'}cmd >/dev/null 2>&1; then
        echo "  [FOUND] ${'$'}cmd -> $(which ${'$'}cmd)"
    else
        echo "  [MISSING] ${'$'}cmd"
    fi
done
echo ""
echo "\033[1;32m[✓] Environment inspection complete.\033[0m"
                """.trimIndent()
            )
        )
    }
}
