package org.neo4j.batchimport;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.parboiled.common.StringUtils;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import static java.lang.String.format;

public class SchemaEntry
{
    public final Label label;
    public final String propertyKey;
    public final Type type;

    public SchemaEntry( String label, String propertyKey, String type )
    {
        this.label = DynamicLabel.label( label );
        this.propertyKey = propertyKey;
        try
        {
            this.type = Type.valueOf( type.toUpperCase() );
        }
        catch ( IllegalArgumentException e )
        {
            throw new IllegalArgumentException( format( "Schema entry value must be one of [%s], but got [%s]",
                    StringUtils.join( Type.values(), ", " ), type ) );
        }
    }

    public static SchemaEntry fromConfigEntry( Map.Entry<String, String> entry )
    {
        Pattern pattern = Pattern.compile( "^batch_import\\.schema\\.(.+)\\.(.+)" );
        Matcher matcher = pattern.matcher( entry.getKey() );
        if ( !matcher.matches() )
        {
            return null;
        }
        final String label = matcher.group( 1 );
        final String propertyKey = matcher.group( 2 );
        final String type = entry.getValue();
        return new SchemaEntry( label, propertyKey, type );
    }

    public String getConfigKey()
    {
        return "batch_import.schema." + label + "." + propertyKey;
    }

    public String getConfigValue()
    {
        return type.toString().toLowerCase();
    }

    public Map<String, String> addToConfig( Map<String, String> config )
    {
        config.put( getConfigKey(), getConfigValue() );
        return config;
    }

    enum Type
    {
        INDEXED
                {
                    @Override public void create( SchemaEntry schemaEntry, BatchInserter db )
                    {
                        db.createDeferredSchemaIndex( schemaEntry.label ).on( schemaEntry.propertyKey ).create();

                    }
                },
        UNIQUE
                {
                    @Override public void create( SchemaEntry schemaEntry, BatchInserter db )
                    {
                        db.createDeferredConstraint( schemaEntry.label ).assertPropertyIsUnique( schemaEntry.propertyKey
                        ).create();
                    }
                };

        public abstract void create( SchemaEntry schemaEntry, BatchInserter db );
    }
}
