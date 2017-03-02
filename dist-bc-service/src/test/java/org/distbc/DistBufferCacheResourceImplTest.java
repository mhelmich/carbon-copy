package org.distbc;

import org.distbc.data.structures.DataStructureModule;
import org.distbc.data.structures.TxnManagerModule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
public class DistBufferCacheResourceImplTest {
    @Test
    public void testBasic() {
        DistBufferCacheResource r = new DistBufferCacheResourceImpl(null);
        Set<Object> rs = r.query("select * from table1");
        assertTrue(rs.size() == 0);
    }
}
