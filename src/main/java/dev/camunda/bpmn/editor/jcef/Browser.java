package dev.camunda.bpmn.editor.jcef;

import static com.intellij.ui.jcef.JBCefBrowserBase.Properties.NO_CONTEXT_MENU;

import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import dev.camunda.bpmn.editor.ui.component.popupmenu.EditorPopupMenu;
import java.util.function.Function;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;

/**
 * A wrapper class for JBCefBrowser that provides additional functionality for handling
 * JavaScript queries and load events.
 *
 * @author Oleksandr Havrysh
 */
public class Browser extends JBCefBrowser {

    /**
     * Constructs a new JBCefBrowserWrapper instance with specific configurations.
     *
     * @param popupMenu The popup menu to be associated with the browser
     */
    public Browser(EditorPopupMenu popupMenu) {
        super(JBCefBrowser.createBuilder()
                .setOffScreenRendering(false)
                .setMouseWheelEventEnable(true)
                .setEnableOpenDevToolsMenuItem(true));
        setProperty(NO_CONTEXT_MENU, true);
        getComponent().setComponentPopupMenu(popupMenu);
    }

    /**
     * Registers a runnable to be executed when the page load ends.
     *
     * @param runnable The runnable to be executed on load end
     */
    public void onLoadEnd(Runnable runnable) {
        myCefClient.addLoadHandler(new CefLoadHandlerAdapter() {

            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                runnable.run();
            }
        }, myCefBrowser);
    }

    /**
     * Creates a JBCefJSQuery instance with the specified handler.
     *
     * @param handler A function that handles the response from the JavaScript query
     * @return A configured JBCefJSQuery instance
     */
    public JBCefJSQuery createJBCefJSQuery(Function<? super String, ? extends JBCefJSQuery.Response> handler) {
        var jbCefJSQuery = JBCefJSQuery.create((JBCefBrowserBase) this);
        jbCefJSQuery.addHandler(handler);
        return jbCefJSQuery;
    }

    /**
     * Executes the specified JavaScript query on the associated JBCefBrowser.
     *
     * @param query The JavaScript query to be executed
     */
    public void executeQuery(String query) {
        myCefBrowser.executeJavaScript(query, getCefBrowser().getURL(), 0);
    }
}