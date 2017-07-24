/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.p4.process;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.concurrent.atomic.AtomicReference;

import java.io.File;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IUpdateableMetadataCatalog.Updater;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.data.process.ui.FieldViewerSite;
import org.polymap.core.data.process.ui.OutputFieldConsumer;
import org.polymap.core.data.raster.catalog.GridServiceResolver;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.SubMonitor;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.style.DefaultStyle;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.Scope;

import org.polymap.model2.query.Expressions;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.AllResolver;
import org.polymap.p4.layer.NewLayerOperation;
import org.polymap.p4.project.ProjectRepository;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class LayerRasterConsumer
        extends OutputFieldConsumer {

    private static final Log log = LogFactory.getLog( LayerRasterConsumer.class );
    
    private Button          btn;

    private Text            text;


    @Override
    public String label() {
        return "Layer";
    }

    
    @Override
    public boolean init( @SuppressWarnings( "hiding" ) FieldViewerSite site ) {
        return super.init( site ) && site.fieldInfo.get().type().isAssignableFrom( GridCoverage2D.class );
    }

    
    @Override
    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        btn = new Button( parent, SWT.PUSH );
        btn.setText( "CREATE..." );
        btn.setToolTipText( "Create a new layer with the given name" );
        btn.addSelectionListener( UIUtils.selectionListener( ev -> {
            consume();
            btn.setEnabled( false );
            btn.setToolTipText( "Layer has been created" );
        }));
        
        text = new Text( parent, SWT.BORDER );
        text.setToolTipText( "The name of the layer to create" );
        text.addModifyListener( ev -> {
            try {
                boolean layerExists = ProjectRepository.unitOfWork()
                        .query( ILayer.class )
                        .where( Expressions.eq( ILayer.TYPE.label, text.getText() ) )
                        .execute().size() > 0;
                if (!isBlank( text.getText() ) && !layerExists ) {
                    btn.setEnabled( true );
                    btn.setToolTipText( "Create a new layer with the given name" );
                }
                else {
                    btn.setEnabled( false );
                    btn.setToolTipText( "Layer name already exists" );
                }
            }
            catch (Exception e) {
                log.warn( "", e );
            } 
        });
        ILayer layer = (ILayer)site.layer.get(); 
        text.setText( layer.label.get() + "-" + site.moduleInfo.get().label() ); // simpleClassname.get() );
        text.forceFocus();
        
        FormDataFactory.on( text ).fill().right( 100, -80 );
        FormDataFactory.on( btn ).fill().left( text );
    }
    
    
    public void consume() {
        String layerName = text.getText();
        UIJob.schedule( "Create layer", monitor -> {
            execute( layerName, monitor );
        });
    }
    
    
    protected void execute( String layerName, IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( "Create layer", IProgressMonitor.UNKNOWN );
        GridCoverage2D resultRaster = site.getFieldValue();
        log.info( "Result: " + resultRaster.toString() );
        
        SubMonitor submon = new SubMonitor( monitor, 1, "Writing raster", IProgressMonitor.UNKNOWN );
        File targetDir = new File( P4Plugin.gridStoreDir(), layerName );
        File rasterFile = new File( targetDir, layerName + ".tiff" );
        targetDir.mkdirs();
        log.info( "Raster file: " + rasterFile.getAbsolutePath() );
        
        writeGeotiff( resultRaster, new File( rasterFile.getAbsolutePath() ) );  
        submon.done();
        
        submon = new SubMonitor( monitor, 1 );
        IServiceInfo service = createCatalogEntry( rasterFile, submon );
        submon.done();

        submon = new SubMonitor( monitor, 1 );
        createLayer( layerName, service, submon );
        submon.done();
    }
    
    
    protected void writeGeotiff( GridCoverage2D raster, File toFile ) throws Exception {
        final GeoTiffFormat format = new GeoTiffFormat();
        final GeoTiffWriteParams wp = new GeoTiffWriteParams();
        wp.setCompressionMode( GeoTiffWriteParams.MODE_DEFAULT );
        wp.setTilingMode( GeoToolsWriteParams.MODE_DEFAULT );
        final ParameterValueGroup paramWrite = format.getWriteParameters();
        paramWrite.parameter( AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString() ).setValue( wp );
        GeoTiffWriter gtw = (GeoTiffWriter)format.getWriter( toFile );
        gtw.write( raster, (GeneralParameterValue[])paramWrite.values().toArray( new GeneralParameterValue[1] ) );
    }

    
    protected IServiceInfo createCatalogEntry( File rasterFile, IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( "new catalog entry", 1 );
        try (
            Updater update = P4Plugin.localCatalog().prepareUpdate()
        ){
            AtomicReference<IServiceInfo> service = new AtomicReference();
            update.newEntry( metadata -> {
                metadata.setTitle( FilenameUtils.getBaseName( rasterFile.getName() ) );
                
//                metadata.setDescription( gridReader.get().getFormat().getName() );
//                if (service.getKeywords() != null) {
//                    metadata.setKeywords( service.getKeywords() );
//                }
                
                metadata.setType( "Grid" );
//                metadata.setFormats( Sets.newHashSet( gridReader.get().getFormat().getName() ) );

                String url = rasterFile.toURI().toString();
                metadata.setConnectionParams( GridServiceResolver.createParams( url ) );
                
                try {
                    service.set( (IServiceInfo)AllResolver.instance().resolve( metadata, monitor ) );
                    log.info( "Resolved: " + service.get()) ;
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            });
            update.commit();
            return service.get();
        }
    }

    
    protected void createLayer( String layerName, IServiceInfo service, IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( "new layer", 2 );
        GridCoverage2DReader reader = service.createService( monitor );
        IResourceInfo res = Iterables.getOnlyElement( service.getResources( monitor ) );
        GridCoverage2D grid = reader.read( res.getName(), null );
        
        FeatureStyle featureStyle = P4Plugin.styleRepo().newFeatureStyle();
        DefaultStyle.create( featureStyle, grid );
        monitor.worked( 1 );

        BatikApplication.instance().getContext().propagate( this );
        NewLayerOperation op = new NewLayerOperation()
                .label.put( layerName )
                .res.put( res )
                .featureStyle.put( featureStyle )
                .uow.put( ProjectRepository.unitOfWork() )
                .map.put( map.get() );

        OperationSupport.instance().execute( op, false, false );
        monitor.worked( 1 );
    }
    
    @Mandatory
    @Scope(P4Plugin.Scope)
    protected Context<IMap>         map;

}
