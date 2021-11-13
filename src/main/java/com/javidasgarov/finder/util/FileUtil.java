package com.javidasgarov.finder.util;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.psi.search.GlobalSearchScope.projectScope;

public class FileUtil {

    public static final String CONTROLLER = "@Controller";
    public static final String REST_CONTROLLER = "@RestController";

    public static List<PsiClass> findControllerClasses(Project project) {
        return FileTypeIndex.getFiles(JavaFileType.INSTANCE, projectScope(project))
                .stream()
                .filter(FileUtil::hasControllerAnnotation)
                .map(PsiManager.getInstance(project)::findFile)
                .map(FileUtil::getClass)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static List<PsiJavaFile> findControllerFiles(Project project) {
        return FileTypeIndex.getFiles(JavaFileType.INSTANCE, projectScope(project))
                .stream()
                .filter(FileUtil::hasControllerAnnotation)
                .map(PsiManager.getInstance(project)::findFile)
                .map(file -> (PsiJavaFile) file)
                .collect(Collectors.toList());
    }

    private static List<PsiClass> getClass(PsiFile psiFile) {
        return Stream.of(psiFile)
                .map(file -> (PsiJavaFile) file)
                .map(PsiJavaFile::getClasses)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private static boolean hasControllerAnnotation(VirtualFile virtualFile) {
        String fileContent = new String(virtualFile.contentsToByteArray());
        return fileContent.contains(CONTROLLER) || fileContent.contains(REST_CONTROLLER);
    }

}
