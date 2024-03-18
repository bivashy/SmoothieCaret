package com.bivashy.plugin.intellij.caret;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretVisualAttributes;
import com.intellij.openapi.editor.CaretVisualAttributes.Shape;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;

public class VisualCaretAnimator implements CaretListener, DocumentListener {

    private final Map<Caret, SmoothCaret> smoothCarets = new HashMap<>();
    private final Editor editor;

    public VisualCaretAnimator(Editor editor) {
        this.editor = editor;

        ApplicationManager.getApplication().invokeLater(() ->
                editor.getCaretModel().getAllCarets().forEach(this::smoothCaret));
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        Caret caret = event.getCaret();
        if (caret == null)
            return;
        if(!this.smoothCarets.containsKey(caret))
            this.smoothCaret(caret);
        SmoothCaret smoothCaret = this.smoothCarets.get(caret);
        Point2D to = editor.visualPositionToPoint2D(editor.logicalToVisualPosition(event.getNewPosition()));
        smoothCaret.move(to);
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        if (event.getDocument().isInBulkUpdate())
            return;
        for (Caret caret : editor.getCaretModel().getAllCarets()) {
            SmoothCaret smoothCaret = this.smoothCarets.get(caret);
            smoothCaret.move();
        }
    }

    @Override
    public void caretAdded(@NotNull CaretEvent event) {
        Caret caret = event.getCaret();
        if (caret == null)
            return;
        this.smoothCaret(caret);
    }

    @Override
    public void caretRemoved(@NotNull CaretEvent event) {
        Caret caret = event.getCaret();
        if (caret == null)
            return;
        this.smoothCarets.remove(caret);
    }

    private void smoothCaret(Caret caret) {
        this.smoothCarets.put(caret, new SmoothCaret(caret));
        hideCaret(caret);
    }

    private void hideCaret(Caret caret) {
        CaretVisualAttributes attributes = caret.getVisualAttributes();
        ApplicationManager.getApplication().invokeLater(() ->
                caret.setVisualAttributes(new CaretVisualAttributes(attributes.getColor(), attributes.getWeight(), Shape.UNDERSCORE, 0)));
    }

    public void repaintCarets() {
        this.smoothCarets.values().forEach(SmoothCaret::repaint);
    }

}
