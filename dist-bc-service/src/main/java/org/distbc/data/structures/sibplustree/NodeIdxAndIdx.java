package org.distbc.data.structures.sibplustree;

class NodeIdxAndIdx {
    static NodeIdxAndIdx INVALID = NodeIdxAndIdx.of(-1, -1);

    final int nodeIdx;
    final int idx;
    private NodeIdxAndIdx(int nodeIdx, int idx) {
        this.nodeIdx = nodeIdx;
        this.idx = idx;
    }

    @Override
    public String toString() {
        return nodeIdx + "_" + idx;
    }

    static NodeIdxAndIdx of(int nodeIdx, int idx) {
        return new NodeIdxAndIdx(nodeIdx, idx);
    }
}
