/* 
 * polymap.org
 * Copyright (C) 2015-2018, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.catalog;

import static org.polymap.core.CorePlugin.getDataLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.local.LocalMetadataCatalog;
import org.polymap.core.catalog.resolve.IResolvableInfo;
import org.polymap.core.data.rs.RDataStore;
import org.polymap.core.data.rs.catalog.RServiceResolver;
import org.polymap.core.data.rs.lucene.LuceneQueryDialect;
import org.polymap.core.data.wms.catalog.WmsServiceResolver;

import org.polymap.rhei.fulltext.store.lucene.LuceneFulltextIndex;

import org.polymap.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.p4.P4Plugin;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LocalCatalog
        extends LocalMetadataCatalog {

    public static final String      LOCAL_FEATURES_STORE_ID = "_local_features_store_";

    public static final String      WORLD_BACKGROUND_ID = "_world_background_";
    public static final String      WORLD_BORDER_RES = "TM_WORLD_BORDERS-0.3";
    
    private LuceneRecordStore       store;

    private LuceneFulltextIndex     index;
    
    
    public LocalCatalog() throws Exception {
        File dataDir = getDataLocation( P4Plugin.instance() );
        store = new LuceneRecordStore( new File( dataDir, "localCatalog" ), false );
        index = new LuceneFulltextIndex( new File( dataDir, "localCatalogIndex" ) );
        
        init( new RecordStoreAdapter( store ), index );
        
        checkInitContents();
    }

    
    @Override
    public String getTitle() {
        return "Project's data sources";
    }


    @Override
    public String getDescription() {
        return "Data sources of this project";
    }


    /**
     * Returns the one and only 'local' store for features in this P4 instance.
     */
    public IMetadata localFeaturesStoreEntry() {
        try {
            return entry( LOCAL_FEATURES_STORE_ID, new NullProgressMonitor() ).get();
        }
        catch (Exception e) {
            throw Throwables.propagate( e );
        }
    }
    
    
    /**
     * Returns the one and only 'local' store for features in this P4 instance.
     */
    public IResolvableInfo localFeaturesStoreInfo() {
        try {
            IMetadata metadata = localFeaturesStoreEntry();
            return P4Plugin.allResolver().resolve( metadata ).get();
        }
        catch (Exception e) {
            throw Throwables.propagate( e );
        }
    }
    
    
    /**
     * Returns the one and only 'local' store for features in this P4 instance.
     */
    public DataAccess localFeaturesStore() {
        try {
            IResolvableInfo info = localFeaturesStoreInfo();
            return info.getServiceInfo().createService( new NullProgressMonitor() );
        }
        catch (Exception e) {
            throw Throwables.propagate( e );
        }
    }
    

    /**
     * Creates standard entries.
     */
    protected void checkInitContents() throws Exception {
        // check empty
        if (query( ALL_QUERY, new NullProgressMonitor() ).execute().size() == 0) {
            // create standard entries
            try (Updater update = prepareUpdate()) {
                P4Plugin.featureStoreDir().mkdirs();
                update.newEntry( metadata -> {
                    metadata.setIdentifier( LOCAL_FEATURES_STORE_ID );
                    metadata.setTitle( "Datastore" );
                    metadata.setDescription( "The data store of this project" );
                    metadata.setType( "Database" );
                    metadata.setConnectionParams( RServiceResolver.createParams( P4Plugin.featureStoreDir() ) );
                });
                update.newEntry( metadata -> {
                    metadata.setIdentifier( WORLD_BACKGROUND_ID );
                    metadata.setTitle( "Example WMS" );
                    metadata.setDescription( "Default background and demo layers from mapzone.io." );
                    metadata.setType( "Service" );
                    metadata.setFormats( Sets.newHashSet( "WMS", "WFS" ) );
                    metadata.setConnectionParams( WmsServiceResolver.createParams( "http://mapzone.io/projects/falko/World/ows/n92ln3he1b536m3ir8rds98ohn/ows?SERVICE=WMS" ) );
                });
//                update.newEntry( metadata -> {
//                    metadata.setTitle( "Schutzgebiete Mittelsachsen" );
//                    metadata.setDescription( "-test entry-" );
//                    metadata.setConnectionParams( WmsServiceResolver.createParams( "http://www.mittelsachsen-atlas.de/polymap-atlas/services/INSPIRE/Schutzgebiete" ) );
//                });
                update.commit();
            }
            
            // copy resources/TM_WORLD_BORDERS-0.3.zip
            URL url = P4Plugin.instance().getBundle().getEntry( "resources/TM_WORLD_BORDERS-0.3.zip" );
            File temp = Files.createTempDirectory( "p4-catalog-init-" ).toFile();
            File shp = null;
            try (
                ZipInputStream zip = new ZipInputStream( url.openStream() );
            ){
                for (ZipEntry entry=zip.getNextEntry(); entry!=null; entry=zip.getNextEntry()) {
                    File f = new File( temp, FilenameUtils.getName( entry.getName() ) );
                    try (FileOutputStream out = new FileOutputStream( f )) {
                        IOUtils.copy( zip, out );
                    }
                    if (f.getName().endsWith( ".shp" )) {
                        shp = f;
                    }
                }
            }
            
            Map<String,Serializable> params = new HashMap<String,Serializable>();
            params.put( ShapefileDataStoreFactory.URLP.key, shp.toURI().toURL() );
            params.put( ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.FALSE );

            ShapefileDataStoreFactory dsf = new ShapefileDataStoreFactory();
            ShapefileDataStore shapeDs = (ShapefileDataStore)dsf.createNewDataStore( params );
            ContentFeatureSource shapeFs = shapeDs.getFeatureSource();
            SimpleFeatureType schema = shapeFs.getSchema();

            LuceneRecordStore rs = new LuceneRecordStore( P4Plugin.featureStoreDir(), false );
            RDataStore ds = new RDataStore( rs, new LuceneQueryDialect() );            
            ds.createSchema( schema );
            FeatureStore fs = (FeatureStore)ds.getFeatureSource( schema.getName() );
            fs.addFeatures( shapeFs.getFeatures() );
            
            ds.dispose();            
            FileUtils.deleteDirectory( temp );
        }
    }


    /**
     * 
     */
    public void deleteEntry( String identifier ) throws Exception {
        try (Updater update = prepareUpdate()) {
            update.removeEntry( identifier );
            update.commit();
        }
    }
    
}
