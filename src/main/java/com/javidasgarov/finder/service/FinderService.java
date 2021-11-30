package com.javidasgarov.finder.service;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.javidasgarov.finder.util.TextUtil;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.psi.impl.PsiImplUtil.findAttributeValue;
import static com.javidasgarov.finder.comparator.PsiAnnotationComparator.firstAppearsInFile;
import static com.javidasgarov.finder.comparator.PsiAnnotationComparator.longestUrlFirst;
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
        List<String> prefixes = getAttributeValueOrPath(prefixAnnotation)
                .map(TextUtil::getAnnotationValues)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

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

    private static List<String> generateUrls(PsiAnnotation annotation, List<String> prefixes) {
        return prefixes.stream().
                map(prefix -> appendPrefixToAllValues(prefix, getValues(annotation)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static List<String> getValues(PsiAnnotation annotation) {
        return getAttributeValueOrPath(annotation)
                .map(TextUtil::getAnnotationValues)
                .filter(Predicate.not(List::isEmpty))
                .findFirst()
                .orElse(List.of(""));
    }

    /**
     * RequestMapping can be written either
     * with value attribute (e.g. @GetMapping("/bookings"))
     * or with path attribute (e.g. @GetMapping(path = "/bookings"))
     * <p>
     * Therefore this method gets children of both and concatenates them into a stream
     *
     * @param annotation
     * @return
     */
    private static Stream<PsiElement[]> getAttributeValueOrPath(PsiAnnotation annotation) {
        return Stream.of(
                        findAttributeValue(annotation, VALUE_ATTRIBUTE),
                        findAttributeValue(annotation, PATH_ATTRIBUTE))
                .filter(Objects::nonNull)
                .map(PsiElement::getChildren);
    }
}
