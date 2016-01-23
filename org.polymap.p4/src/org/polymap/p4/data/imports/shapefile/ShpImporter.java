/*
 * polymap.org Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.data.imports.shapefile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ContextOut;
import org.polymap.p4.data.imports.Importer;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShpImporter
        implements Importer {

    private static Log log = LogFactory.getLog( ShpImporter.class );
    
    private static final ShapefileDataStoreFactory dsFactory = new ShapefileDataStoreFactory();
    
    private ImporterSite            site;

    @ContextIn
    protected List<File>            files;

    @ContextIn
    protected File                  shp;

    @ContextOut
    protected FeatureCollection     features;

    protected Exception             exception;

    private ShapefileDataStore      ds;

    private CharsetPrompt           charsetPrompt;
    
    private CrsPrompt               crsPrompt;


    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    public void init( @SuppressWarnings("hiding") ImporterSite site, IProgressMonitor monitor ) {
        this.site = site;
        site.icon.set( P4Plugin.images().svgImage( "shp.svg", SvgImageRegistryHelper.NORMAL24 ) );
        site.summary.set( "Shapefile: " + shp.getName() );
        site.description.set( "A Shapefile is a common file format which contains features of the same type." );
        site.terminal.set( true );
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        charsetPrompt = createCharsetPrompt();
        crsPrompt = createCrsPrompt();
    }

    
    protected CharsetPrompt createCharsetPrompt() {
        return new CharsetPrompt( site(), files );
    }

    
    protected CrsPrompt createCrsPrompt() {
        return new CrsPrompt( site(), files );
    }

    
    @Override
    public void verify( IProgressMonitor monitor ) {
        try {
            if (ds != null) {
                ds.dispose();
            }
            Map<String,Serializable> params = new HashMap<String,Serializable>();
            params.put( "url", shp.toURI().toURL() );
            params.put( "create spatial index", Boolean.TRUE );

            ds = (ShapefileDataStore)dsFactory.createNewDataStore( params );
            ds.setCharset( charsetPrompt.selection() );

            Query query = new Query();
            query.setMaxFeatures( 100 );
            features = ds.getFeatureSource().getFeatures( query );

            ContentFeatureCollection contentFeatures = ds.getFeatureSource().getFeatures( query );
            SimpleFeatureIterator iter = contentFeatures.features();
            List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
            SimpleFeature feature = null;
            while (iter.hasNext()) {
                feature = iter.next();
                featureList.add( feature );
            }

            SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype( ds.getSchema(), crsPrompt.selection() );
            ds.dispose();

            ds = (ShapefileDataStore)dsFactory.createNewDataStore( params );
            ds.createSchema( featureType );
            ds.setCharset( charsetPrompt.selection() );

            boolean success = retypeFeatures( ds, featureType, featureList );
            if (success) {
                features = ds.getFeatureSource().getFeatures( query );
            }

            site().ok.set( true );
            exception = null;
        }
        catch (Exception e) {
            site().ok.set( false );
            exception = e;
        }
    }


    private boolean retypeFeatures( ShapefileDataStore shpDataStore,
            SimpleFeatureType newSchema, List<SimpleFeature> featureList ) {
        try {
            Transaction transaction = new DefaultTransaction( "create" );
            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource( newSchema.getTypeName() );
            if (featureSource instanceof SimpleFeatureStore) {
                SimpleFeatureStore featureStore = (SimpleFeatureStore)featureSource;
                List<SimpleFeature> retypedFeatures = new ArrayList<SimpleFeature>();
                SimpleFeature retypedFeature = null;
                for (SimpleFeature feature : featureList) {
                    retypedFeature = SimpleFeatureBuilder
                            .build( newSchema, feature.getAttributes(), "" );
                    retypedFeatures.add( retypedFeature );
                }
                SimpleFeatureCollection collection = new ListFeatureCollection(
                        newSchema, retypedFeatures );
                featureStore.setTransaction( transaction );
                try {
                    featureStore
                            .addFeatures( collection );
                    transaction.commit();
                }
                catch (Exception e) {
                    exception = e;
                    transaction.rollback();
                }
                finally {
                    transaction.close();
                }
                return true;
            }
            else {
                exception = new Exception( "Couldn't write shape files." );
                return false;
            }
        }
        catch (IOException e) {
            exception = e;
        }
        return false;
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit tk ) {
        if (exception != null) {
            tk.createFlowText( parent, "\nUnable to read the data.\n\n**Reason**: " + exception.getMessage() );
        }
        else {
            SimpleFeatureType schema = (SimpleFeatureType)features.getSchema();
            //log.info( "Features: " + features.size() + " : " + schema.getTypeName() );
            // tk.createFlowText( parent, "Features: *" + features.size() + "*" );
            ShpFeatureTableViewer table = new ShpFeatureTableViewer( parent, schema );
            table.setContent( features );
        }
    }

    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        // no maxResults restriction
        features = ds.getFeatureSource().getFeatures();
    }

}
