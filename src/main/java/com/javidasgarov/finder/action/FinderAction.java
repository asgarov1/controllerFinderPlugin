package com.javidasgarov.finder.action;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static com.javidasgarov.finder.service.FinderService.findMatchingMethod;
import static com.javidasgarov.finder.util.ActionUtil.*;
import static com.javidasgarov.finder.util.FileUtil.findControllerFiles;
import static java.util.Optional.ofNullable;

public class FinderAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(ofNullable(event.getProject()).isPresent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        String searchUrl = getSelectedText(event).orElse(getClipboardContent());
        List<PsiJavaFile> controllerFiles = findControllerFiles(project);

        Optional<PsiAnnotation> matchedAnnotation = controllerFiles.stream()
                .map(controller -> findMatchingMethod(controller, searchUrl))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        matchedAnnotation.ifPresentOrElse(annotation -> moveToThatAnnotation(project, annotation),
                () -> HintManager.getInstance().showInformationHint(event.getData(EDITOR),
                        "Couldn't find any controller for '" + searchUrl + "'"));
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
