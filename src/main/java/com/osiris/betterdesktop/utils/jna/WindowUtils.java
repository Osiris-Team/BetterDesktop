package com.osiris.betterdesktop.utils.jna;

import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.system.windows.User32.*;

public class WindowUtils {
    /**
     * @param window GLFW window.
     */
    public static void hideTaskbarIcon(long window) {
        long hwnd = glfwGetWin32Window(window);
        SetWindowLongPtr(hwnd, GWL_EXSTYLE, WS_EX_TOOLWINDOW);
    }
}