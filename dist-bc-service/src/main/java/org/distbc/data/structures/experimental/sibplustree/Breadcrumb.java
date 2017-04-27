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
