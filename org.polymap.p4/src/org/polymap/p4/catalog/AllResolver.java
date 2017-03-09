/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.geotools.data.FeatureSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.IMetadataCatalog;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResolvableInfo;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.catalog.resolve.ResourceResolverExtension;
import org.polymap.core.data.pipeline.DataSourceDescription;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.JobExecutor;
import org.polymap.core.runtime.SubMonitor;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.layer.FeatureLayer;

/**
 * Provides the connection between an {@link ILayer} -> {@link IMetadata} ->
 * {@link IServiceInfo} and back.
 * <p/>
 * Holds a list of delegate {@link IMetadataResourceResolver}s which are responsible
 * of actually creating a service/resource out of a metadata entry. A {@link ILayer}
 * is connected to a metadata entry via the
 * {@link #resourceIdentifier(IResourceInfo)} which consists of the metadata
 * identifier and the resource name.
 *
 * @see IMetadataResourceResolver
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AllResolver
        implements IMetadataResourceResolver {

    private static Log log = LogFactory.getLog( AllResolver.class );

    public static final char                ID_DELIMITER = '|';
    
    /**
     * Returns {@link P4Plugin#allResolver()}.
     */
    public static AllResolver instance() {
        return P4Plugin.allResolver();
    }
    
    
    // instance *******************************************
    
    private List<IMetadataCatalog>          catalogs;

    /** The delegates. */
    private List<IMetadataResourceResolver> resolvers; 

    /**
     * Caches {@link IResolvableInfo} instances in order to have just one underlying
     * service instances (WMS, Shape, RDataStore, etc.) per JVM.
     * <p/>
     * Using {@link ConcurrentHashMap} instead of {@link Cache} ensures that mapping
     * function is executed at most once per key and there is actually just one
     * resolvable constructed per metadata.
     */
    private Map<IMetadata,CompletableFuture<IResolvableInfo>> resolved = new ConcurrentHashMap();
    
    /**
     * Caches {@link IMetadata}.
     */
    private Cache<String,IMetadata>         metadataCache = CacheConfig.defaults().initSize( 128 ).createCache();
    
    
    public AllResolver( List<IMetadataCatalog> catalogs ) {
        assert !catalogs.isEmpty();
        this.catalogs = catalogs;
        this.resolvers = ResourceResolverExtension.createAllResolvers(); 
    }


    public static String resourceIdentifier( IResourceInfo res ) {
        IServiceInfo serviceInfo = res.getServiceInfo();
        IMetadata metadata = serviceInfo.getMetadata();
        return metadata.getIdentifier() + ID_DELIMITER + res.getName();
    }
    
    
    /**
     * Index 0: metadata identifier; index 1: resource name 
     */
    public static String[] parseResourceIdentifier( String resId ) {
        assert resId.indexOf( ID_DELIMITER ) > 0;
        return StringUtils.split( resId, ID_DELIMITER );
    }
    
    
    /**
     * Connects the given layer with its backend service.
     * <p/>
     * Consider {@link FeatureLayer} if interested in the {@link FeatureSource}
     * of a layer.
     *
     * @see FeatureLayer
     * @param layer
     * @param monitor
     * @return Newly created {@link DataSourceDescription} with (cached) service
     *         instance.
     * @throws Exception
     */
    public Optional<DataSourceDescription> connectLayer( ILayer layer, IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( "Connect layer \"" + layer.label.get() + "\"", 10 );
        IServiceInfo serviceInfo = serviceInfo( layer, SubMonitor.on( monitor, 5 ) ).orElse( null );
        
        if (serviceInfo != null) {
            String resName = parseResourceIdentifier( layer.resourceIdentifier.get() )[1];
            Object service = serviceInfo.createService( SubMonitor.on( monitor, 5 ) );
            monitor.done();
            
            return Optional.of( new DataSourceDescription()
                    .service.put( service )
                    .resourceName.put( resName ) );
        }
        return Optional.empty();
    }
    

    public Optional<IMetadata> metadata( ILayer layer, IProgressMonitor monitor ) throws Exception {
        try {
            String metadataId = parseResourceIdentifier( layer.resourceIdentifier.get() )[0];
            
            return Optional.ofNullable( metadataCache.get( metadataId, key -> {
                monitor.beginTask( "Metadata", catalogs.size() );
                for (IMetadataCatalog catalog : catalogs) {
                    Optional<? extends IMetadata> result = catalog.entry( metadataId, monitor );
                    if (result.isPresent()) {
                        return result.get();
                    }
                    monitor.worked( 1 );
                }
                return null;
            }));
        }
        finally {
            monitor.done();
        }
    }
    
    
    public Optional<IServiceInfo> serviceInfo( ILayer layer, IProgressMonitor monitor ) throws Exception {
        try {
            monitor.beginTask( "Service", 10 );
            IMetadata metadata = metadata( layer, SubMonitor.on( monitor, 5 ) ).orElse( null );

            return Optional.ofNullable( metadata != null
                    ? (IServiceInfo)resolve( metadata, SubMonitor.on( monitor, 5 ) ) : null );
        }
        finally {
            monitor.done();
        } 
    }
    
    
    public Optional<IResourceInfo> resInfo( ILayer layer, IProgressMonitor monitor ) throws Exception {
        try {
            monitor.beginTask( "Resource", 10 );
            IServiceInfo serviceInfo = serviceInfo( layer, SubMonitor.on( monitor, 5 ) ).orElse( null );
            
            if (serviceInfo != null) {
                String resName = parseResourceIdentifier( layer.resourceIdentifier.get() )[1];
                SubMonitor submon = SubMonitor.on( monitor, 5 );
                submon.beginTask( "Resources", IProgressMonitor.UNKNOWN );
                for (IResourceInfo info : serviceInfo.getResources( submon )) {
                    if (info.getName().equals( resName )) {
                        return Optional.of( info );
                    }
                }
            }
            return Optional.empty();
        }
        finally {
            monitor.done();
        }        
    }

  
    // IMetadataResourceResolver **************************

    /**
     * {@inheritDoc}
     * <p/>
     * Resolves info for the given metadata and caches the result.
     * 
     * @param metadata
     * @param monitor
     * @return Created or cached info instance.
     */
    @Override
    public CompletableFuture<IResolvableInfo> resolve( IMetadata metadata ) {
        return resolved.computeIfAbsent( metadata, key -> {
            return doResolve( metadata, null );
        });
    }

    
    @Override
    public IResolvableInfo resolve( IMetadata metadata, IProgressMonitor monitor ) throws Exception {
        return resolved.computeIfAbsent( metadata, key -> {
            return doResolve( metadata, monitor );
        }).get();
    }
    
    
    protected CompletableFuture<IResolvableInfo> doResolve( IMetadata metadata, IProgressMonitor monitor ) {
        monitor.beginTask( "Resolve", resolvers.size() );
        for (IMetadataResourceResolver resolver : resolvers) {
            if (resolver.canResolve( metadata ) ) {
                return CompletableFuture.supplyAsync( () -> {
                    try {
                        IProgressMonitor mon = /*monitor != null ? monitor :*/ UIJob.monitorOfThread();
                        return resolver.resolve( metadata, mon );
                    }
                    catch (Exception e) {
                        log.warn( "", e );
                        throw Throwables.propagate( e );
                    }
                }, JobExecutor.withProgress() );
            }
            monitor.worked( 1 );
        }
        throw new IllegalStateException( "Unable to resolve: " + metadata );
    }


    @Override
    public boolean canResolve( IMetadata metadata ) {
        return resolved.get( metadata ) == null
                ? resolvers.stream().filter( r -> r.canResolve( metadata ) ).findFirst().isPresent()
                : true;
    }

    @Override
    public Map<String,String> createParams( Object service ) {
        throw new RuntimeException( "Not yet implemented for AllResolver." );
    }
    
}
