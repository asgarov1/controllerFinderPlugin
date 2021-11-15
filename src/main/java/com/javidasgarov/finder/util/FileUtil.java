package com.javidasgarov.finder.util;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.psi.search.GlobalSearchScope.projectScope;

@UtilityClass
public class FileUtil {

    public static final String PATH = "@Path";
    public static final String CONTROLLER = "@Controller";
    public static final String REST_CONTROLLER = "@RestController";
    public static final String NOT_CONTINUED_BY_LETTER_REGEX = "(?![A-z])";

    public static List<PsiJavaFile> findControllerFiles(Project project) {
        return FileTypeIndex.getFiles(JavaFileType.INSTANCE, projectScope(project))
                .stream()
                .filter(FileUtil::hasControllerAnnotation)
                .map(PsiManager.getInstance(project)::findFile)
                .map(PsiJavaFile.class::cast)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private static boolean hasControllerAnnotation(VirtualFile virtualFile) {
        String fileContent = new String(virtualFile.contentsToByteArray());

        return Stream.of(CONTROLLER, REST_CONTROLLER, PATH)
                .map(word -> word + NOT_CONTINUED_BY_LETTER_REGEX)
                .map(Pattern::compile)
                .anyMatch(pattern -> pattern.matcher(fileContent).find());
    }

}
