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

package org.distbc.data.structures;

import co.paralleluniverse.galaxy.Cluster;
import co.paralleluniverse.galaxy.Grid;
import co.paralleluniverse.galaxy.Messenger;
import co.paralleluniverse.galaxy.Store;
import com.google.inject.AbstractModule;

/**
 * This class configures all things data structures and below
 */
public class DataStructureModule extends AbstractModule {
    @Override
    protected void configure() {
        Grid g;
        try {
            g = Grid.getInstance("../config/peer.xml", "../config/peer.properties");
            if (!g.cluster().isOnline()) {
                g.goOnline();
            }
        } catch (Exception xcp) {
            // when we catch any exception, there's no point in bringing this node online
            // fail the startup
            throw new RuntimeException(xcp);
        }

        bind(Store.class).toInstance(g.store());
        bind(Cluster.class).toInstance(g.cluster());
        bind(Messenger.class).toInstance(g.messenger());

        bind(InternalDataStructureFactory.class).to(DataStructureFactoryImpl.class);
        bind(DataStructureFactory.class).to(DataStructureFactoryImpl.class);

        bind(Catalog.class).to(CatalogImpl.class);
    }
}
