/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.UIThreadExecutor;

import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.toolkit.MinHeightConstraint;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.table.DefaultFeatureTableColumn;
import org.polymap.rhei.table.FeatureCollectionContentProvider;
import org.polymap.rhei.table.FeatureTableViewer;

import org.polymap.p4.P4Panel;

/**
 * Preview {@link MapViewer} of an {@link IServiceInfo} or {@link IResourceInfo}.
 *
 * @author Falko Bräutigam
 */
public class PreviewFeaturesDashlet
        extends DefaultDashlet {

    private static final Log log = LogFactory.getLog( PreviewFeaturesDashlet.class );

    private IResourceInfo   resInfo;

    
    public PreviewFeaturesDashlet( IResourceInfo resInfo ) {
        this.resInfo = resInfo;
    }

    
    @Override
    public void init( DashletSite site ) {
        super.init( site );
        site.title.set( "Data preview" );
        //site.constraints.get().add( new PriorityConstraint( 100 ) );
        site.constraints.get().add( new MinWidthConstraint( P4Panel.SIDE_PANEL_WIDTH, 1 ) );
        site.constraints.get().add( new MinHeightConstraint( P4Panel.SIDE_PANEL_WIDTH-60, 1 ) );
        site.border.set( true );
    }

    
    @Override
    public void createContents( Composite parent ) {
        new UIJob( "Create map") {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                try {
                    Object service = resInfo.getServiceInfo().createService( monitor );
                    UIThreadExecutor.async( () -> {
                        if (service instanceof DataAccess) {
                            createFeatureTable( parent, (DataAccess)service );
                        }
                        else {
                            parent.setLayout( new FillLayout() );
                            site().toolkit().createFlowText( parent, "This resource does not contain data to preview in a table." );
                        }
                        return null;
                    });
                }
                catch (Exception e) {
                    log.warn( "", e );
                    getSite().toolkit().createLabel( parent, "Unable to created preview." );
                } 
            }
        }.scheduleWithUIUpdate();
    }
    
    
    protected void createFeatureTable( Composite parent, DataAccess ds ) throws Exception {
        parent.setLayout( new FillLayout() );
        FeatureSource fs = ds.getFeatureSource( new NameImpl( resInfo.getName() ) );
        SimpleFeatureType schema = (SimpleFeatureType)fs.getSchema();

        FeatureTableViewer table = new FeatureTableViewer( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION );
        table.setContentProvider( new FeatureCollectionContentProvider() );

        for (PropertyDescriptor prop : schema.getDescriptors()) {
            if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                // skip Geometry
            }
            else {
                DefaultFeatureTableColumn column = new DefaultFeatureTableColumn( prop );
                column.setWeight( 1, 65 );
                table.addColumn( column );
            }
        }

        Query query = new Query();
        query.setMaxFeatures( 25 );
        table.setInput( fs.getFeatures( query ) );
    }
    
}
