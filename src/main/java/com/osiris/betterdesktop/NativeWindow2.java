package com.osiris.betterdesktop;


import com.osiris.betterdesktop.utils.NoExRunnable;
import com.osiris.betterdesktop.utils.jna.WindowUtils;
import com.sun.jna.platform.KeyboardUtils;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.app.Color;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageReader;
import javax.swing.*;
import javax.swing.text.html.ImageView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Low-level abstraction, which creates application window and starts the main loop.
 * It's recommended to use {@link Application}, but this class could be extended directly as well.
 * When extended, life-cycle methods should be called manually.
 */
public class NativeWindow2 {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    public CopyOnWriteArrayList<Runnable> onClose = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<Runnable> onRender = new CopyOnWriteArrayList<>();
    /**
     * Executes on focus gain/loss. The passed over boolean is true on focus gain, and
     * false on focus loss.
     */
    public CopyOnWriteArrayList<Consumer<Boolean>> onFocus = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<Consumer<KeyEvent>> onKey = new CopyOnWriteArrayList<>();
    /**
     * TODO update when changed.
     */
    public int width;
    /**
     * TODO update when changed.
     */
    public int height;
    public NoExRunnable sleepRunnable = () -> {
    };
    /**
     * Pointer to the native GLFW window.
     */
    protected long window;
    /**
     * Background color of the window. Default transparent.
     */
    public Color backgroundColor = new Color(0, 0, 0, 0);
    private String glslVersion = null;

    public NativeWindow2(String title) {
        Rectangle screen = getScreenSize();
        init(screen.width, screen.height, title, new Hints());
    }
    public NativeWindow2(String title, Hints hints) {
        Rectangle screen = getScreenSize();
        init(screen.width, screen.height, title, hints);
    }

    public NativeWindow2(int width, int height, String title) {
        init(width, height, title, new Hints());
    }

    public NativeWindow2(int width, int height, String title, Hints hints) {
        init(width, height, title, hints);
    }

    /**
     * Returns the screen size - task bar.
     */
    public static Rectangle getScreenSize() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle screenSize = gc.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

        int width = screenSize.width - screenInsets.left - screenInsets.right;
        int height = screenSize.height - screenInsets.top - screenInsets.bottom;
        return new Rectangle(width, height);
    }

    public static Rectangle getTaskBarSubstract() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Rectangle screenSize = gc.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        // TODO if window has decoration, add it to this calculation

        int width = screenSize.width - screenInsets.left - screenInsets.right;
        int height = screenSize.height - screenInsets.top - screenInsets.bottom;
        return new Rectangle(screenSize.width - width, screenSize.height - height);
    }

    /**
     * By dividing 1000 milliseconds through 60, we get the millis for 1 iteration. <br>
     * The default is 60 iterations per second. <br>
     */
    public NativeWindow2 fpsLimit(int fps) {
        int sleepMsRender = 1000 / fps;
        sleepRunnable = () -> {
            Thread.sleep(sleepMsRender);
        };
        return this;
    }

    public NativeWindow2 fpsNoLimit() {
        sleepRunnable = () -> {
        };
        return this;
    }

    /**
     * Method to initialize application.
     * Starts with default 60 fps limit.
     *
     * @param config configuration object with basic window information
     */
    protected void init(int width, int height, String title, Hints hints) {
        this.width = width;
        this.height = height;
        fpsLimit(60);
        AtomicBoolean isInit = new AtomicBoolean(false);
        new Thread(() -> {
            initWindow(width, height, title, hints);
            initImGui();
            imGuiGlfw.init(window, true);
            imGuiGl3.init(glslVersion);
            isInit.set(true);
            run(); // Start window render loop, blocks until window is closed
        }).start();
        while (!isInit.get()) {
            Thread.yield();
        }
    }

    /**
     * Method to dispose all used application resources and destroy its window.
     */
    public void close() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        disposeImGui();
        disposeWindow();
    }

    /**
     * Method to create and initialize GLFW window.
     *
     * @param config configuration object with basic window information
     */
    private void initWindow(int width, int height, String title, Hints hints) {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        decideGlGlslVersions();

        hints.map.forEach((key, val) -> {
            glfwWindowHint(key, val);
        });
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);

        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Make sure window is not behind task bar:
        Rectangle taskBarSubstract = getTaskBarSubstract();
        glfwSetWindowPos(window, taskBarSubstract.width, taskBarSubstract.height);

        GLFW.glfwMakeContextCurrent(window);

        GL.createCapabilities();

        GLFW.glfwSwapInterval(GLFW.GLFW_TRUE);

        GLFW.glfwShowWindow(window);

        clearBuffer();
        renderBuffer();

        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                KeyEvent keyEvent = new KeyEvent(key, scancode, action, mods);
                for (Consumer<KeyEvent> consumer : onKey) {
                    consumer.accept(keyEvent);
                }
            }
        });
        glfwSetWindowFocusCallback(window, new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long window, boolean isFocus) {
                for (Consumer<Boolean> f : onFocus) {
                    f.accept(isFocus);
                }
            }
        });
        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(final long window, final int width, final int height) {
                try {
                    runFrame();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void decideGlGlslVersions() {
        final boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        if (isMac) {
            glslVersion = "#version 150";
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);  // 3.2+ only
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);          // Required on Mac
        } else {
            glslVersion = "#version 130";
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 0);
        }
    }

    /**
     * Method to initialize Dear ImGui context. Could be overridden to do custom Dear ImGui setup before application start.
     *
     * @param config configuration object with basic window information
     */
    protected void initImGui() {
        ImGui.createContext();
    }

    /**
     * Method called every frame, before calling {@link #process()} method.
     */
    protected void preProcess() {
    }

    /**
     * Method called every frame, after calling {@link #process()} method.
     */
    protected void postProcess() {
    }

    /**
     * Main application loop.
     */
    protected void run() {
        try {
            while (!GLFW.glfwWindowShouldClose(window)) {
                runFrame();
                sleepRunnable.run();
            }
            for (Runnable runnable : onClose) {
                runnable.run();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method used to run the next frame.
     */
    protected void runFrame() throws Exception {
        startFrame();
        preProcess();
        for (Runnable runnable : onRender) {
            runnable.run();
        }
        postProcess();
        endFrame();
    }

    /**
     * Method used to clear the OpenGL buffer.
     */
    private void clearBuffer() {
        GL32.glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), backgroundColor.getAlpha());
        GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Method called at the beginning of the main cycle.
     * It clears OpenGL buffer and starts an ImGui frame.
     */
    protected void startFrame() {
        clearBuffer();
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    /**
     * Method called in the end of the main cycle.
     * It renders ImGui and swaps GLFW buffers to show an updated frame.
     */
    protected void endFrame() {
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupWindowPtr);
        }

        renderBuffer();
    }

    /**
     * Method to render the OpenGL buffer and poll window events.
     */
    private void renderBuffer() {
        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }

    /**
     * Method to destroy Dear ImGui context.
     */
    protected void disposeImGui() {
        ImGui.destroyContext();
    }

    /**
     * Method to destroy GLFW window.
     */
    protected void disposeWindow() {
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        Objects.requireNonNull(GLFW.glfwSetErrorCallback(null)).free();
    }

    /**
     * @return pointer to the native GLFW window
     */
    public final long getHandle() {
        return window;
    }

    public NativeWindow2 move(int x, int y) {
        glfwSetWindowPos(window, x, y);
        return this;
    }

    public NativeWindow2 size(int width, int height) {
        glfwSetWindowSize(window, width, height);
        return this;
    }

    public NativeWindow2 maximize() {
        glfwMaximizeWindow(window);
        return this;
    }

    public NativeWindow2 minimize() {
        glfwIconifyWindow(window);
        return this;
    }

    /**
     * Call after {@link #minimize()} to restore the window.
     */
    public NativeWindow2 restore() {
        glfwRestoreWindow(window);
        return this;
    }

    public NativeWindow2 decorate(boolean b) {
        if (b) glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_TRUE);
        else glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_FALSE);
        return this;
    }

    public NativeWindow2 allwaysOnTop(boolean b) {
        if (b) glfwSetWindowAttrib(window, GLFW_FLOATING, GLFW_TRUE);
        else glfwSetWindowAttrib(window, GLFW_FLOATING, GLFW_FALSE);
        return this;
    }

    public NativeWindow2 focusOnShow(boolean b) {
        if (b) glfwSetWindowAttrib(window, GLFW_FOCUS_ON_SHOW, GLFW_TRUE);
        else glfwSetWindowAttrib(window, GLFW_FOCUS_ON_SHOW, GLFW_FALSE);
        return this;
    }

    /**
     * Currently can only hide the task bar icon
     */
    public NativeWindow2 showIcon(boolean b) {
        WindowUtils.hideTaskbarIcon(window);
        return this;
    }

    public NativeWindow2 focus() {
        glfwFocusWindow(window);
        return this;
    }

    /**
     * Loads the image from the provided relative path (resources folder),
     * and sets it as this windows' icon. Example:<br>
     * Absolute path: C:/User/MyProject/src/resources/icon.png <br>
     * Relative path: icon.png <br>
     */
    public NativeWindow2 iconFromResources(String path) throws IOException, URISyntaxException {
        return icon(UI.loadImageFromStream(NativeWindow2.class.getResourceAsStream(path)).byteBuffer);
    }

    public NativeWindow2 iconFromFile(File file) throws IOException, URISyntaxException {
        return icon(UI.loadImageFromFile(file).byteBuffer);
    }

    public NativeWindow2 icon(ByteBuffer buf) {
        GLFWImage glfwImage= GLFWImage.malloc();
        glfwImage.set(512, 512, buf);
        GLFWImage.Buffer images = GLFWImage.malloc(1);
        images.put(0, glfwImage);

        glfwSetWindowIcon(window, images);

        images.free();
        glfwImage.free();
        return this;
    }

    /**
     * Executes the provided code one time in the render thread/loop.
     * If you want to run it in each render add your runnable to the {@link #onRender} list.
     */
    public NativeWindow2 access(Runnable runnable) {
        onRender.add(new Runnable() {
            @Override
            public void run() {
                onRender.remove(this);
                runnable.run();
            }
        });
        return this;
    }

    /**
     * Not supported officially by GLFW, thus use {@link java.awt.event.KeyEvent} static constants instead.<br>
     * Creates a new thread that checks every 500ms if the provided keys are pressed,
     * and executes the code if they actually are.
     * Note that the chances are high that a key event will be missed since we only check every 500ms,
     * thus make it clear to your user to press and hold the desired key(s) a bit longer than normally.
     * <pre>
     *     onKeysPressedGlobal(new int[]{KeyEvent.VK_A}, () -> {});
     * </pre>
     */
    public void onKeysPressedGlobal(int[] keys, Runnable code) {
        new Thread(() -> {
            try {
                while (true) {
                    boolean allPressed = true;
                    for (int key : keys) {
                        if (!KeyboardUtils.isPressed(key)) {
                            allPressed = false;
                            break;
                        }
                    }
                    if (allPressed)
                        code.run();
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Executes the provided code when all provided keys are pressed. <br>
     * If you want to listen globally use {@link #onKeysPressedGlobal(int[], Runnable)}.
     * <pre>
     *     onKeysPressed(new int[]{GLFW_KEY_A}, () -> {});
     * </pre>
     */
    public NativeWindow2 onKeysPressed(int[] keys, Runnable code) {
        if (keys == null) return this;
        boolean[] pressed = new boolean[keys.length];
        onKey.add(event -> {
            for (int i = 0; i < keys.length; i++) {
                int key = keys[i];
                if (key == event.key) {
                    if (event.action == GLFW_PRESS)
                        pressed[i] = true;
                    else if (event.action == GLFW_RELEASE)
                        pressed[i] = false;
                    boolean allPressed = true;
                    for (int j = 0; j < pressed.length; j++) {
                        if (!pressed[j]) {
                            allPressed = false;
                            break;
                        }
                    }
                    if (allPressed)
                        code.run();
                    break;
                }
            }
        });
        return this;
    }

    public static class Hints{
        public Map<Integer, Integer> map = new HashMap<>();

        public Hints maximize(boolean b) {
            if (b) map.put(GLFW_MAXIMIZED, GLFW_TRUE);
            else map.put(GLFW_MAXIMIZED, GLFW_FALSE);
            return this;
        }

        public Hints decorate(boolean b) {
            if (b) map.put(GLFW_DECORATED, GLFW_TRUE);
            else map.put(GLFW_DECORATED, GLFW_FALSE);
            return this;
        }

        public Hints allwaysOnTop(boolean b) {
            if (b) map.put(GLFW_FLOATING, GLFW_TRUE);
            else map.put(GLFW_FLOATING, GLFW_FALSE);
            return this;
        }

        public Hints focusOnShow(boolean b) {
            if (b) map.put(GLFW_FOCUS_ON_SHOW, GLFW_TRUE);
            else map.put(GLFW_FOCUS_ON_SHOW, GLFW_FALSE);
            return this;
        }
    }

    public class KeyEvent {
        public int key;
        public int scancode;
        /**
         * Possible values: <br>
         * - {@link GLFW#GLFW_PRESS} <br>
         * - {@link GLFW#GLFW_RELEASE} <br>
         * - {@link GLFW#GLFW_REPEAT} <br>
         */
        public int action;
        public int mods;

        public KeyEvent(int key, int scancode, int action, int mods) {
            this.key = key;
            this.scancode = scancode;
            this.action = action;
            this.mods = mods;
        }
    }
}
