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
package org.polymap.p4.style;

import org.eclipse.swt.widgets.Composite;
import org.polymap.core.style.DefaultStyle;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.rhei.batik.toolkit.ActionItem;
import org.polymap.rhei.batik.toolkit.ItemContainer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.model2.Property;
import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FeatureStyleEditor
        extends StyleEditor<FeatureStyleEditorInput> {

    public FeatureStyleEditor( FeatureStyleEditorInput editorInput ) {
        super( editorInput );
    }

    
    public void createContents( Composite parent, @SuppressWarnings( "hiding" ) MdToolkit tk ) {
        super.createContents( parent, tk );
        
        // toolbar
        new AddTextItem( toolbar );
        new AddPointItem( toolbar );
        new AddPolygonItem( toolbar );
        new AddLineItem( toolbar );
    }
    
    
    @Override
    protected StylePropertyFieldSite createFieldSite( Property<StylePropertyValue> prop ) {
        StylePropertyFieldSite fieldSite = new StylePropertyFieldSite();
        fieldSite.prop.set( prop );
        fieldSite.featureStore.set( editorInput.featureStore.get() );
        fieldSite.featureType.set( editorInput.featureType.get() );
        fieldSite.maxExtent = editorInput.maxExtent;
        fieldSite.mapExtent = editorInput.mapExtent;
        fieldSite.mapSize = editorInput.mapSize;
        return fieldSite;
    }


    /**
     * 
     */
    protected class AddPointItem
            extends ActionItem {

        public AddPointItem( ItemContainer container ) {
            super( container );
            icon.set( P4Plugin.images().svgImage( "map-marker.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Create a new Point/Marker render description" );
            action.set( ev -> {
                DefaultStyle.fillPointStyle( featureStyle );
                list.refresh( true );
            });
        }
    }

    
    /**
     * 
     */
    protected class AddPolygonItem
            extends ActionItem {

        public AddPolygonItem( ItemContainer container ) {
            super( container );
            icon.set( P4Plugin.images().svgImage( "vector-polygon.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Create a new Polygon render description" );
            action.set( ev -> {
                DefaultStyle.fillPolygonStyle( featureStyle );
                list.refresh( true );
            } );
        }
    }


    /**
     * 
     */
    protected class AddLineItem
            extends ActionItem {

        public AddLineItem( ItemContainer container ) {
            super( container );
            icon.set( P4Plugin.images().svgImage( "vector-polyline.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Create a new Line render description" );
            action.set( ev -> {
                DefaultStyle.fillLineStyle( featureStyle );
                list.refresh( true );
            } );
        }
    }


    /**
     * 
     */
    protected class AddTextItem
            extends ActionItem {

        public AddTextItem( ItemContainer container ) {
            super( container );
            // XXX we need a text icon here
            icon.set( P4Plugin.images().svgImage( "format-title.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Create a new Text render description" );
            action.set( ev -> {
                DefaultStyle.fillTextStyle( featureStyle, editorInput.featureType.get() );
                list.refresh( true );
            });
        }
    }

}
