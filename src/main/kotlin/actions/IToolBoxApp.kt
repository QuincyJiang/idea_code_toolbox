package actions

import com.intellij.openapi.components.ServiceManager

interface IToolBoxApp {
    companion object {
        val instance: IToolBoxApp
            get() = ServiceManager.getService(IToolBoxApp::class.java)
    }
}
