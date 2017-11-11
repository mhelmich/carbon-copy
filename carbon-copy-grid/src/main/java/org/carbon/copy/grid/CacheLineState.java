/*
 * Copyright 2017 Marco Helmich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.carbon.copy.grid;

/**
 * MOESI cache line transition states
 * see http://developer.amd.com/wordpress/media/2012/10/24593_APM_v21.pdf
 * for more details
 */
enum CacheLineState {
    // A cache line in the invalid state does not hold a valid copy of the data.
    // Valid copies of the data can be either in main memory or another processor cache.
    INVALID,
    // A cache line in the exclusive state holds the most recent, correct copy of the data.
    // The copy in main memory is also the most recent, correct copy of the data.
    // No other processor holds a copy of the data.
    EXCLUSIVE,
    // A cache line in the shared state holds the most recent, correct copy of the data.
    // Other processors in the system may hold copies of the data in the shared state, as well.
    // If no other processor holds it in the owned state, then the copy in main memory is also the most recent.
    SHARED,
    // A cache line in the modified state holds the most recent, correct copy of the data.
    // The copy in main memory is stale (incorrect), and no other processor holds a copy.
    MODIFIED,
    // A cache line in the owned state holds the most recent, correct copy of the data.
    // The owned state is similar to the shared state in that other processors can hold a copy of the most recent,
    // correct data. Unlike the shared state, however, the copy in main memory can be stale (incorrect).
    // Only one processor can hold the data in the owned state—all other processors must hold the data in
    // the shared state.
    OWNED,
}
