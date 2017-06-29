package org.carbon.copy.calcite;

import org.apache.calcite.schema.SchemaPlus;

import java.util.Map;

/**
 * This is the entry point for calcite.
 * This class is referenced by a model (aka connection string).
 */
@SuppressWarnings("UnusedDeclaration")
public class SchemaFactory implements org.apache.calcite.schema.SchemaFactory {
    // must have a public, no parameter constructor as per factory contract
    public SchemaFactory() { }

    @Override
    public org.apache.calcite.schema.Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {
        return new Schema(Injector.getCatalog());
    }
}
