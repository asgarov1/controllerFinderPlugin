package com.javidasgarov.finder.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiJavaFile;
import com.javidasgarov.finder.util.ActionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.javidasgarov.finder.service.FinderService.getPsiAnnotation;
import static com.javidasgarov.finder.service.NotificationService.*;
import static com.javidasgarov.finder.util.ActionUtil.getSelectedText;
import static com.javidasgarov.finder.util.ActionUtil.moveToThatAnnotation;
import static com.javidasgarov.finder.util.FileUtil.findControllerFiles;
import static java.util.Optional.ofNullable;

public class FinderAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(ofNullable(event.getProject()).isPresent());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Optional<String> searchUrlOptional = getSearchUrl(event);
        if (searchUrlOptional.isEmpty()) {
            displayMustSelectSomethingMessage(event);
            return;
        }

        Project project = event.getProject();
        List<PsiJavaFile> controllerFiles = findControllerFiles(project);

        String searchUrl = searchUrlOptional.get();
        Optional<PsiAnnotation> matchedAnnotation = getPsiAnnotation(searchUrl, controllerFiles);
        matchedAnnotation.ifPresentOrElse(
                annotation -> {
                    moveToThatAnnotation(project, annotation);
                    displayMatchFoundMessage(event, searchUrl);
                },
                () -> displayNotFoundMessage(event, searchUrl));
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }

    private Optional<String> getSearchUrl(AnActionEvent event) {
        return getSelectedText(event)
                .or(ActionUtil::getClipboardContent)
                .map(url -> {
                    if (url.contains("?")) {
                        return url.substring(0, url.indexOf("?"));
                    }
                    return url;
                })
                .map(String::trim);
    }
}
