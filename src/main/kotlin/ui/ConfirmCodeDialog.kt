package ui

import model.GeneratedSourceCode
import utils.centerDialog
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class ConfirmCodeDialog(var code: GeneratedSourceCode?, var listener: onConfirmListener?) : JDialog() {
    private var contentPane: JPanel? = null
    private var buttonOK: JButton? = null
    private var buttonCancel: JButton? = null
    private var codeContent: JTextArea? = null

    init {
        centerDialog(  700, 600)
        setSize(700, 600)
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK

        buttonOK!!.addActionListener { onOK() }

        buttonCancel!!.addActionListener { onCancel() }

        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                onCancel()
            }
        })

        contentPane!!.registerKeyboardAction(
            { onCancel() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
        initCodeContent()
    }
    private fun initCodeContent() {
        code?.let {
            codeContent?.text = it.sourceCode
        }
    }

    private fun onOK() {
        dispose()
        codeContent?.text?.let { contentPaneEditedText ->
            code?.let {
               val result = GeneratedSourceCode::class.java.newInstance()
                result.className = it.className
                result.sourceCode = contentPaneEditedText
                listener?.onSelected(result)
            }
        }
    }

    private fun onCancel() {
        dispose()
    }
}
interface onConfirmListener {
    fun onSelected(code: GeneratedSourceCode?)
}

