package org.distbc.data.structures.sibplustree;

class Breadcrumb<K extends Comparable<K>> {
    final InternalNodeGroup<K> ing;
    final NodeIdxAndIdx indexes;
    private Breadcrumb(InternalNodeGroup<K> ing, NodeIdxAndIdx indexes) {
        this.ing = ing;
        this.indexes = indexes;
    }

    @Override
    public String toString() {
        return indexes.toString() + "_" + ing.toString();
    }

    static <K extends Comparable<K>> Breadcrumb<K> of(InternalNodeGroup<K> ing, NodeIdxAndIdx indexes) {
        return new Breadcrumb<>(ing, indexes);
    }
}
