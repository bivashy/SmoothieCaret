package com.bivashy.plugin.intellij.caret.view;

import static com.bivashy.plugin.intellij.caret.proxy.EditorPainterProxy.SessionProxy.createSession;
import static com.bivashy.plugin.intellij.caret.proxy.EditorPainterProxy.SessionProxy.paintCaretBar;
import static com.bivashy.plugin.intellij.caret.proxy.EditorPainterProxy.SessionProxy.paintCaretBlock;
import static com.bivashy.plugin.intellij.caret.proxy.EditorPainterProxy.SessionProxy.paintCaretBox;
import static com.bivashy.plugin.intellij.caret.proxy.EditorPainterProxy.SessionProxy.paintCaretText;
import static com.bivashy.plugin.intellij.caret.proxy.EditorPainterProxy.SessionProxy.paintCaretUnderscore;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

import com.bivashy.plugin.intellij.caret.SmoothCaret;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretVisualAttributes;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.view.EditorView;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.scale.JBUIScale;

public class CaretPainter extends JComponent implements ComponentListener {

    private final SmoothCaret smoothCaret;
    private final Caret caret;
    private final EditorImpl editor;
    private final EditorView editorView;
    private final Insets contentInsets;
    private final CaretVisualAttributes attributes;

    public CaretPainter(SmoothCaret smoothCaret, Caret caret, EditorImpl editor, EditorView editorView) {
        super();
        this.smoothCaret = smoothCaret;
        this.caret = caret;
        this.editor = editor;
        this.editorView = editorView;
        this.contentInsets = this.editor.getContentComponent().getInsets();
        this.attributes = this.caret.getVisualAttributes();
        setVisible(true);
        this.update();
    }

    @Override
    public void componentResized(ComponentEvent componentEvent) {
        this.update();
        this.repaint();
    }

    @Override
    public void componentMoved(ComponentEvent componentEvent) {
        this.update();
        this.repaint();
    }

    @Override
    public void componentShown(ComponentEvent componentEvent) {
    }

    @Override
    public void componentHidden(ComponentEvent componentEvent) {
    }

    public void update() {
        this.setBounds(editor.getScrollingModel().getVisibleArea());
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (editor.isPurePaintingMode())
            return;
        Graphics2D g = IdeBackgroundUtil.getOriginalGraphics(graphics);

        Object session = createSession(editorView, g);
        int nominalLineHeight = editorView.getNominalLineHeight();
        int topOverhang = editorView.getTopOverhang();
        EditorSettings settings = editor.getSettings();
        Color caretColor = editor.getColorsScheme().getColor(EditorColors.CARET_COLOR);
        if (caretColor == null)
            caretColor = new JBColor(JBColor.DARK_GRAY, JBColor.LIGHT_GRAY);
        int minX = contentInsets.left;
        Point2D point = smoothCaret.position();
        float x = (float) point.getX();
        int yShift = -editor.getScrollingModel().getVisibleArea().y;
        int y = (int) point.getY() - topOverhang + yShift;
        g.setColor(attributes.getColor() != null ? attributes.getColor() : caretColor);
        boolean isRtl = caret.isAtRtlLocation();
        float width = caret.getVisualAttributes().getWidth(1);
        float startX = isRtl ? x - width : x;
        CaretVisualAttributes.Shape shape = attributes.getShape();
        switch (shape) {
            case DEFAULT -> {
                if (editor.isInsertMode() != settings.isBlockCursor()) {
                    int lineWidth = JBUIScale.scale(attributes.getWidth(settings.getLineCursorWidth()));
                    if (lineWidth > 1)
                        x -= 1 / JBUIScale.sysScale(g);
                    paintCaretBar(session, g, caret, x, y, lineWidth, nominalLineHeight, isRtl);
                } else {
                    paintCaretBlock(g, startX, y, width, nominalLineHeight);
                    paintCaretText(session, g, caret, caretColor, startX, y, topOverhang, isRtl);
                }
            }
            case BLOCK -> {
                paintCaretBlock(g, startX, y, width, nominalLineHeight);
                paintCaretText(session, g, caret, caretColor, startX, y, topOverhang, isRtl);
            }
            case BAR -> {
                // Don't draw if thickness is zero. This allows a plugin to "hide" carets, e.g. to visually emulate a block selection as a
                // selection rather than as multiple carets with discrete selections
                if (attributes.getThickness() > 0) {
                    int barWidth = Math.max((int) (width * attributes.getThickness()), JBUIScale.scale(settings.getLineCursorWidth()));
                    if (!isRtl && x > minX && barWidth > 1 && barWidth < (width / 2))
                        x -= 1 / JBUIScale.sysScale(g);
                    paintCaretBar(session, g, caret, isRtl ? x - barWidth : x, y, barWidth, nominalLineHeight, isRtl);
                    Shape savedClip = g.getClip();
                    g.setClip(new Rectangle2D.Float(isRtl ? x - barWidth : x, y, barWidth, nominalLineHeight));
                    paintCaretText(session, g, caret, caretColor, startX, y, topOverhang, isRtl);
                    g.setClip(savedClip);
                }
            }
            case UNDERSCORE -> {
                if (attributes.getThickness() > 0) {
                    int underscoreHeight = Math.max((int) (nominalLineHeight * attributes.getThickness()), 1);
                    paintCaretUnderscore(g, startX, y + nominalLineHeight - underscoreHeight, width, underscoreHeight);
                    Shape oldClip = g.getClip();
                    g.setClip(new Rectangle2D.Float(startX, y + nominalLineHeight - underscoreHeight, width, underscoreHeight));
                    paintCaretText(session, g, caret, caretColor, startX, y, topOverhang, isRtl);
                    g.setClip(oldClip);
                }
            }
            case BOX -> paintCaretBox(g, startX, y, width, nominalLineHeight);
        }
    }

}
