package com.javidasgarov.finder.service;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.javidasgarov.finder.util.TextUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.psi.impl.PsiImplUtil.findAttributeValue;
import static com.javidasgarov.finder.util.TextUtil.appendPrefixToAllValues;
import static com.javidasgarov.finder.util.TextUtil.getAnnotationValues;
import static com.javidasgarov.finder.util.UrlUtil.isAMatch;

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
    public static final String VALUE = "value";

    public static Optional<PsiAnnotation> findMatchingMethod(PsiJavaFile controller, String searchUrl) {
        List<PsiAnnotation> controllerAnnotations = getControllerAnnotations(controller);

        int classDeclarationOffset = controller.getContainingFile().getText().indexOf("public class ");
        Optional<PsiAnnotation> prefixAnnotation = getPrefixAnnotation(controllerAnnotations, classDeclarationOffset);

        prefixAnnotation.ifPresent(controllerAnnotations::remove);
        return findMatchingAnnotation(prefixAnnotation, controllerAnnotations, searchUrl);
    }

    public static List<PsiAnnotation> getControllerAnnotations(PsiJavaFile controller) {
        return PsiTreeUtil.findChildrenOfType(controller, PsiAnnotation.class)
                .stream()
                .filter(FinderService::isControllerAnnotation)
                .collect(Collectors.toList());
    }

    public static List<String> getPrefixes(PsiAnnotation prefixAnnotation) {
        return Stream.of(prefixAnnotation)
                .map(annotation -> findAttributeValue(annotation, VALUE))
                .filter(Objects::nonNull)
                .map(PsiElement::getChildren)
                .map(TextUtil::getAnnotationValues)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
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

    public static Optional<PsiAnnotation> findMatchingAnnotation(Optional<PsiAnnotation> prefixAnnotation,
                                                                 List<PsiAnnotation> controllerAnnotations,
                                                                 String searchUrl) {
        List<String> prefixes = prefixAnnotation.isPresent() ? getPrefixes(prefixAnnotation.get()) : List.of("");
        Map<PsiAnnotation, List<String>> annotationUrls = controllerAnnotations.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        annotation -> generateUrls(annotation, prefixes)
                ));

        return annotationUrls.entrySet().stream()
                .filter(entry -> isAMatch(entry.getValue(), searchUrl))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private static List<String> generateUrls(PsiAnnotation annotation, List<String> prefixes) {
        Optional<PsiAnnotationMemberValue> attributeValue = Optional.ofNullable(findAttributeValue(annotation, VALUE));
        if (attributeValue.isPresent()) {
            List<String> values = getAnnotationValues(attributeValue.get().getChildren());
            return prefixes.stream()
                    .map(prefix -> appendPrefixToAllValues(prefix, values))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        return prefixes;
    }
}
