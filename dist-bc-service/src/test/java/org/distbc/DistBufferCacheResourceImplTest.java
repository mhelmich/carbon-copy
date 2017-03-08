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
