package com.javidasgarov.finder.service;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.javidasgarov.finder.comparator.PsiAnnotationComparator.firstAppearsInFile;
import static com.javidasgarov.finder.comparator.PsiAnnotationComparator.longestUrlFirst;
import static com.javidasgarov.finder.util.AnnotationUtil.resolveAnnotationValues;
import static com.javidasgarov.finder.util.TextUtil.appendPrefixToAllValues;
import static com.javidasgarov.finder.util.UrlUtil.isAMatch;

@UtilityClass
public class FinderService {

    private static final List<String> REQUEST_MAPPING_ANNOTATIONS = List.of(
            "@RequestMapping",
            "@GetMapping",
            "@PostMapping",
            "@PatchMapping",
            "@PutMapping",
            "@DeleteMapping",
            "@Path"
    );
    public static final String VALUE_ATTRIBUTE = "value";
    public static final String PATH_ATTRIBUTE = "path";

    public static List<Entry<PsiAnnotation, List<String>>> findMatchingAnnotations(
            PsiJavaFile controller,
            String searchUrl) {
        List<PsiAnnotation> controllerAnnotations = getControllerAnnotations(controller);

        int classDeclarationOffset = controller.getContainingFile().getText().indexOf("public class ");
        Optional<PsiAnnotation> prefixAnnotation = getPrefixAnnotation(controllerAnnotations, classDeclarationOffset);

        prefixAnnotation.ifPresent(controllerAnnotations::remove);
        return findMatchingAnnotations(prefixAnnotation, controllerAnnotations, searchUrl);
    }

    public static List<PsiAnnotation> getControllerAnnotations(PsiJavaFile controller) {
        return PsiTreeUtil.findChildrenOfType(controller, PsiAnnotation.class)
                .stream()
                .filter(FinderService::isControllerAnnotation)
                .collect(Collectors.toList());
    }

    public static List<String> getPrefixes(PsiAnnotation prefixAnnotation) {
        List<String> prefixes = resolveAnnotationValues(prefixAnnotation);

        if (prefixes.isEmpty()) {
            return List.of("");
        }
        return prefixes;
    }

    private static boolean isControllerAnnotation(PsiAnnotation psiAnnotation) {
        String annotationName = psiAnnotation.getText();
        if (annotationName.contains("(")) {
            annotationName = psiAnnotation.getText().substring(0, psiAnnotation.getText().indexOf("("));
        }
        return REQUEST_MAPPING_ANNOTATIONS.contains(annotationName);
    }

    public static Optional<PsiAnnotation> getPrefixAnnotation(List<PsiAnnotation> controllerAnnotations,
                                                              int classDeclarationOffset) {
        return controllerAnnotations.stream()
                .filter(annotation -> annotation.getTextOffset() < classDeclarationOffset)
                .findFirst();
    }

    public static List<Entry<PsiAnnotation, List<String>>> findMatchingAnnotations(
            Optional<PsiAnnotation> prefixAnnotation,
            List<PsiAnnotation> controllerAnnotations,
            String searchUrl) {
        List<String> prefixes = prefixAnnotation.map(FinderService::getPrefixes)
                .orElseGet(() -> List.of(""));

        Map<PsiAnnotation, List<String>> annotationUrls = controllerAnnotations.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        annotation -> generateUrls(annotation, prefixes)
                ));

        return annotationUrls.entrySet().stream()
                .filter(entry -> isAMatch(entry.getValue(), searchUrl))
                .collect(Collectors.toList());
    }

    @NotNull
    public static Optional<PsiAnnotation> getPsiAnnotation(String searchUrl, List<PsiJavaFile> controllerFiles) {
        return controllerFiles
                .stream()
                .map(controller -> findMatchingAnnotations(controller, searchUrl))
                .flatMap(Collection::stream)
                .sorted(longestUrlFirst.thenComparing(firstAppearsInFile))
                .map(Entry::getKey)
                .findFirst();
    }

    static List<String> generateUrls(PsiAnnotation annotation, List<String> prefixes) {
        return prefixes.stream().
                map(prefix -> appendPrefixToAllValues(prefix, resolveAnnotationValues(annotation)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
