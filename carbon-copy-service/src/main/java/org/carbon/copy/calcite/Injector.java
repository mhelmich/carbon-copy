package org.carbon.copy.calcite;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.carbon.copy.data.structures.Catalog;
import org.carbon.copy.data.structures.DataStructureFactory;

/**
 * Ok, this sucks...a lot!
 * The SchemaFactory is created by calcite and
 * I'm not able to inject my guice objects in any way, shape, or form.
 * That's why I'm using guice' static injection to fake it until I make it.
 */
class Injector {
    @Inject
    static Provider<Catalog> catalogProvider;

    @Inject
    static Provider<DataStructureFactory> dataStructureFactoryProvider;

    static Catalog getCatalog() {
        return catalogProvider.get();
    }

    static DataStructureFactory getDataStructureFactory() {
        return dataStructureFactoryProvider.get();
    }
}
