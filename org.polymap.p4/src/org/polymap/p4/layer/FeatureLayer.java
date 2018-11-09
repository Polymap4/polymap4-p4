/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.layer;

import static org.polymap.core.runtime.event.TypeEventFilter.ifType;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.opengis.feature.Feature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.PipelineDataStore;
import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.feature.FeaturesProducer;
import org.polymap.core.data.pipeline.DataSourceDescriptor;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineBuilderException;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ProjectNode.ProjectNodeCommittedEvent;
import org.polymap.core.runtime.JobExecutor;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.session.SessionSingleton;

import org.polymap.p4.P4Panel;
import org.polymap.p4.catalog.AllResolver;
import org.polymap.p4.data.P4PipelineBuilder;

/**
 * Carries the {@link FeatureSource}, the {@link #filter()}ed features and the
 * {@link #clicked()} feature for layers which are connected to a {@link DataStore}.
 * There is one instance per layer per session. This instance is retrieved via
 * {@link #of(ILayer)}.
 * <p/>
 * The currently <b>active</b> layer and its selection is stored in
 * {@link P4Panel#featureSelection()}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureLayer {
    
    private static final Log log = LogFactory.getLog( FeatureLayer.class );

    public static final FilterFactory2  ff = DataPlugin.ff;

    /**
     * The selection modes.
     */
    public enum Mode { 
        REPLACE, ADD, DIFFERENCE, INTERSECT 
    };

    
    /**
     * Waits for the {@link FeatureLayer} of the given {@link ILayer}.
     * <p/>
     * Avoid calling just {@link CompletableFuture#get()} as this may block the
     * calling (UI) thread. Instead register callbacks that handle the result
     * asynchronously.
     * <p/>
     * The callbacks are called from within an {@link UIJob}. Use
     * {@link UIThreadExecutor} to update the UI.
     * <p/>
     * <b>Example usage:</b>
     * <pre>
     *      FeatureLayer.of( layer ).thenAccept( featureLayer -> {
     *          if (featureLayer.isPresent()) {
     *              ...
     *          }
     *          else {
     *              ...
     *          }
     *      })
     *      .exceptionally( e -> {
     *          StatusDispatcher.handleError( "", e );
     *          return null;
     *      });
     * </pre>
     * 
     * @param layer
     */
    public static CompletableFuture<Optional<FeatureLayer>> of( ILayer layer ) {
        return CompletableFuture.supplyAsync( () -> {
            SessionHolder session = SessionHolder.instance( SessionHolder.class );
            FeatureLayer result = session.instances.computeIfAbsent( (String)layer.id(), key -> { 
                try {
                    IProgressMonitor monitor = UIJob.monitorOfThread();
                    return new FeatureLayer( layer ).doConnectLayer( monitor );
                }
                catch (Exception e) {
                    throw new CompletionException( e );
                }
            });
            return result.isValid() ? Optional.of( result ) : Optional.empty();
        }, JobExecutor.withProgress() );
    }
    

    private static class SessionHolder
            extends SessionSingleton {
    
        /**
         * Using {@link ConcurrentHashMap} instead of {@link Cache} ensures that
         * mapping function is executed at most once per key and just one
         * {@link FeatureLayer} instance is constructed per {@link ILayer}.
         */
        ConcurrentMap<String,FeatureLayer>    instances = new ConcurrentHashMap( 32 );
    }
    
    
    // instance *******************************************
    
    private ILayer                      layer;
    
    private PipelineFeatureSource       fs;
    
    private Filter                      filter = Filter.INCLUDE;
    
    private Optional<Feature>           clicked = Optional.empty();
    
    
    protected FeatureLayer( ILayer layer ) {
        this.layer = layer;
        
        EventManager.instance().subscribe( this, ifType( ProjectNodeCommittedEvent.class, ev -> 
                    ev.isEntity( layer ) ) );
    }

    
    /**
     * Listen to changes of the {@link #layer}. Builds a new pipeline to reflect
     * possible changes of the processors and/or their params.
     */
    @EventHandler( delay=100 )
    protected void handleLayerCommit( List<ProjectNodeCommittedEvent> evs ) {
        if (fs != null) {
            new UIJob( "Update pipeline" ) {
                @Override
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    log.debug( "handleLayerCommit(): " + layer.label.get() );
                    DataSourceDescriptor dsd = fs.pipeline().dataSourceDescription();
                    Pipeline newPipeline = P4PipelineBuilder.forLayer( layer ).createPipeline( FeaturesProducer.class, dsd )
                            .orElseThrow( () -> new PipelineBuilderException( "Unable to build pipeline for: " + layer  ) );
                    fs.setPipeline( newPipeline );
                }
            }.schedule();
        }
    }
    
    
    protected FeatureLayer doConnectLayer( IProgressMonitor monitor ) throws PipelineBuilderException, Exception {
        assert fs == null;
        log.debug( "doConnectLayer(): " + layer.label.get() );
        // resolve service
        DataSourceDescriptor dsd = AllResolver.instance().connectLayer( layer, monitor )
                .orElseThrow( () -> new RuntimeException( "No data source for layer: " + layer ) );

        // create pipeline for it
        P4PipelineBuilder.forLayer( layer )
                .createPipeline( FeaturesProducer.class, dsd )
                .ifPresent( pipeline -> {
                    try { fs = new PipelineDataStore( pipeline ).getFeatureSource(); }
                    catch (IOException e) { throw new RuntimeException( e ); }
                });

        return this;
    }


    @Override
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        else if (obj instanceof FeatureLayer) {
            FeatureLayer rhs = (FeatureLayer)obj;
            return layer.id().equals( rhs.layer.id() );
        }
        else {
            return false;
        }
    }


    public boolean isValid() {
        return fs != null;
    }


    public PipelineFeatureSource featureSource() {
        assert isValid();
        return fs;
    }


    public ILayer layer() {
        assert isValid();
        return layer;
    }


    public Filter filter() {
        assert isValid();
        return filter;
    }


    public void select( Filter selection, Mode mode ) {
        assert isValid();
        Filter old = filter;
        switch (mode) {
            case REPLACE: 
                filter = selection; break;
            case ADD: 
                filter = ff.or( filter, selection ); break;
            default: 
                throw new RuntimeException( "Unhandled mode: " + mode );
        }
        // event
        EventManager.instance().publish( new FeatureSelectionEvent( this, filter, old ) );
    }
    
    
    /**
     * The one feature that was 'clicked' somewhere in the UI. Usually a feature can
     * be clicked in the map and a feature table.
     */
    public Optional<Feature> clicked() {
        assert isValid();
        return clicked;
    }
    
    
    /**
     * 
     *
     * @param clicked The newly {@link #clicked()} feature, or null if currently
     *        clicked feature should be un-clicked.
     */
    public void setClicked( Feature clicked ) {
        assert isValid();
        Optional<Feature> previous = this.clicked;
        this.clicked = Optional.ofNullable( clicked );
        EventManager.instance().publish( new FeatureClickEvent( this, this.clicked, previous ) );
    }

}
