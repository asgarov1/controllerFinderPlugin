package com.javidasgarov.finder.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.PsiImplUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AnnotationUtilTest {

    @Test
    void testResolveAnnotationValues_whenAnnotationIsNull() {
        List<String> strings = AnnotationUtil.resolveAnnotationValues(null);
        assertNotNull(strings);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "public final static String ORDER = \"myorder\";",
            "public final static String ORDER = SOME_OTHER_CONSTANT + \"myorder\";",
            "public final static String ORDER = \"\" + \"myorder\";"
    })
    void testResolveAnnotationValues_whenValueIsConstant(String constantLine) {
        // GIVEN that I have a mock annotation that returns a constant value
        PsiAnnotation mockAnnotation = Mockito.mock(PsiAnnotation.class);
        when(mockAnnotation.getText()).thenReturn("ORDER");


        try (MockedStatic<PsiImplUtil> psiImplUtilMock = Mockito.mockStatic(PsiImplUtil.class)) {

            // AND I mocked the intermittent operations to return a constantLine
            PsiAnnotationMemberValue psiAnnotationMemberValueMock = Mockito.mock(PsiAnnotationMemberValue.class);
            PsiReference psiReferenceMock = Mockito.mock(PsiReference.class);
            PsiElement psiElementMock = Mockito.mock(PsiElement.class);

            psiImplUtilMock.when(() -> PsiImplUtil.findAttributeValue(any(), any())).thenReturn(psiAnnotationMemberValueMock);
            when(psiReferenceMock.resolve()).thenReturn(psiElementMock);
            when(psiAnnotationMemberValueMock.getReference()).thenReturn(psiReferenceMock);
            when(psiElementMock.getText()).thenReturn(constantLine);

            // WHEN I call resolveAnnotationValues
            List<String> result = AnnotationUtil.resolveAnnotationValues(mockAnnotation);

            // THEN the correct value is returned
            assertEquals("myorder", result.get(0));
        }
    }

    @Test
    void testResolveAnnotationValues_whenValueIsStringLiteral() {
        // GIVEN that I have a mock annotation that returns a constant value
        PsiAnnotation mockAnnotation = Mockito.mock(PsiAnnotation.class);
        when(mockAnnotation.getText()).thenReturn("\"ORDER\"");


        try (MockedStatic<PsiImplUtil> psiImplUtilMock = Mockito.mockStatic(PsiImplUtil.class)) {

            // AND I mocked the intermittent operations to return a constantLine
            PsiAnnotationMemberValue psiAnnotationMemberValueMock = Mockito.mock(PsiAnnotationMemberValue.class);
            PsiElement psiElementMock = Mockito.mock(PsiElement.class);

            psiImplUtilMock.when(() -> PsiImplUtil.findAttributeValue(any(), any())).thenReturn(psiAnnotationMemberValueMock);
            when(psiAnnotationMemberValueMock.getChildren()).thenReturn(new PsiElement[]{psiElementMock});
            when(psiElementMock.getText()).thenReturn("myorder");

            // WHEN I call resolveAnnotationValues
            List<String> result = AnnotationUtil.resolveAnnotationValues(mockAnnotation);

            // THEN the correct value is returned
            assertEquals("myorder", result.get(0));
        }
    }

    @Test
    void testResolveAnnotationValues_whenValueIsEmpty() {
        // GIVEN that I have a mock annotation that returns a constant value
        PsiAnnotation mockAnnotation = Mockito.mock(PsiAnnotation.class);
        when(mockAnnotation.getText()).thenReturn("\"ORDER\"");


        try (MockedStatic<PsiImplUtil> psiImplUtilMock = Mockito.mockStatic(PsiImplUtil.class)) {

            // AND I mocked the intermittent operations to return a constantLine
            psiImplUtilMock.when(() -> PsiImplUtil.findAttributeValue(any(), any())).thenReturn(null);

            // WHEN I call resolveAnnotationValues
            List<String> result = AnnotationUtil.resolveAnnotationValues(mockAnnotation);

            // THEN the correct value is returned
            assertEquals(1, result.size());
            assertEquals("", result.get(0));
        }
    }
}