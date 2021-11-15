package com.javidasgarov.finder.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.Transferable;
import java.util.Optional;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static java.awt.datatransfer.DataFlavor.stringFlavor;

@UtilityClass
public class ActionUtil {
    public static Optional<String> getSelectedText(@NotNull AnActionEvent event) {
        return Optional.ofNullable(event.getData(EDITOR))
                .map(Editor::getSelectionModel)
                .map(SelectionModel::getSelectedText);
    }

    @SneakyThrows
    public static Optional<String> getClipboardContent() {
        Optional<Transferable> contents = Optional.ofNullable(CopyPasteManager.getInstance().getContents());
        if (contents.isPresent()) {
            return Optional.ofNullable(contents.get().getTransferData(stringFlavor).toString());
        }
        return Optional.empty();
    }

    public static void moveToThatAnnotation(Project project, PsiAnnotation annotation) {
        new OpenFileDescriptor(project, annotation.getContainingFile().getVirtualFile(), annotation.getTextOffset())
                .navigate(true);
    }
}
