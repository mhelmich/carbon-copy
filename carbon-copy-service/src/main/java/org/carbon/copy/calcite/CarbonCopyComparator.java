package org.carbon.copy.calcite;

import java.util.Comparator;

@SuppressWarnings("UnusedDeclaration")
public final class CarbonCopyComparator implements Comparator<Comparable> {
    public static final CarbonCopyComparator COMPARATOR = new CarbonCopyComparator();

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Comparable o1, Comparable o2) {
        if (o1 == o2) return 0;
        return o1.compareTo(o2);
    }
}
