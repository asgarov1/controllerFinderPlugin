package com.javidasgarov.finder.service;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.intellij.notification.NotificationType.ERROR;
import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@UtilityClass
public class NotificationService {

    public static final String SELECT_A_URL_TEXT_OR_HAVE_IT_COPIED_MESSAGE =
            "You must either select a url text or have it copied";
    public static final String COULD_NOT_FIND_MESSAGE = "Couldn't find any controller method for '%s'";
    public static final String CONTROLLER_FINDER_NOTIFICATION_GROUP = "Controller Finder Notification Group";
    public static final String MATCH_FOR_URL = "Match for '%s'";

    public static void displayMustSelectSomethingMessage(@NotNull AnActionEvent event) {
        ofNullable(event.getData(EDITOR)).ifPresentOrElse(
                editor -> HintManager.getInstance().showErrorHint(editor, SELECT_A_URL_TEXT_OR_HAVE_IT_COPIED_MESSAGE),
                () -> createNotification(SELECT_A_URL_TEXT_OR_HAVE_IT_COPIED_MESSAGE, event.getProject()));
    }

    public static void displayNotFoundMessage(@NotNull AnActionEvent event, String searchUrl) {
        ofNullable(event.getData(EDITOR)).ifPresentOrElse(
                editor -> HintManager.getInstance().showErrorHint(editor, format(COULD_NOT_FIND_MESSAGE, searchUrl)),
                () -> createNotification(format(COULD_NOT_FIND_MESSAGE, searchUrl), event.getProject()));
    }

    public static void displayMatchFoundMessage(@NotNull AnActionEvent event, String searchUrl) {
        ofNullable(event.getData(EDITOR)).ifPresent(
                editor -> HintManager.getInstance().showInformationHint(editor, format(MATCH_FOR_URL, searchUrl)));
    }

    private void createNotification(String content, Project project) {
        Optional.ofNullable(project).ifPresent(myProject ->
                NotificationGroupManager.getInstance().getNotificationGroup(CONTROLLER_FINDER_NOTIFICATION_GROUP)
                        .createNotification(content, ERROR)
                        .notify(myProject)
        );
    }
}
