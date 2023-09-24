package com.javidasgarov.finder.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.psi.impl.PsiImplUtil.findAttributeValue;
import static com.javidasgarov.finder.service.FinderService.PATH_ATTRIBUTE;
import static com.javidasgarov.finder.service.FinderService.VALUE_ATTRIBUTE;

public class AnnotationUtil {
    public static List<String> resolveAnnotationValues(PsiAnnotation annotation) {
        if (isAnnotationsValueConstant(annotation)) {
            // if contains a constant get the actual value of a constant
            return Stream.of(
                            findAttributeValue(annotation, VALUE_ATTRIBUTE),
                            findAttributeValue(annotation, PATH_ATTRIBUTE))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(PsiAnnotationMemberValue::getReference)
                    .map(PsiReference::resolve)
                    .map(PsiElement::getText)
                    .map(constantDeclaration -> constantDeclaration.substring(constantDeclaration.indexOf("\"") + 1, constantDeclaration.lastIndexOf("\"")))
                    .stream()
                    .collect(Collectors.toList());
        } else {
            return getValues(annotation);
        }
    }

    public static List<String> getValues(PsiAnnotation annotation) {
        return Stream.of(
                        findAttributeValue(annotation, VALUE_ATTRIBUTE),
                        findAttributeValue(annotation, PATH_ATTRIBUTE))
                .filter(Objects::nonNull)
                .map(PsiElement::getChildren)
                .map(TextUtil::getAnnotationValues)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static boolean isAnnotationsValueConstant(PsiAnnotation annotation) {
        return !annotation.getText().contains("\"");
    }
}
