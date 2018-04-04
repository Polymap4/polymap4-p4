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
package org.polymap.p4.data.importer.wfs;

import static org.polymap.core.ui.FormDataFactory.on;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.WFSServiceInfo;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IUpdateableMetadataCatalog.Updater;
import org.polymap.core.data.wfs.catalog.WfsServiceResolver;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.SimpleDialog;
import org.polymap.rhei.table.FeatureCollectionContentProvider;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.importer.Importer;
import org.polymap.p4.data.importer.ImporterPlugin;
import org.polymap.p4.data.importer.ImporterPrompt;
import org.polymap.p4.data.importer.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.importer.ImporterPrompt.Severity;
import org.polymap.p4.data.importer.ImporterSite;
import org.polymap.p4.data.importer.Messages;
import org.polymap.p4.data.importer.shapefile.ShpFeatureTableViewer;
import org.polymap.p4.data.importer.wms.OwsMetadata;
import org.polymap.p4.data.importer.wms.WmsImporter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WfsImporter
        implements Importer {

    private static final Log log = LogFactory.getLog( WfsImporter.class );

    private static final IMessages i18n = Messages.forPrefix( "WFS" );

    public static final Pattern    urlPattern = WmsImporter.urlPattern;

    private static final WFSDataStoreFactory dsf  = new WFSDataStoreFactory();

    private ImporterSite            site;

    private Exception               exception;

    private ImporterPrompt          urlPrompt;

    private String                  url;

    private WFSDataStore            ds;

    //private SimpleFeatureSource    fs;

    private IPanelToolkit           toolkit;

    private Composite               resultArea;


    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    public void init( @SuppressWarnings( "hiding" ) ImporterSite site, IProgressMonitor monitor ) {
        this.site = site;
        site.icon.set( ImporterPlugin.images().svgImage( "earth.svg", SvgImageRegistryHelper.NORMAL24 ) );
        site.summary.set( i18n.get( "summary" ) );
        site.description.set( i18n.get( "description" ) );
        site.terminal.set( true );
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        // start without alert icon and un-expanded
        urlPrompt = site.newPrompt( "url" )
                .summary.put( "URL" )
                .value.put( "<Click to specify>" )
                .description.put( "The URL of the remote server" )
                .severity.put( Severity.VERIFY )
                .extendedUI.put( new PromptUIBuilder() {
                    @Override
                    public void createContents( ImporterPrompt prompt, Composite parent, IPanelToolkit tk ) {
                        parent.setLayout( FormLayoutFactory.defaults().spacing( 3 ).create() );
                        Text text = on( tk.createText( parent, url != null ? url : "", SWT.BORDER, SWT.WRAP, SWT.MULTI ) )
                                .top( 0 ).left( 0 ).right( 100 ).width( DEFAULT_WIDTH ).height( 40 ).control();
                        text.forceFocus();

                        final Label msg = on( tk.createLabel( parent, "" ) ).fill().top( text ).control();

                        text.addModifyListener( new ModifyListener() {
                            @Override
                            public void modifyText( ModifyEvent ev ) {
                                url = text.getText();
                                msg.setText( urlPattern.matcher( url ).matches() ? "Ok" : "Not a valid URL" );
                            }
                        } );
                    }

                    @Override
                    public void submit( ImporterPrompt prompt ) {
                        urlPrompt.severity.set( Severity.REQUIRED );
                        urlPrompt.ok.set( urlPattern.matcher( url ).matches() );
                        urlPrompt.value.set( url );
                    }
                });
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
        try {
            if (ds != null) {
                ds.dispose();
            }
            if (url != null) {
                Map<String,Serializable> params = new HashMap();
                URL getCapabilities = WFSDataStoreFactory.createGetCapabilitiesRequest( new URL( url ) );
                log.info( "URL: " + getCapabilities );
                params.put( WFSDataStoreFactory.URL.key, getCapabilities );
                params.put( WFSDataStoreFactory.TIMEOUT.key, 10000 );

                ds = (WFSDataStore)dsf.createDataStore( params );

                if (ds.getNames().size() > 0) {
                    Name first = ds.getNames().get( 0 );
                    SimpleFeatureSource fs = ds.getFeatureSource( first );
                    
                    // check access, fail fast in verify()
                    SimpleFeatureType schema = (SimpleFeatureType)fs.getSchema();
                    log.info( "schema: " + schema );
                    Query query = new Query();
                    query.setMaxFeatures( 100 );
                    try (
                        SimpleFeatureIterator features = fs.getFeatures( query ).features();
                    ){
                        while (features.hasNext()) {
                            features.next();
                        }
                    }
                }

                site.ok.set( true );
                exception = null;
            }
        }
        catch (Exception e) {
            log.warn( "", e );
            site.ok.set( false );
            exception = e;
        }
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit tk ) {
        this.toolkit = tk;
        try {
            if (ds == null) {
                tk.createFlowText( parent, "Please specify the URL of the service." );
            }
            else if (exception != null) {
                tk.createFlowText( parent, "\nUnable to read the data.\n\n**Reason**: " + exception.getMessage() );
            }
            else if (ds.getNames().isEmpty()) {
                tk.createFlowText( parent, "\nNo data types found." );
            }
            else {
                //tabs = tk.createTabViewer( parent );
                parent.setLayout( FormLayoutFactory.defaults().spacing( 3 ).create() );
                Combo combo = tk.adapt( new Combo( parent, SWT.READ_ONLY|SWT.FLAT ), false, false );
                combo.setFont( UIUtils.bold( combo.getFont() ) );
                combo.forceFocus();
                FormDataFactory.on( combo ).fill().noBottom();

                combo.setVisibleItemCount( 8 );
                combo.add( "Overview" );
                ds.getNames().forEach( name -> combo.add( name.getLocalPart() ) );
                
                resultArea = tk.createComposite( parent );
                FormDataFactory.on( resultArea ).fill().top( combo );
                
                SelectionAdapter handler = new SelectionAdapter() {
                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        UIUtils.disposeChildren( resultArea );                        
                        if (combo.getText().equals( "Overview" )) {
                            createOverview( resultArea, tk );
                        }
                        else {
                            createFeatureTable( resultArea, tk, combo.getText() );
                        }
                        resultArea.layout( true );
                        parent.layout( true );
                    }
                };
                combo.addSelectionListener( handler );
                combo.select( 0 );
                handler.widgetSelected( null );
            }
        }
        catch (Exception e) {
            log.warn( "", e );
            tk.createFlowText( parent, "\nUnable to read the data.\n\n**Reason**: " + e.getMessage() );
            site.ok.set( false );
            exception = e;
        }
    }

    
    protected void createOverview( Composite parent, IPanelToolkit tk ) {
        ScrolledComposite scrolledComposite = new ScrolledComposite( parent, SWT.V_SCROLL | SWT.H_SCROLL );
        scrolledComposite.setExpandHorizontal( true );
        scrolledComposite.setExpandVertical( true );

        Composite content = tk.createComposite( scrolledComposite );
        scrolledComposite.setContent( content );
        //int preferredWidth = preferredWidth( parent );
        
        try {
            // the great gt-wfs-ng does not provide info about the service
            //WFSGetCapabilities capabilities = ds.getWfsClient().getCapabilities();
            OwsMetadata md = new OwsMetadata()
                    .markdown( ds.getInfo() )
                    .markdownLayers( ds.getNames() );
                    //.markdown( "\n\nClick on \"Overview\" to preview data." );
            tk.createFlowText( content, md.toString() );
        }
        // XXX wfs-ng seems to be buggy with WFS2.0
        catch (ClassCastException e) {
            UIUtils.disposeChildren( parent );
            log.warn( "", e );
            tk.createFlowText( parent, "\nUnable to read description from server." );
        }
        catch (Throwable e) {
            UIUtils.disposeChildren( parent );
            log.warn( "", e );
            tk.createFlowText( parent, "\nUnable to read description from server.\n\n**Reason**: " + e.getMessage() );
            // don't break entire import
//            site.ok.set( false );
//            exception = e;
        }
    }
    
    
    protected ShpFeatureTableViewer createFeatureTable( Composite parent, IPanelToolkit tk, String typeName ) {
        try {
            ContentFeatureSource fs = ds.getFeatureSource( typeName );
            SimpleFeatureType schema = (SimpleFeatureType)fs.getSchema();

            ShpFeatureTableViewer table = new ShpFeatureTableViewer( parent, schema );
            table.setContentProvider( new FeatureCollectionContentProvider() );

            Query query = new Query();
            query.setMaxFeatures( 25 );
            SimpleFeatureCollection content = fs.getFeatures( query );
            table.setInput( content );
            return table;
        }
        catch (IOException e) {
            log.warn( "", e );
            tk.createFlowText( parent, "\nUnable to read the data.\n\n**Reason**: " + e.getMessage() );
            site.ok.set( false );
            exception = e;
            return null;
        }
    }

    
    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        // create catalog entry
        try (
            Updater update = P4Plugin.localCatalog().prepareUpdate()
        ) {
            WFSServiceInfo serviceInfo = ds.getInfo();
            update.newEntry( metadata -> {
                try {
                    metadata.setType( "Service" );
                    metadata.setFormats( Sets.newHashSet( "WFS" ) );
                    // XXX wfs-ng seems to be buggy with WFS2.0
                    metadata.setTitle( serviceInfo.getTitle().contains( "LanguageStringType" )
                            ? url : serviceInfo.getTitle() );
                    metadata.setDescription( normalize( serviceInfo.getDescription() ) );
                }
                catch (Exception e) {
                    // geotools wfs-ng is buggy, for WFS 2.0 it throws ClassCastException in getDescription()
                    log.warn( "Exception while creating catalog entry", e );
                }

                if (serviceInfo.getKeywords() != null) {
                    metadata.setKeywords( Sets.newHashSet( serviceInfo.getKeywords() ) );
                }

//                // WMS provides some more but GeoTools does not deliver more info
//                metadata.setDescription( IMetadata.Field.Publisher, 
//                        normalize( new OwsMetadata().markdown( service.getContactInformation() ).toString() ) );

                metadata.setConnectionParams( WfsServiceResolver.createParams( url ) );
            } );
            update.commit();
        }

        //
        UIThreadExecutor.async( () -> {
            SimpleDialog dialog = new SimpleDialog();
            dialog.title.put( "Information" );
            dialog.setContents( parent -> {
                toolkit.createFlowText( parent, i18n.get( "infoAdded" ) );
            });
            dialog.addOkAction( () -> {
                dialog.close();
                return true;
            });
            dialog.open();
        });
    }


    protected String normalize( String s ) {
        return StringUtils.defaultIfBlank( s, null );
    }

    protected int preferredWidth( Composite parent ) {
        return Math.min( parent.getDisplay().getClientArea().width, 400 ) - 50;
    }
    
}
