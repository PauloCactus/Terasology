// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWNativeX11;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.rendering.ShaderManager;
import org.terasology.engine.rendering.ShaderManagerLwjgl;
import org.terasology.engine.rendering.nui.internal.LwjglCanvasRenderer;
import org.terasology.engine.rust.EngineKernel;
import org.terasology.engine.utilities.OS;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.nui.canvas.CanvasRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class LwjglGraphics extends BaseLwjglSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(LwjglGraphics.class);

    // we don't use context so we need to
    public static long primaryWindow = 0;

    private Context context;
    private RenderingConfig config;

    private GameEngine engine;
    private LwjglDisplayDevice lwjglDisplay;
//    private EngineKernel kernel;

    private LwjglGraphicsManager graphics = new LwjglGraphicsManager();

    @Override
    public String getName() {
        return "Graphics";
    }

    @Override
    public void initialise(GameEngine gameEngine, Context rootContext) {
        logger.info("Starting initialization of LWJGL");
        this.engine = gameEngine;
        this.context = rootContext;
        this.config = context.get(Config.class).getRendering();
        lwjglDisplay = new LwjglDisplayDevice(context);
        context.put(DisplayDevice.class, lwjglDisplay);
        logger.info("Initial initialization complete");
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        graphics.registerCoreAssetTypes(assetTypeManager);
    }

    @Override
    public void postInitialise(Context rootContext) {
        graphics.registerRenderingSubsystem(context);
//        this.kernel = context.get(EngineKernel.class);

        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_COCOA_GRAPHICS_SWITCHING, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, config.getPixelFormat());
        if (config.getDebug().isEnabled()) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        }
        GLFW.glfwSetErrorCallback(new GLFWErrorCallback());
        logger.info("Initializing display (if last line in log then likely the game crashed from an issue with your " +
                "video card)");

        long window = GLFW.glfwCreateWindow(
                config.getWindowWidth(), config.getWindowHeight(), "Terasology Alpha", 0, 0);

        switch(GLFW.glfwGetPlatform()) {
            case GLFW.GLFW_PLATFORM_X11:
                EngineKernel.instance().initializeWinX11Surface(GLFWNativeX11.glfwGetX11Display(),
                        GLFWNativeX11.glfwGetX11Window(window));
                break;
            default:
                throw new RuntimeException("missing platform: " + GLFW.glfwGetPlatform());
        }


        if (window == 0) {
            throw new RuntimeException("Failed to create window");
        }
        primaryWindow = window;

        EngineKernel.instance().resizeSurface(lwjglDisplay.getWidth(), lwjglDisplay.getHeight());
        if (OS.get() != OS.MACOSX) {
            try {
                String root = "org/terasology/engine/icons/";
                ClassLoader classLoader = getClass().getClassLoader();

                BufferedImage icon16 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_16.png"));
                BufferedImage icon32 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_32.png"));
                BufferedImage icon64 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_64.png"));
                BufferedImage icon128 = ImageIO.read(classLoader.getResourceAsStream(root + "gooey_sweet_128.png"));
                GLFWImage.Buffer buffer = GLFWImage.create(4);
                buffer.put(0, LwjglGraphicsUtil.convertToGLFWFormat(icon16));
                buffer.put(1, LwjglGraphicsUtil.convertToGLFWFormat(icon32));
                buffer.put(2, LwjglGraphicsUtil.convertToGLFWFormat(icon64));
                buffer.put(3, LwjglGraphicsUtil.convertToGLFWFormat(icon128));
                // Not supported on Mac: Code: 65548, Description: Cocoa: Regular windows do not have icons on macOS
                GLFW.glfwSetWindowIcon(window, buffer);

            } catch (IOException | IllegalArgumentException e) {
                logger.warn("Could not set icon", e);
            }
        }

        lwjglDisplay.setDisplayModeSetting(config.getDisplayModeSetting(), false);
        GLFW.glfwShowWindow(window);
        GLFW.glfwSetFramebufferSizeCallback(LwjglGraphics.primaryWindow, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                lwjglDisplay.updateViewport(width, height);
                EngineKernel.instance().resizeSurface(width, height);
            }
        });

        context.put(ShaderManager.class, new ShaderManagerLwjgl());
        context.put(CanvasRenderer.class, new LwjglCanvasRenderer(context));
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        graphics.processActions();

        boolean gameWindowIsMinimized = GLFW.glfwGetWindowAttrib(LwjglGraphics.primaryWindow, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
        if (!gameWindowIsMinimized) {
            EngineKernel kernel = EngineKernel.instance();
            kernel.cmdPrepare();
            currentState.render();
            kernel.cmdDispatch();
        }

        lwjglDisplay.update();
        int frameLimit = context.get(Config.class).getRendering().getFrameLimit();
        if (frameLimit > 0) {
            Lwjgl2Sync.sync(frameLimit);
        }
        if (lwjglDisplay.isCloseRequested()) {
            engine.shutdown();
        }
    }

    @Override
    public void preShutdown() {
//        long window = GLFW.glfwGetCurrentContext();
        if (primaryWindow != MemoryUtil.NULL) {
            boolean isVisible = GLFW.glfwGetWindowAttrib(primaryWindow, GLFW.GLFW_VISIBLE) == GLFW.GLFW_TRUE;
            boolean isFullScreen = lwjglDisplay.isFullscreen();
            if (!isFullScreen && isVisible) {
                int[] xBuffer = new int[1];
                int[] yBuffer = new int[1];
                GLFW.glfwGetWindowPos(primaryWindow, xBuffer, yBuffer);
                int[] widthBuffer = new int[1];
                int[] heightBuffer = new int[1];
                GLFW.glfwGetWindowSize(primaryWindow, widthBuffer, heightBuffer);

                if (widthBuffer[0] > 0 && heightBuffer[0] > 0 && xBuffer[0] > 0 && yBuffer[0] > 0) {
                    config.setWindowWidth(widthBuffer[0]);
                    config.setWindowHeight(heightBuffer[0]);
                    config.setWindowPosX(xBuffer[0]);
                    config.setWindowPosY(yBuffer[0]);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        GLFW.glfwTerminate();
    }

    private void initWindow() {

    }

}
