package org.neo4j.batchimport;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author mh
* @since 11.06.13
*/
public class LegacyIndexInfo
{
    public LegacyIndexInfo( String[] args, int offset ) {
        this.elementType = args[offset];
        this.indexName = args[offset+1];
        this.indexType = args[offset+2];
        this.indexFileName = args[offset+3];
    }

    public LegacyIndexInfo( String elementType, String indexName, String indexType, String indexFileName ) {
        if (!(elementType.equals("node_index") || elementType.equals("relationship_index"))) throw new IllegalArgumentException("ElementType has to be node_index or relationship_index, but is "+elementType);
        if (!(indexType.equals("exact") || indexType.equals("fulltext"))) throw new IllegalArgumentException("IndexType has to be exact or fulltext, but is "+indexType);
        this.elementType = elementType;
        this.indexName = indexName;
        this.indexType = indexType;
        this.indexFileName = indexFileName;
    }

    public final String elementType, indexName, indexType, indexFileName;

    public static LegacyIndexInfo fromConfigEntry(Map.Entry<String, String> entry) {
        Pattern pattern = Pattern.compile( "^batch_import(\\.legacy)?\\.(node_index|relationship_index)\\.(.+)" );
        Matcher matcher = pattern.matcher( entry.getKey() );
        if (!matcher.matches()) return null;
        final String elementType = matcher.group( 2 );
        final String indexName = matcher.group( 3 );
        final String[] valueParts = entry.getValue().split(":");
        final String indexType = valueParts[0];
        final String indexFileName = valueParts.length > 1 ? valueParts[1] : null;
        return new LegacyIndexInfo(elementType,indexName,indexType,indexFileName);
    }

    public boolean isNodeIndex() {
        return elementType.equals("node_index");
    }

    public String getConfigKey() {
        return "batch_import.legacy."+elementType+"."+indexName;
    }

    public String getConfigValue() {
        if (indexFileName==null) return indexType;
        return indexType+":"+indexFileName;
    }

    public Map<String, String> addToConfig(Map<String, String> config) {
        config.put(getConfigKey(), getConfigValue());
        return config;
    }

    public boolean shouldImportFile() {
        if (indexFileName == null) return false;
        final File file = new File(indexFileName);
        return file.exists() && file.isFile() && file.canRead();
    }
}
