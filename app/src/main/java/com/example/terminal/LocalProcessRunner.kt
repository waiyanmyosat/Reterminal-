package com.example.terminal

import android.content.Context
import android.os.Process
import java.io.File

object LocalProcessRunner {

    fun prepareEnvironment(context: Context): File {
        val homeDir = File(context.filesDir, "home").apply { if (!exists()) mkdirs() }
        val binDir = File(context.filesDir, "bin").apply { if (!exists()) mkdirs() }
        val tmpDir = File(context.cacheDir, "tmp").apply { if (!exists()) mkdirs() }
        val scriptsDir = File(context.filesDir, "scripts").apply { if (!exists()) mkdirs() }
        return homeDir
    }

    fun startShell(context: Context, workingDir: File? = null): java.lang.Process {
        val homeDir = prepareEnvironment(context)
        val targetDir = workingDir ?: homeDir

        val shellPath = findShellPath()
        val processBuilder = ProcessBuilder(shellPath, "-i")

        processBuilder.directory(if (targetDir.exists()) targetDir else context.filesDir)

        val env = processBuilder.environment()
        val binDir = File(context.filesDir, "bin").absolutePath
        val systemPath = System.getenv("PATH") ?: "/system/bin:/system/xbin:/vendor/bin"
        
        env["HOME"] = homeDir.absolutePath
        env["PATH"] = "$binDir:$systemPath:/apex/com.android.runtime/bin:/apex/com.android.art/bin"
        env["TMPDIR"] = File(context.cacheDir, "tmp").absolutePath
        env["TERM"] = "xterm-256color"
        env["COLORTERM"] = "truecolor"
        env["SHELL"] = shellPath
        env["LANG"] = "en_US.UTF-8"
        env["LC_ALL"] = "en_US.UTF-8"
        env["PS1"] = "\\033[1;32mReTerminal\\033[0m:\\033[1;34m\\w\\033[0m\\$ "
        env["USER"] = "u0_a" + (Process.myUid() % 100000)

        processBuilder.redirectErrorStream(true)
        return processBuilder.start()
    }

    private fun findShellPath(): String {
        val candidates = listOf(
            "/system/bin/sh",
            "/system/xbin/sh",
            "/bin/sh",
            "/system/bin/toybox",
            "/system/bin/toolbox"
        )
        for (path in candidates) {
            val file = File(path)
            if (file.exists() && file.canExecute()) {
                return path
            }
        }
        return "/system/bin/sh"
    }
}
