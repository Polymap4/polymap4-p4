/*
 * polymap.org Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.map;

import static org.polymap.core.ui.FormDataFactory.on;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Envelope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.security.SecurityContext;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.Messages;
import org.polymap.p4.P4AppDesign;
import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.layer.FeatureSelection;
import org.polymap.p4.layer.FeatureSelectionEvent;
import org.polymap.p4.project.ProjectRepository;
import org.polymap.rap.openlayers.base.OlFeature;
import org.polymap.rap.openlayers.base.OlMap;
import org.polymap.rap.openlayers.control.MousePositionControl;
import org.polymap.rap.openlayers.control.ScaleLineControl;
import org.polymap.rap.openlayers.format.GeoJSONFormat;
import org.polymap.rap.openlayers.geom.PointGeometry;
import org.polymap.rap.openlayers.layer.Layer;
import org.polymap.rap.openlayers.layer.VectorLayer;
import org.polymap.rap.openlayers.source.Source;
import org.polymap.rap.openlayers.source.VectorSource;
import org.polymap.rap.openlayers.style.CircleStyle;
import org.polymap.rap.openlayers.style.FillStyle;
import org.polymap.rap.openlayers.style.StrokeStyle;
import org.polymap.rap.openlayers.style.Style;
import org.polymap.rap.openlayers.types.Attribution;
import org.polymap.rap.openlayers.types.Color;
import org.polymap.rap.openlayers.types.Coordinate;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectMapPanel
        extends P4Panel {

    private static Log                  log  = LogFactory.getLog( ProjectMapPanel.class );

    public static final PanelIdentifier ID   = PanelIdentifier.parse( "start" );

    private static final IMessages      i18n = Messages.forPrefix( "ProjectPanel" );

    /**
     * The map of this P4 instance. This instance belongs to
     * {@link ProjectRepository#unitOfWork()}. Don't forget to load a local copy for
     * an nested {@link UnitOfWork} if you are going to modify anything.
     */
    @Scope(P4Plugin.Scope)
    protected Context<IMap>             map;

    public MapViewer<ILayer>            mapViewer;

    private Composite                   tableParent;

    private Layer<? extends Source>     vectorSelected;

    @Override
    public void init() {
        // the 'start' panel initializes context
        map.compareAndSet( null, ProjectRepository.unitOfWork().entity( IMap.class, "root" ) );

        // XXX fake user login; used by ProjectNodeUser for example
        SecurityContext sc = SecurityContext.instance();
        if (!sc.isLoggedIn()) {
            if (!sc.login( "admin", "admin" )) {
                throw new RuntimeException( "Default/fake login did not succeed." );
            }
        }
    }


    @Override
    public void dispose() {
    }


    @Override
    public void createContents( Composite parent ) {
        // title and layout
        String title = map.get().label.get();
        site().title.set( title );
        site().preferredWidth.set( 650 );

        ((P4AppDesign)BatikApplication.instance().getAppDesign()).setAppTitle( title );

        // parent.setBackground( UIUtils.getColor( 0xff, 0xff, 0xff ) );
        parent.setLayout( FormLayoutFactory.defaults().margins( 0 ).spacing( 0 ).create() );

        // buttom toolbar
        MdToolbar2 tb = ((MdToolkit)site().toolkit()).createToolbar( parent );
        on( tb.getControl() ).fill().noTop();
        tb.getControl().moveAbove( null );

        ContributionManager.instance().contributeTo( tb, this );

        // table area
        tableParent = on( site().toolkit().createComposite( parent, SWT.NONE ) )
                .fill().bottom( tb.getControl() ).noTop().height( 0 ).control();

        // mapViewer
        try {
            mapViewer = new MapViewer( parent );
            // triggers {@link MapViewer#refresh()} on {@link
            // ProjectNodeCommittedEvent}
            mapViewer.contentProvider.set( new ProjectContentProvider() );
            mapViewer.layerProvider.set( new ProjectLayerProvider() );

            // FIXME
            CoordinateReferenceSystem epsg3857 = Geometries.crs( "EPSG:3857" );
            mapViewer.maxExtent.set( new ReferencedEnvelope( 1380000, 1390000, 6680000, 6690000, epsg3857 ) );

            mapViewer.addMapControl( new MousePositionControl() );
            mapViewer.addMapControl( new ScaleLineControl() );

            mapViewer.setInput( map.get() );
            on( mapViewer.getControl() ).fill().bottom( tableParent );
            mapViewer.getControl().moveBelow( null );
            mapViewer.getControl().setBackground( UIUtils.getColor( 0xff, 0xff, 0xff ) );

            mapViewer.getMap().addEventListener( OlMap.EVENT.click, event -> {
                JsonObject json = event.properties();
                JsonObject feature = (JsonObject)json.get( "feature" );
                JsonArray coordinate = (JsonArray)feature.get( "coordinate" );
                handleSelectionEvent( coordinate.get( 0 ).asDouble(), coordinate.get( 1 ).asDouble(), true );
            } );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

        ContributionManager.instance().contributeTo( this, this );
    }


    private void handleSelectionEvent( double x, double y, boolean selectionFromMap ) {
        Filter filter = Filter.INCLUDE;
        FeatureSelection foundSelection = null;
        Feature foundFeature = null;
        Double foundX = null;
        Double foundY = null;
        if (vectorSelected != null) {
            mapViewer.getMap().removeLayer( vectorSelected );
            vectorSelected = null;
        }
        FeatureSelection selection;
        for (ILayer layer : mapViewer.getLayers()) {
            selection = FeatureSelection.forLayer( layer );
            if (selection != null) {
                foundSelection = selection;
                try {
                    FeatureStore fs = selection.waitForFs().get();
                    FeatureIterator featureIt = fs.getFeatures().features();
                    Feature feature;
                    while (featureIt.hasNext()) {
                        feature = featureIt.next();
                        Envelope bounds = ((ReferencedEnvelope)feature.getDefaultGeometryProperty().getBounds());
                        MathTransform transform = CRS.findMathTransform( CRS.decode( "EPSG:4326" ),
                                CRS.decode( "EPSG:3857" ), false );
                        try {
                            bounds = JTS.transform( bounds, transform );
                            if (bounds.getMinX() >= x && x <= bounds.getMaxX()
                                    && bounds.getMinY() >= y && y <= bounds.getMaxY()) {
                                foundX = bounds.getMinX();
                                foundY = bounds.getMinY();
                                foundFeature = feature;
                                foundSelection.setClicked( foundFeature );
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (foundFeature != null) {
            VectorSource sourceSelected = new VectorSource().format.put( new GeoJSONFormat() ).attributions
                    .put( Arrays.asList( new Attribution( "Selected" ) ) );

            vectorSelected = new VectorLayer().style.put( new Style().fill.put( new FillStyle().color
                    .put( new Color( 0, 0, 255, 0.1f ) ) ).stroke.put( new StrokeStyle().color.put( new Color(
                    "red" ) ).width.put( 1f ) ) ).source.put( sourceSelected );

            OlFeature olFeatureSelected = new OlFeature();
            olFeatureSelected.id.set( foundFeature.getIdentifier().getID() );
            olFeatureSelected.geometry.set( new PointGeometry( new Coordinate( foundX, foundY ) ) );
            olFeatureSelected.style.put( new Style().stroke.put( new StrokeStyle().color.put( new Color(
                    "green" ) ).width.put( 2f ) ).image.put( new CircleStyle( 5.0f ).fill
                    .put( new FillStyle().color.put( new Color( "blue" ) ) ) ) );
            sourceSelected.addFeature( olFeatureSelected );

            mapViewer.getMap().addLayer( vectorSelected );

            EventManager.instance().publish( new FeatureSelectionEvent( foundSelection, filter, filter ) );
        }
        else {
            if (foundSelection != null) {
                EventManager.instance().publish( new FeatureSelectionEvent( foundSelection, filter, filter ) );
                if (vectorSelected != null) {
                    mapViewer.getMap().removeLayer( vectorSelected );
                }
            }
        }
    }


    /**
     * Simple/experimental way to add bottom view to this panel.
     *
     * @param creator
     */
    public void addButtomView( Consumer<Composite> creator ) {
        on( tableParent ).height( 200 );

        UIUtils.disposeChildren( tableParent );
        creator.accept( tableParent );
        tableParent.getParent().layout();
    }

}
