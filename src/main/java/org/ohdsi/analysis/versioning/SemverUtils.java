package org.ohdsi.analysis.versioning;

import org.semver4j.Semver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SemverUtils {

    private SemverUtils() {
    }

    /**
     * @param ranges A list of version ranges using limited node semver. e.g '5.3', '>=5.2.0', '3 - 5', '<6.0.0'.
     *               Ranges can be specified only using primitive ranges '<' '<=' '>' '>=' and hyphen ranges e.g '3.0 - 5.2', as per the spec this is inclusive,
     *               don't add other operators or range types. Tilde ~, caret ^ and pipe || are not supported.
     * @return The intersection between the provided ranges, if no intersection exists null is returned
     * @throws IllegalArgumentException is thrown when ranges is null or empty or contains invalid or unsupported version range syntax, is thrown when range contains invalid or unsupported version range syntax
     * @author Rowan Parry
     */
    public static String getRangesIntersection(List<String> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            throw new IllegalArgumentException("ranges parameter can not be null or empty");
        }
        Set<String> distinctRanges = new HashSet<>(ranges);
        if (distinctRanges.size() == 1) {
            return ranges.get(0);
        }
        List<String> validRanges = findValidRanges(distinctRanges);
        if (validRanges.isEmpty()) {
            return null;
        } else if (validRanges.size() == 1) {
            return validRanges.get(0);
        } else {
            return handleBoundedRange(validRanges);
        }
    }

    private static List<String> findValidRanges(Set<String> ranges) {
        return ranges.stream()
                .filter(r -> !r.equals("*")) // the star is useless
                .map(rng -> {
                    List<String> items = splitRange(rng);
                    List<String> result = new ArrayList<>();
                    if (items.size() == 1) {
                        Semver semver = findBoundary(rng);
                        if (ranges.stream().allMatch(semver::satisfies)) {
                            result.add(rng);
                        }
                    } else {
                        Semver lower = findBoundary(items.get(0));
                        if (ranges.stream().allMatch(lower::satisfies)) {
                            result.add(">=" + lower);
                        }
                        Semver upper = findBoundary(items.get(1));
                        if (ranges.stream().allMatch(upper::satisfies)) {
                            result.add("<=" + items.get(1));
                        }
                    }
                    return result;
                })
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    // split bounded ranges into upper and lower boundary
    private static List<String> splitRange(String range) {
        if (range.contains("-")) {
            String[] boundaries = range.split("-");
            return Arrays.asList(boundaries[0].trim(), boundaries[1].trim());
        } else {
            return Collections.singletonList(range.trim());
        }
    }

    private static Semver findBoundary(String range) {
        String withoutOperator = removeOperator(range);
        Semver boundary = Semver.coerce(withoutOperator);
        if (withoutOperator.equals(range) || range.contains("=")) {
            return boundary;
        } else if (range.contains(">")) {
            return boundary.withIncPatch();
        } else if (range.contains("<")) {
            if (boundary.getPatch() != 0) {
                return boundary.withIncPatch(-1);
            } else if (boundary.getMinor() != 0) {
                return boundary.withIncMinor(-1);
            } else {
                return new Semver(boundary.getMajor() - 1 + ".99.99");
            }
        } else {
            throw new IllegalArgumentException(range + " is not a valid version range. Please use only things  5.3, >=5.2.0, 3 - 5, <6.0.0");
        }
    }

    private static String removeOperator(String range) {
        return range.replaceAll("[^\\d.]", "");
    }

    private static String handleBoundedRange(List<String> satisfactoryRanges) {
        Semver max = satisfactoryRanges.stream().map(SemverUtils::findBoundary).max(Semver::compareTo).get();
        Semver min = satisfactoryRanges.stream().map(SemverUtils::findBoundary).min(Semver::compareTo).get();
        if (max.equals(min)) {
            return max.toString();
        } else {
            return min + " - " + max;
        }
    }
}
