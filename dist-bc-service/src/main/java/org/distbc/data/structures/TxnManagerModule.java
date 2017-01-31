package org.distbc.data.structures;

import com.google.inject.AbstractModule;

public class TxnManagerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TxnManager.class).to(TxnManagerImpl.class);
    }
}
