package com.bivashy.plugin.intellij.caret;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.Disposer;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

public class SmoothyStartupActivity implements ProjectActivity {

    private final Map<Editor, VisualCaretAnimator> caretAnimators = new HashMap<>();

    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            registerAnimator(editor);
        }

        EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
            @Override
            public void editorCreated(@NotNull EditorFactoryEvent event) {
                registerAnimator(event.getEditor());
            }

            @Override
            public void editorReleased(@NotNull EditorFactoryEvent event) {
                caretAnimators.remove(event.getEditor());
            }
        }, Disposer.newDisposable("EditorFactory animator listener"));

        IdeEventQueue.getInstance().addDispatcher((awtEvent) -> {
            caretAnimators.values().forEach(VisualCaretAnimator::repaintCarets);
            return false;
        }, Disposer.newDisposable("test"));
        return null;
    }

    private void registerAnimator(Editor editor) {
        if (caretAnimators.containsKey(editor))
            return;
        VisualCaretAnimator caretAnimator = new VisualCaretAnimator(editor);
        caretAnimators.put(editor, caretAnimator);
        editor.getCaretModel().addCaretListener(caretAnimator);
        editor.getDocument().addDocumentListener(caretAnimator);
    }

}
