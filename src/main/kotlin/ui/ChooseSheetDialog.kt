package ui

import model.HiidoStaticSheet
import utils.OnConfirmListener
import utils.centerDialog
import java.awt.Dimension
import java.awt.Insets
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

/**
 * 读取Excel files 后选择目标sheets
 * */
class ChooseSheetDialog(private var mData: HashMap<String, HiidoStaticSheet>?, var listener: OnConfirmListener<HiidoStaticSheet?>?) : JDialog() {
    private var contentPane: JPanel? = null
    private var buttonOK: JButton? = null
    private var buttonCancel: JButton? = null
    private var headerView: JPanel? = null
    private var sheetsList: JPanel? = null
    private var buttonGroup: ButtonGroup? = null


    init {
        centerDialog(400, 300)
        setContentPane(contentPane)
        buttonGroup = ButtonGroup()
        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK!!.addActionListener { onOK() }
        buttonCancel!!.addActionListener { onCancel() }
        // call onCancel() when cross is clicked
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
        addHeader()
        addSheetsList()
    }


    private fun addHeader() {
        headerView!!.layout = BoxLayout(headerView, BoxLayout.X_AXIS)
        val label1 = JLabel("选择表格")
        label1.preferredSize = Dimension(100, 25)
        headerView!!.add(label1)
    }

    private fun addSheetsList() {
        sheetsList!!.layout = BoxLayout(sheetsList, BoxLayout.Y_AXIS)
        mData?.forEach { sheetsName, _ ->
            val radioButton = JRadioButton(sheetsName)
            radioButton.margin = Insets(20, 20, 20, 20)
            sheetsList?.add(radioButton)
            buttonGroup?.add(radioButton)
        }
    }

    private fun onOK() {
        dispose()
        mData?.let { map ->
            val sheetName = buttonGroup?.elements?.toList()?.filter {
                it.isSelected
            }?.get(0)?.text
            sheetName?.let {
                listener?.onConfirm(map[it])
            }
        }
    }

    private fun onCancel() {
        dispose()
    }
}
