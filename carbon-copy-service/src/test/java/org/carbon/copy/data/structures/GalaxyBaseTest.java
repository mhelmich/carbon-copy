package org.carbon.copy.data.structures;

import com.google.inject.Inject;
import org.carbon.copy.GuiceJUnit4Runner;
import org.carbon.copy.GuiceModules;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(GuiceJUnit4Runner.class)
@GuiceModules({ DataStructureModule.class, TxnManagerModule.class })
abstract class GalaxyBaseTest {
    @Inject
    private GalaxyGrid grid;

    @Before
    public void startGrid() throws InterruptedException {
        if (!grid.isStarted()) {
            grid.start();
        }
    }
}
