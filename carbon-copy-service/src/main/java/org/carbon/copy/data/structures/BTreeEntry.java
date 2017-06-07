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

package org.carbon.copy.data.structures;

/**
 * always remember
 * internal nodes: only use key and childNode
 * leaf nodes: only use key and value
 */
class BTreeEntry<Key extends Comparable<Key>, Value> {
    private Key key;
    private final Value val;
    // helper field to iterate over array entries
    private BTreeNode<Key, Value> childNode;

    BTreeEntry(Key key, Value val) {
        this(key, val, null);
    }

    BTreeEntry(Key key, BTreeNode<Key, Value> childNode) {
        this(key, null, childNode);
    }

    BTreeEntry(Key key, Value val, BTreeNode<Key, Value> childNode) {
        this.key  = key;
        this.val  = val;
        this.childNode = childNode;
    }

    Key getKey() {
        return key;
    }

    void setKey(Key key) {
        this.key = key;
    }

    Value getValue() {
        return val;
    }

    BTreeNode<Key, Value> getChildNode() {
        return childNode;
    }

    void setChildNode(BTreeNode<Key, Value> childNode) {
        this.childNode = childNode;
    }
}
