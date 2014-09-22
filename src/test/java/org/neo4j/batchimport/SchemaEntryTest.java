package org.neo4j.batchimport;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static org.neo4j.graphdb.DynamicLabel.label;
import static org.neo4j.helpers.collection.MapUtil.stringMap;

/**
 * @author mh
 * @since 11.06.13
 */
public class SchemaEntryTest
{
    @Test
    public void testCreateConfigEntry() throws Exception
    {
        assertEquals( stringMap( "batch_import.schema.Person.person_id", "indexed" ),
                new SchemaEntry( "Person", "person_id", "indexed" ).addToConfig( stringMap() ) );
    }

    @Test
    public void testReadFromConfigEntry() throws Exception
    {
        final SchemaEntry info = SchemaEntry.fromConfigEntry(
                stringMap( "batch_import.schema.Person.person_id", "indexed" ).entrySet().iterator().next() );
        assertEquals( label( "Person" ), info.label );
        assertEquals( "person_id", info.propertyKey );
        assertEquals( SchemaEntry.Type.INDEXED, info.type );
    }

    @Test
    public void testInvalidSchemaEntry() throws Exception
    {
        try
        {
            new SchemaEntry( "Person", "person_id", "unrecognised_index_type" );
            fail( "Should have thrown exception" );
        }
        catch ( Exception e )
        {
            assertEquals( "Schema entry value must be one of [INDEXED, UNIQUE], but got [unrecognised_index_type]", e.getMessage() );
        }
    }

}
