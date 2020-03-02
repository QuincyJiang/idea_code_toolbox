package actions

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import org.apache.commons.io.FileUtils
import utils.loge
import java.io.File

class CreateFileRunnable(
    private val outputFile: String,
    private val content: String,
    private val fileEncoding: String,
    private val dataContext: DataContext
) : Runnable {

    override fun run() {
        try {
            val manager = VirtualFileManager.getInstance()
            var virtualFile = manager
                .refreshAndFindFileByUrl(VfsUtil.pathToUrl(outputFile))

            if (virtualFile != null && virtualFile.exists()) {
                virtualFile.setBinaryContent(content.toByteArray(charset(fileEncoding)))
            } else {
                val file = File(outputFile)
                FileUtils.writeStringToFile(file, content, fileEncoding)
                virtualFile = manager.refreshAndFindFileByUrl(VfsUtil.pathToUrl(outputFile))
            }
            val finalVirtualFile = virtualFile
            val project = DataKeys.PROJECT.getData(dataContext)
            if (finalVirtualFile == null || project == null) {
                loge("CreateFile failed")
                return
            }
            ApplicationManager.getApplication()
                .invokeLater {
                    FileEditorManager.getInstance(project).openFile(
                        finalVirtualFile, true,
                        true
                    )
                }

        } catch (ex: Exception) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showMessageDialog(
                    "Unknown Charset: $fileEncoding, please use the correct charset",
                    "Generate Failed",
                    null
                )
            }
        } catch (e: Exception) {
            loge("Create file failed, ${e.message}")
        }

    }
}