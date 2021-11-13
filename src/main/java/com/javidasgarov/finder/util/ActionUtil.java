package com.javidasgarov.finder.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static java.awt.datatransfer.DataFlavor.stringFlavor;

public class ActionUtil {
    public static Optional<String> getSelectedText(@NotNull AnActionEvent event) {
        Editor editor = event.getData(EDITOR);
        return Optional.ofNullable(editor.getSelectionModel().getSelectedText());
    }

    @SneakyThrows
    public static String getClipboardContent() {
        return CopyPasteManager.getInstance()
                .getContents().getTransferData(stringFlavor).toString();
    }

    public static void moveToThatAnnotation(Project project, PsiAnnotation annotation) {
        new OpenFileDescriptor(project, annotation.getContainingFile().getVirtualFile(), annotation.getTextOffset())
                .navigate(true);
    }
}
