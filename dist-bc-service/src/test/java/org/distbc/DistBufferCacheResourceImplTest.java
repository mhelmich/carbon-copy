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

import java.util.Set;

import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class, QueryPlannerModule.class, QueryPaserModule.class})
public class DistBufferCacheResourceImplTest {

    @Inject
    private QueryParser queryParser;

    @Inject
    private QueryPlanner queryPlanner;

    @Test
    public void testBasic() {
        DistBufferCacheResource r = new DistBufferCacheResourceImpl(null, queryParser, queryPlanner);
        Set<Object> rs = r.query("select * from table1");
        assertTrue(rs.size() == 0);
    }
}
