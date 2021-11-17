package com.javidasgarov.finder.comparator;

import com.intellij.psi.PsiAnnotation;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import static com.javidasgarov.finder.util.TextUtil.PLACEHOLDER_REGEX;

@UtilityClass
public class PsiAnnotationComparator {

    public static final Comparator<Entry<PsiAnnotation, List<String>>> longestUrlFirst = PsiAnnotationComparator::longestUrlFirst;
    public static final Comparator<Entry<PsiAnnotation, List<String>>> firstAppearsInFile = PsiAnnotationComparator::firstAppearsInFile;

    private static int longestUrlFirst(Entry<PsiAnnotation, List<String>> a,
                                      Entry<PsiAnnotation, List<String>> b) {
        return getLongestUrlCharCount(b.getValue()) - getLongestUrlCharCount(a.getValue());
    }

    private static int getLongestUrlCharCount(List<String> urls) {
        return urls.stream()
                .map(PsiAnnotationComparator::bringPlaceholdersToUniformLength)
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    @NotNull
    private static String bringPlaceholdersToUniformLength(String url) {
        return url.replaceAll(PLACEHOLDER_REGEX, "{id}");
    }

    private static int firstAppearsInFile(Entry<PsiAnnotation, List<String>> a,
                                         Entry<PsiAnnotation, List<String>> b) {
        return a.getKey().getTextOffset() - b.getKey().getTextOffset();
    }
}
