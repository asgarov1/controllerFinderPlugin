package com.javidasgarov.finder.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.psi.impl.PsiImplUtil.findAttributeValue;
import static com.javidasgarov.finder.service.FinderService.PATH_ATTRIBUTE;
import static com.javidasgarov.finder.service.FinderService.VALUE_ATTRIBUTE;

public class AnnotationUtil {

    public static final String SPACE_AND_PLUS_SIGN_SURROUNDED_BY_QUOTES = "\"[+\\s]+\"";

    public static List<String> resolveAnnotationValues(PsiAnnotation annotation) {
        if (annotation == null) {
            return List.of();
        }

        List<String> result;
        if (isAnnotationsValueConstant(annotation)) {
            // if contains a constant get the actual value of a constant
            result = Stream.of(
                            findAttributeValue(annotation, VALUE_ATTRIBUTE),
                            findAttributeValue(annotation, PATH_ATTRIBUTE))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(PsiAnnotationMemberValue::getReference)
                    .map(PsiReference::resolve)
                    .map(PsiElement::getText)
                    .map(AnnotationUtil::extractValueFromJavaStatement)
                    .stream()
                    .collect(Collectors.toList());
        } else {
            result = getValues(annotation);
        }

        if (result.isEmpty()) {
            // necessary for prefix concatenation to work
            return List.of("");
        }
        return result;
    }

    /**
     * This method extracts the String literal value from the Java statement.
     * e.g. the input: `public final static String ORDER = \"myorder\";`
     * Then the method returns "myorder"
     * <p>
     * If statement concatenates several Strings then it will remove the quotes and plus signs with regex replace
     *
     * @param input is the Java statement
     * @return the string literal value
     */
    @NotNull
    private static String extractValueFromJavaStatement(String input) {
        int beginIndex = input.indexOf("\"") + 1;
        int endIndex = input.lastIndexOf("\"");
        return input.substring(beginIndex, endIndex)
                .replaceAll(SPACE_AND_PLUS_SIGN_SURROUNDED_BY_QUOTES, "");
    }

    private static List<String> getValues(PsiAnnotation annotation) {
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
