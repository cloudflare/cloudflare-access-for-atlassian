package com.cloudflare.access.atlassian.base.support;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class VersionComparatorTest {

    @Parameters(name = "{index}: compare({0}, {1}) = {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 { "0.0.0", "0.0.0", 0 },
                 { "1.0.0", "1.0.0", 0 },
                 { "0.2.0", "0.2.0", 0 },
                 { "0.0.3", "0.0.3", 0 },
                 { "1.2.3", "1.2.3", 0 },

                 { "1.0.0", "0.0.0", 1 },
                 { "2.0.0", "1.0.0", 1 },
                 { "1.2.0", "0.2.0", 1 },
                 { "0.0.3", "0.0.2", 1 },
                 { "2.6.0", "2.5.1", 1 },
                 { "9.91.91", "9.9.9", 1 },

                 { "0.0.0", "2.5.0", -1 },
                 { "0.99.0", "1.0.0", -1 },
                 { "0.0.0", "0.0.1", -1 },
                 { "1.5.0", "2.0.1", -1 },
                 { "2.6.1", "2.6.2", -1 },
                 { "2.0.0", "3.0.0", -1 },
           });
    }


	private String left, right;
	private int expectedResult;

	public VersionComparatorTest(String left, String right, int expectedResult) {
		this.left = left;
		this.right = right;
		this.expectedResult = expectedResult;
	}

	@Test
	public void test() {
		int actual = VersionComparator.INSTANCE.compare(left, right);
		assertEquals(expectedResult, actual);
	}
}
