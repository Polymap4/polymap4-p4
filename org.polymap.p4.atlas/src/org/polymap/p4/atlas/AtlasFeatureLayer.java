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
package org.polymap.p4.atlas;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

import org.geotools.data.Query;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.runtime.session.SessionSingleton;

import org.polymap.p4.layer.FeatureLayer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class AtlasFeatureLayer
        extends Configurable {

    private static final Log log = LogFactory.getLog( AtlasFeatureLayer.class );
    
    public static final AtlasFeatureLayer   TYPE = new AtlasFeatureLayer();
    
    private static ConcurrentMap<FeatureLayer,AtlasFeatureLayer>  instances = new MapMaker().weakKeys().makeMap();
            
    /**
     * Returns the one and only {@link AtlasFeatureLayer} for the given
     * {@link ILayer} in the current {@link SessionContext}. If not yet present this
     * computes a new instance.
     *
     * @see FeatureLayer#of(ILayer)
     * @return Newly created or cached instance.
     */
    public static CompletableFuture<Optional<AtlasFeatureLayer>> of( ILayer layer ) {
        return FeatureLayer.of( layer ).thenApply( optionalFeatureLayer -> 
                optionalFeatureLayer.map( fl -> instances.computeIfAbsent( fl, _fl -> new AtlasFeatureLayer( _fl ) ) ) );
    }
    
    /**
     * The fulltext query and map extent commonly used to filter the features of all
     * layers of the current session.
     */
    public static LayerQueryBuilder query() {
        return SessionHolder.instance( SessionHolder.class ).layerQuery;
    }

    static class SessionHolder
            extends SessionSingleton {
        public LayerQueryBuilder    layerQuery = new LayerQueryBuilder();
    }

    
    // instance *******************************************
        
    private FeatureLayer            featureLayer;

    /** Layer is visible in the map. */
    @DefaultBoolean( false )
    @Concern( PropertyChangeEvent.Fire.class )
    public Config<Boolean>          visible;
    
    
    protected AtlasFeatureLayer() {
    }
    
    protected AtlasFeatureLayer( FeatureLayer featureLayer ) {
        this.featureLayer = featureLayer;
    }
    
    public FeatureLayer featureLayer() {
        return featureLayer;
    }

    /** 
     * Shortcut to {@link #featureLayer}.layer(). 
     */
    public ILayer layer() {
        return featureLayer.layer();
    }

    /**
     * Builds a {@link Query} using the currently defined {@link #layerQuery}.
     *
     * @return Newly created {@link Query}.
     * @throws Exception
     */
    public Filter fulltextFilter() throws Exception {
        return query().fulltextFilter( layer() );
    }

}
