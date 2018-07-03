/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.geotools.styling.Style;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.feature.DefaultStyles;
import org.polymap.core.data.feature.FeatureRenderProcessor2;
import org.polymap.core.data.image.EncodedImageProducer;
import org.polymap.core.data.pipeline.DataSourceDescriptor;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.mapeditor.ILayerProvider;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.mapeditor.services.SimpleWmsServer;
import org.polymap.core.project.ILayer;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.AllResolver;
import org.polymap.p4.data.P4PipelineBuilder;
import org.polymap.rap.openlayers.layer.ImageLayer;
import org.polymap.rap.openlayers.layer.Layer;
import org.polymap.rap.openlayers.layer.TileLayer;
import org.polymap.rap.openlayers.source.ImageWMSSource;
import org.polymap.rap.openlayers.source.TileWMSSource;
import org.polymap.rap.openlayers.source.WMSRequestParams;

/**
 * Builds OpenLayers {@link Layer} objects for the {@link MapViewer} of the
 * {@link ProjectMapPanel} out of {@link ILayer} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectLayerProvider
        implements ILayerProvider<ILayer>, AutoCloseable {

    private static final Log log = LogFactory.getLog( ProjectLayerProvider.class );

    protected String                    alias;
    
    private SimpleWmsServer             wms;
    
    protected Map<String,ILayer>        layers = new ConcurrentHashMap();

    
    public ProjectLayerProvider() {
        try {
            // register WMS servlet
            wms = new SimpleWmsServer() {
                @Override
                protected String[] layerNames() {
                    throw new RuntimeException( "not yet implemented." );
                }
                @Override
                protected Pipeline createPipeline( String layerName ) {
                    return ProjectLayerProvider.this.createPipeline( layerName );
                }
            };
            alias = "/mapviewer" + hashCode();
            P4Plugin.instance().httpService().registerServlet( alias, wms , null, null );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    protected Pipeline createPipeline( String layerName ) {
        try {
            ILayer layer = layers.get( layerName );
            
            // resolve service
            IProgressMonitor monitor = new NullProgressMonitor();  //.monitorOfThread();
            DataSourceDescriptor dsd = AllResolver.instance().connectLayer( layer, monitor )
                    .orElseThrow( () -> new RuntimeException( "No data source for layer: " + layer ) );

            // feature style
            Supplier<Style> styleSupplier = () -> {
                String styleId = layer.styleIdentifier.get();
                return styleId != null
                    ? P4Plugin.styleRepo().serializedFeatureStyle( styleId, Style.class )
                            .orElse( DefaultStyles.createAllStyle() )
                    : DefaultStyles.createAllStyle();
            };

            // create pipeline for it
            P4PipelineBuilder builder = P4PipelineBuilder.forLayer( layer );
            FeatureRenderProcessor2.STYLE_SUPPLIER.rawput( builder, styleSupplier );
            return builder.createPipeline( EncodedImageProducer.class, dsd )
                    .orElseThrow( () -> new RuntimeException( "Unable to build pipeline for: " + dsd ) );
        }
        catch (Exception e) {
            log.warn( "", e );
            throw Throwables.propagate( e );
        }
    }


    @Override
    public Layer getLayer( ILayer elm ) {
        String layerName = elm.label.get();
        String styleHash = "defaultStyle";
        try {
            styleHash = elm.styleIdentifier.opt().map( styleId ->
                    "#" + P4Plugin.styleRepo().serializedFeatureStyle( styleId, String.class ).get().hashCode() )
                    .orElse( "defaultStyle" );
        }
        catch (Throwable e) {
            log.error( "", e );
        }
                
        layers.put( layerName, elm );
        wms.disposePipeline( layerName );
        return buildTiledLayer( layerName, styleHash );
    }
    

    /**
     * Builds a single-tile layer
     */
    protected Layer buildLayer( String layerName, String styleHash ) {
        return new ImageLayer()
                 .source.put( new ImageWMSSource()
                         .url.put( "." + alias )
                         .params.put( new WMSRequestParams()
                                 .version.put( "1.1.1" )  // send "SRS" param
                                 .layers.put( layerName )
                                 .styles.put( styleHash )
                                 .format.put( "image/png" ) ) );
    }
    
    
    protected Layer buildTiledLayer( String layerName, String styleHash ) {
        return new TileLayer()
                .source.put( new TileWMSSource()
                        //.tileGrid.put( new TileGrid( "ol.tilegrid.TileGrid" ) {}.tileSize.put( new Size( 1024, 1024 ) ) )
                        .url.put( "." + alias )
                        .params.put( new WMSRequestParams()
                                .version.put( "1.1.1" )  // send "SRS" param
                                .layers.put( layerName )
                                .styles.put( styleHash )
                                .format.put( "image/png" ) ) );
    }
    
    
    @Override
    public int getPriority( ILayer elm ) {
        return elm.orderKey.get();
    }


    @Override
    public void close() {
        if (alias != null) {
            P4Plugin.instance().httpService().unregister( alias );
            alias = null;
        }
    }


    @Override
    protected void finalize() throws Throwable {
        close();
    }

}
