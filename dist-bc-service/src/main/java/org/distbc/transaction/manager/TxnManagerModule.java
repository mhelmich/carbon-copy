package org.distbc.transaction.manager;

import com.google.inject.AbstractModule;

public class TxnManagerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TxnManager.class).to(TxnManagerImpl.class);
    }
}
