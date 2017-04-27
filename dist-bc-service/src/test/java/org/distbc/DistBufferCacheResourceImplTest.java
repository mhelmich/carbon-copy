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

package org.distbc;

import com.google.inject.Inject;
import org.distbc.data.structures.DataStructureModule;
import org.distbc.data.structures.TxnManagerModule;
import org.distbc.parser.QueryParser;
import org.distbc.parser.QueryPaserModule;
import org.distbc.planner.QueryPlanner;
import org.distbc.planner.QueryPlannerModule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class, QueryPlannerModule.class, QueryPaserModule.class})
public class DistBufferCacheResourceImplTest {

    @Inject
    private QueryParser queryParser;

    @Inject
    private QueryPlanner queryPlanner;

    @Test(expected = IllegalArgumentException.class)
    public void testNoTablePresent() {
        DistBufferCacheResource r = new DistBufferCacheResourceImpl(null, queryParser, queryPlanner);
        r.query("select * from table1");
    }
}
