package com.bivashy.plugin.intellij.caret;

import java.awt.geom.Point2D;

import javax.swing.*;

import com.bivashy.plugin.intellij.caret.view.CaretPainter;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.editor.impl.view.EditorView;
import com.intellij.util.ReflectionUtil;

public class SmoothCaret {

    private final Caret caret;
    private final CaretPainter painter;
    private final int duration = 300;
    private Point2D position;
    private Timer caretAnimation;

    public SmoothCaret(Caret caret) {
        this.caret = caret;
        Editor editor = caret.getEditor();
        this.painter = new CaretPainter(this, caret, (EditorImpl) editor, ReflectionUtil.getField(editor.getClass(), editor, EditorView.class, "myView"));

        this.position = editor.visualPositionToPoint2D(caret.getVisualPosition());
        editor.getContentComponent().add(painter);
        editor.getContentComponent().addComponentListener(painter);
    }

    public static Point2D calculateProgress(Point2D startPoint, Point2D targetPoint, double progress) {
        Point2D point = new Point2D.Double();
        if (startPoint != null && targetPoint != null) {
            point.setLocation(
                    calculateProgress(startPoint.getX(), targetPoint.getX(), progress),
                    calculateProgress(startPoint.getY(), targetPoint.getY(), progress)
            );
        }
        return point;
    }

    public static double calculateProgress(double startValue, double endValue, double fraction) {
        double value;
        double distance = endValue - startValue;
        value = distance * fraction;
        value += startValue;
        return value;
    }

    public void move() {
        Editor editor = caret.getEditor();
        move(editor.visualPositionToPoint2D(caret.getVisualPosition()));
    }

    public void move(Point2D endPoint) {
        long startTime = System.currentTimeMillis();
        if (caretAnimation != null)
            caretAnimation.stop();
        caretAnimation = new Timer(60 / 1000, e -> {
            long now = System.currentTimeMillis();
            long diff = now - startTime;
            if (diff < duration) {
                double progress = (double) diff / (double) duration;
                position = calculateProgress(position, endPoint, progress);
            } else {
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        caretAnimation.start();
        caretAnimation.setRepeats(true);
        caretAnimation.setCoalesce(true);
        caretAnimation.setInitialDelay(0);
    }

    public void repaint() {
        painter.update();
        painter.repaint();
    }

    public Point2D position() {
        return position;
    }

}
