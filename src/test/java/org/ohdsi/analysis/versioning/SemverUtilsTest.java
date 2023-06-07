package org.ohdsi.analysis.versioning;

import com.vdurmont.semver4j.SemverException;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.ohdsi.analysis.versioning.SemverUtils.getRangesIntersection;

public class SemverUtilsTest {


    @Test
    public void testGetRangesIntersectionSingle() {
        String intersection = getRangesIntersection(singletonList(">=5.2"));
        assertEquals(">=5.2", intersection);
    }

    @Test
    public void testGetRangesIntersectionSimple() {
        String intersection = getRangesIntersection(asList(">=5.0.0", ">=3.0.0"));
        assertEquals(">=5.0.0", intersection);
    }

    @Test
    public void testGetRangesIntersectionNoIntersection() {
        String intersection = getRangesIntersection(asList(">=5.0.0", "3.0.0"));
        assertNull(intersection);
    }
    
    @Test
    public void testGetRangesIntersectionWithSame1() {
        String intersection = getRangesIntersection(asList(">=5.0.0", "5.0.0"));
        assertEquals("5.0.0", intersection);
    }

    @Test
    public void testGetRangesIntersectionWithSame2() {
        String intersection = getRangesIntersection(asList(">=5.0.0", ">5.0.0"));
        assertEquals(">5.0.0", intersection);
    }

    @Test
    public void testGetRangesIntersectionWithSame3() {
        String intersection = getRangesIntersection(asList("<=5.0.0", "<5.0.0"));
        assertEquals("<5.0.0", intersection);
    }

    @Test
    public void testGetRangesIntersectionWithSame4() {
        String intersection = getRangesIntersection(asList(">=5.0.0", "<=5.0.0"));
        assertEquals("5.0.0", intersection);
    }

    @Test
    public void testGetRangesIntersectionWithRange1() {
        String intersection = getRangesIntersection(asList(">=3.0.0", "<=5.0.0"));
        assertEquals("3.0.0 - 5.0.0", intersection);
    }

    @Test
    public void testGetRangesIntersectionWithRange2() {
        String intersection = getRangesIntersection(asList("4 - 5", "<=5.0.0"));
        assertEquals("4.0.0 - 5.0.0", intersection);
    }

    @Test
    public void testGetRangesIntersectionWithTwoRanges() {
        String intersection = getRangesIntersection(asList("2.3.0 - 4.5.0", "3.0.0 - 5.3.0", "*"));
        assertEquals("3.0.0 - 4.5.0", intersection);
    }

    @Test
    public void testGetRangesIntersectionWithAstrix() {
        String intersection = getRangesIntersection(asList("*", "<=5.0.0"));
        assertEquals("<=5.0.0", intersection);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRangesIntersectionWithEmptyList() {
        getRangesIntersection(emptyList());
    }

    @Test(expected = SemverException.class)
    public void testGetRangesIntersectionWithJunk() {
        getRangesIntersection(asList("5.3.0", "junk"));
    }

    @Test
    public void testGetRangesIntersectionWithOnlyAstrix() {
        String intersection = getRangesIntersection(singletonList("*"));
        assertEquals("*", intersection);
    }
    
}
