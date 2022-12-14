package me.deotime.syncd.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.UIManager
import javax.swing.filechooser.FileFilter


object FileSelector {

    init {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }

    suspend fun selectFile(
        prompt: String = "Select file",
        files: Boolean = true,
        directories: Boolean = true,
        filter: (File?) -> Boolean = { true }
    ): File? {
        val frame = JFrame("test").apply { isVisible = true }
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        val selector = JFileChooser().apply {
            fileSelectionMode = when {
                files && directories -> JFileChooser.FILES_AND_DIRECTORIES
                files -> JFileChooser.FILES_ONLY
                directories -> JFileChooser.DIRECTORIES_ONLY
                else -> return null
            }
            dialogTitle = prompt
            fileFilter = object : FileFilter() {
                override fun accept(f: File?) = filter(f)
                override fun getDescription() = "File selector"
            }
        }


        return withContext(Dispatchers.IO + NonCancellable) {
            frame.requestFocus()
            selector.showOpenDialog(frame)
            launch {
                delay(1) // swing throws weird exceptions if you dont wait
                frame.dispose()
            }
            selector.selectedFile
        }
    }

}