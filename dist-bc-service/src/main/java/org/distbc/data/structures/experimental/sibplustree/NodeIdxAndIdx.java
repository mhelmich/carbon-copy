/*
 *
 *  Copyright 2017 Marco Helmich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.distbc.data.structures.experimental.sibplustree;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !NodeIdxAndIdx.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        NodeIdxAndIdx other = (NodeIdxAndIdx) obj;
        return this.nodeIdx == other.nodeIdx && this.idx == other.idx;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 53 + nodeIdx;
        hash = hash * 53 + idx;
        return hash;
    }

    static NodeIdxAndIdx of(int nodeIdx, int idx) {
        return new NodeIdxAndIdx(nodeIdx, idx);
    }
}
