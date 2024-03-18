package com.bivashy.plugin.intellij.caret.proxy;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.impl.view.EditorPainter;
import com.intellij.openapi.editor.impl.view.EditorView;

public class EditorPainterProxy {

    public static final String CLASS_NAME = EditorPainter.class.getName();
    public static class SessionProxy {

        public static final String CLASS_NAME = EditorPainterProxy.CLASS_NAME + "$Session";
        private static Class<?> SESSION_CLASS;
        private static Method PAINT_CARET_BAR;
        private static Method PAINT_CARET_BLOCK;
        private static Method PAINT_CARET_UNDERSCORE;
        private static Method PAINT_CARET_BOX;
        private static Method PAINT_CARET_TEXT;

        static {
            try {
                SESSION_CLASS = Class.forName(CLASS_NAME);
                // Session#paintCaretBar(Graphics2D, float, float, float, float, boolean)
                PAINT_CARET_BAR = SESSION_CLASS.getDeclaredMethod("paintCaretBar",
                        Graphics2D.class, Caret.class, float.class, float.class, float.class, float.class, boolean.class);
                // Session#paintCaretBlock(Graphics2D, float, float, float, float)
                PAINT_CARET_BLOCK = SESSION_CLASS.getDeclaredMethod("paintCaretBlock",
                        Graphics2D.class, float.class, float.class, float.class, float.class);
                // Session#paintCaretUnderscore(Graphics2D, float, float, float, float)
                PAINT_CARET_UNDERSCORE = SESSION_CLASS.getDeclaredMethod("paintCaretUnderscore",
                        Graphics2D.class, float.class, float.class, float.class, float.class);
                // Session#paintCaretBox(Graphics2D, float, float, float, float)
                PAINT_CARET_BOX = SESSION_CLASS.getDeclaredMethod("paintCaretBox",
                        Graphics2D.class, float.class, float.class, float.class, float.class);
                // Session#paintCaretText(Graphics2D, Caret, Color, float, float, int, boolean)
                PAINT_CARET_TEXT = SESSION_CLASS.getDeclaredMethod("paintCaretText",
                        Graphics2D.class, Caret.class, Color.class, float.class, float.class, int.class, boolean.class);

                PAINT_CARET_BAR.setAccessible(true);
                PAINT_CARET_BLOCK.setAccessible(true);
                PAINT_CARET_UNDERSCORE.setAccessible(true);
                PAINT_CARET_BOX.setAccessible(true);
                PAINT_CARET_TEXT.setAccessible(true);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        public static Object createSession(EditorView editorView, Graphics2D g) {
            try {
                Constructor<?> constructor = SESSION_CLASS.getDeclaredConstructor(EditorView.class, Graphics2D.class);
                constructor.setAccessible(true);
                return constructor.newInstance(editorView, g);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return null;
            }
        }

        public static void paintCaretBar(Object session, Graphics2D g, Caret caret, float x, float y, float w, float h, boolean isRtl) {
            try {
                PAINT_CARET_BAR.invoke(session, g, caret, x, y, w, h, isRtl);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public static void paintCaretBlock(Graphics2D g, float x, float y, float w, float h) {
            try {
                PAINT_CARET_BLOCK.invoke(null, g, x, y, w, h);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public static void paintCaretUnderscore(Graphics2D g, float x, float y, float w, float h) {
            try {
                PAINT_CARET_UNDERSCORE.invoke(null, g, x, y, w, h);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public static void paintCaretBox(Graphics2D g, float x, float y, float w, float h) {
            try {
                PAINT_CARET_BOX.invoke(null, g, x, y, w, h);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public static void paintCaretText(Object session, Graphics2D g, Caret caret, Color caretColor, float x, float y, int topOverhang, boolean isRtl) {
            try {
                PAINT_CARET_TEXT.invoke(session, g, caret, caretColor, x, y, topOverhang, isRtl);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

}
