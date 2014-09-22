package org.neo4j.batchimport;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.MapUtil.stringMap;

/**
 * @author mh
 * @since 11.06.13
 */
public class LegacyIndexInfoTest
{

    private static final String INDEX_FILE = "target/node_index.csv";

    @Test
    public void testCreateConfigEntry() throws Exception {
        assertEquals(stringMap("batch_import.legacy.node_index.foo", "exact"), new LegacyIndexInfo("node_index", "foo", "exact", null).addToConfig(stringMap()));
    }

    @Test
    public void testReadFromConfigEntry() throws Exception {
        final LegacyIndexInfo info = LegacyIndexInfo.fromConfigEntry( stringMap( "batch_import.legacy.node_index" +
                ".foo", "exact:file" ).entrySet().iterator().next() );
        assertEquals("node_index",info.elementType);
        assertEquals("foo",info.indexName);
        assertEquals("exact",info.indexType);
        assertEquals("file",info.indexFileName);
    }

    @Test
    public void testReadFromDeprecatedConfigEntry() throws Exception {
        final LegacyIndexInfo info = LegacyIndexInfo.fromConfigEntry( stringMap( "batch_import.node_index.foo",
                "exact:file" ).entrySet().iterator().next() );
        assertEquals("node_index",info.elementType);
        assertEquals("foo",info.indexName);
        assertEquals("exact",info.indexType);
        assertEquals("file",info.indexFileName);
    }

    @Test
    public void testCreateFromParams() throws Exception {
        final LegacyIndexInfo info = new LegacyIndexInfo(new String[]{"relationship_index", "bar", "fulltext", "file"},0);
        assertEquals("relationship_index",info.elementType);
        assertEquals("bar",info.indexName);
        assertEquals("fulltext",info.indexType);
        assertEquals("file",info.indexFileName);
    }
    @Test
    public void testCreateFromParamsWithOffset() throws Exception {
        final LegacyIndexInfo info = new LegacyIndexInfo(new String[]{"a","b","relationship_index", "bar", "fulltext", "file"},2);
        assertEquals("relationship_index",info.elementType);
        assertEquals("bar",info.indexName);
        assertEquals("fulltext",info.indexType);
        assertEquals("file",info.indexFileName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidIndexType() throws Exception {
        new LegacyIndexInfo("node_index","foo","bar",null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidElementType() throws Exception {
        new LegacyIndexInfo("foo","exact","bar",null);
    }

    @Test
    public void testShouldImportFile() throws Exception {
        assertEquals(false, new LegacyIndexInfo("node_index","name","exact",null).shouldImportFile());
        assertEquals(false, new LegacyIndexInfo("node_index","name","exact", "target").shouldImportFile());
        assertEquals(false, new LegacyIndexInfo("node_index","name","exact", INDEX_FILE).shouldImportFile());
        final FileOutputStream fos = new FileOutputStream(INDEX_FILE);
        fos.write(0);
        fos.close();
        assertEquals(true, new LegacyIndexInfo("node_index","name", "exact", INDEX_FILE).shouldImportFile());
        new File(INDEX_FILE).delete();
    }
}
