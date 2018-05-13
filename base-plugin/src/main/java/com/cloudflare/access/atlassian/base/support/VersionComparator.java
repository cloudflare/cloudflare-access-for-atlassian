package com.cloudflare.access.atlassian.base.support;

import java.util.Arrays;
import java.util.Comparator;

public class VersionComparator implements Comparator<String>{

	public static final VersionComparator INSTANCE = new VersionComparator();

	private VersionComparator() {}

	@Override
	public int compare(String left, String right) {
		int[] leftComponents = Arrays.stream(left.split("\\.")).mapToInt(Integer::parseInt).toArray();
		int[] rightComponents = Arrays.stream(right.split("\\.")).mapToInt(Integer::parseInt).toArray();
		for(int i = 0; i < 3; i++) {
			int l = leftComponents[i];
			int r = rightComponents[i];
			if(l != r) {
				return Integer.signum(l - r);
			}
		}
		return 0;
	}


}
