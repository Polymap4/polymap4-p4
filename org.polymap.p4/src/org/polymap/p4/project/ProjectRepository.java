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
package org.polymap.p4.project;

import static org.polymap.core.data.DataPlugin.ff;

import java.io.File;
import java.io.IOException;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.CorePlugin;
import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.data.image.cache304.ImageCacheProcessor;
import org.polymap.core.data.pipeline.ProcessorExtension;
import org.polymap.core.data.util.Geometries;
import org.polymap.core.project.EnvelopeComposite;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.ILayer.ProcessorConfig;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.runtime.session.SessionSingleton;
import org.polymap.core.style.DefaultStyle;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.feature.ConstantColor;
import org.polymap.core.style.model.feature.ConstantNumber;
import org.polymap.core.style.model.feature.FilterMappedPrimitives;
import org.polymap.core.style.model.feature.PolygonStyle;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.locking.OptimisticLocking;
import org.polymap.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.AllResolver;
import org.polymap.p4.catalog.LocalCatalog;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectRepository {

    private static final Log log = LogFactory.getLog( ProjectRepository.class );
    
    public static final String          ROOT_MAP_ID = "root";

    private static EntityRepository     repo;

    
    static {
        try {
            File dir = new File( CorePlugin.getDataLocation( P4Plugin.instance() ), "project" );
            dir.mkdirs();
            LuceneRecordStore store = LuceneRecordStore.newConfiguration()
                    .indexDir.put( dir )
                    .clean.put( false )
                    .executor.put( null )
                    .create();
            
            repo = EntityRepository.newConfiguration()
                    .entities.set( new Class[] {
                            IMap.class, 
                            ILayer.class, 
                            ILayer.LayerUserSettings.class, 
                            MetadataReference.class } )
                    .store.set( 
                            new OptimisticLocking( 
                            new RecordStoreAdapter( store ) ) )
                    .create();
            
            checkInitRepo();
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    protected static void checkInitRepo() {
        try (
            UnitOfWork uow = repo.newUnitOfWork()
        ){
            String srs = "EPSG:3857";
            CoordinateReferenceSystem epsg3857 = Geometries.crs( srs );
            ReferencedEnvelope maxExtent = new ReferencedEnvelope( 
                    -20026376.39, 20026376.39, -20048966.10, 20048966.10, epsg3857 );

            IMap map = uow.entity( IMap.class, "root" );
            if (map == null) {
                // The one and only project of a P4 instance
                map = uow.createEntity( IMap.class, ROOT_MAP_ID, (IMap proto) -> {
                    proto.label.set( "Project" );
                    proto.srsCode.set( srs );
                    proto.maxExtent.createValue( EnvelopeComposite.defaults( maxExtent ) );
                    return proto;
                });
                
                // default background layer from mapzone.io
                try {
                    // WMS
                    NullProgressMonitor monitor = new NullProgressMonitor();
//                    IMetadata md = P4Plugin.localCatalog().entry( LocalCatalog.WORLD_BACKGROUND_ID, monitor ).get();
//                    IServiceInfo service = (IServiceInfo)AllResolver.instance().resolve( md ).get();
//                    for (IResourceInfo res : service.getResources( monitor )) {
//                        if ("Simple".equalsIgnoreCase( res.getName() ) ) {
//                            ILayer layer = uow.createEntity( ILayer.class, null, (ILayer proto) -> {
//                                proto.label.set( "World - WMS" );
//                                proto.description.set( res.getDescription().orElse( null ) );
//                                proto.resourceIdentifier.set( AllResolver.resourceIdentifier( res ) );
//                                proto.orderKey.set( 1 );
//                                return proto;
//                            });
//                            layer.parentMap.set( map );
//                            break;
//                        }
//                    }
                    // Features
                    IMetadata md = P4Plugin.localCatalog().entry( LocalCatalog.LOCAL_FEATURES_STORE_ID, monitor ).get();
                    IServiceInfo service = (IServiceInfo)AllResolver.instance().resolve( md ).get();
                    for (IResourceInfo res : service.getResources( monitor )) {
                        if (res.getName().equals( LocalCatalog.WORLD_BORDER_RES )) {
                            ILayer layer = uow.createEntity( ILayer.class, null, (ILayer proto) -> {
                                proto.label.set( "World Borders" );
                                proto.description.set( "Sample features imported from thematicmapping.org/ downloads/world_borders.php.\n\n"
                                        + "You may use this layer to try out styling of features." );
                                proto.resourceIdentifier.set( AllResolver.resourceIdentifier( res ) );
                                proto.orderKey.set( 2 );
                                // style
                                FeatureStyle style = P4Plugin.styleRepo().newFeatureStyle();
                                PolygonStyle polygon = DefaultStyle.fillPolygonStyle( style );
                                polygon.fill.get().color.createValue( ConstantColor.defaults( 0xF5, 0xE4, 0x9C ) );
                                polygon.stroke.get().color.createValue( ConstantColor.defaults( 0xD4, 0xAB, 0x89 ) );
                                polygon.stroke.get().width.createValue( ConstantNumber.defaults( 0.2 ) );
                                // opacity: see NumberGradient2FilterEditor
                                int upper = 1300000;
                                int breakpoints = 9;
                                PropertyName prop = ff.property( "AREA" );
                                FilterMappedPrimitives<Double> mapped = polygon.fill.get().opacity.createValue( FilterMappedPrimitives.defaults() );
                                mapped.add( ff.less( prop, ff.literal( upper/breakpoints ) ), 0.1 );
                                for (int i=0; i<breakpoints; i++) {
                                    mapped.add( ff.and( 
                                            ff.greaterOrEqual( prop, ff.literal( upper/breakpoints*i ) ), 
                                            ff.less( prop, ff.literal( upper/breakpoints*(i+1) ) ) ), 0.1 + (1d/breakpoints*i) );
                                }
                                mapped.add( ff.greaterOrEqual( prop, ff.literal( upper ) ), 1d );
                                style.store();
                                proto.styleIdentifier.set( style.id() );
                                // image cache
                                ProcessorExtension ext = ProcessorExtension.forType( ImageCacheProcessor.class.getName() )
                                        .orElseThrow( () -> new RuntimeException( "ImageCacheProcessor is not available. Is the plugin installed?" ) );
                                proto.processorConfigs.createElement( ProcessorConfig.init( ext ) );
                                return proto;
                            });
                            layer.parentMap.set( map );
                            break;
                        }
                    }
                }
                catch (Exception e) {
                    log.warn( "Error while creating default background layer.", e );
                }
            }
            else {
                // convert legacy setting
                if (map.maxExtent().getMinX() == ReferencedEnvelope.EVERYTHING.getMinX() ) {
                    log.info( "Converting old EVERYTHING envelope..." );
                    map.srsCode.set( srs );
                    map.maxExtent.createValue( EnvelopeComposite.defaults( maxExtent ) );
                }
            }
            uow.commit();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    /**
     * The instance of the current {@link SessionContext}.
     */
    public static UnitOfWork unitOfWork() {
        return SessionHolder.instance( SessionHolder.class ).uow;
    }

    
    static class SessionHolder
            extends SessionSingleton {
        UnitOfWork uow = repo.newUnitOfWork();
    }
    
    
    public static UnitOfWork newUnitOfWork() {
        return repo.newUnitOfWork();
    }
    
}
