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
package org.polymap.p4.style;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.style.DefaultStyle;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.style.ui.raster.PredefinedColorMap;

import org.polymap.rhei.batik.toolkit.ActionItem;
import org.polymap.rhei.batik.toolkit.ItemContainer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.model2.Property;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class RasterStyleEditor
        extends StyleEditor<RasterStyleEditorInput> {

    public RasterStyleEditor( RasterStyleEditorInput editorInput ) {
        super( editorInput );
    }

    
    public void createContents( Composite parent, @SuppressWarnings( "hiding" ) MdToolkit tk ) {
        super.createContents( parent, tk );
        
        // toolbar
        new AddGrayscaleItem( toolbar );
        new AddRGBItem( toolbar );
        new AddColorMapItem( toolbar );
    }
    
    
    @Override
    protected StylePropertyFieldSite createFieldSite( Property<StylePropertyValue> prop ) {
        StylePropertyFieldSite fieldSite = new StylePropertyFieldSite();
        fieldSite.prop.set( prop );
        fieldSite.gridCoverage.set( editorInput.gridCoverage.get() );
        fieldSite.maxExtent = editorInput.maxExtent;
        fieldSite.mapExtent = editorInput.mapExtent;
        fieldSite.mapSize = editorInput.mapSize;
        return fieldSite;
    }


    /**
     * 
     */
    protected class AddGrayscaleItem
            extends ActionItem {

        public AddGrayscaleItem( ItemContainer container ) {
            super( container );
            text.set( "Grayscale" );
            //icon.set( P4Plugin.images().svgImage( "grid2.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Add a new Grayscale style" );
            action.set( ev -> {
                DefaultStyle.fillGrayscaleStyle( featureStyle, editorInput.gridCoverage.get() );
                list.refresh( true );
            });
        }
    }

    /**
     * 
     */
    protected class AddRGBItem
            extends ActionItem {

        public AddRGBItem( ItemContainer container ) {
            super( container );
            text.set( "RGB" );
            //icon.set( P4Plugin.images().svgImage( "grid2.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Add a new RGB style" );
            action.set( ev -> {
                DefaultStyle.fillRGBStyle( featureStyle, editorInput.gridCoverage.get() );
                list.refresh( true );
            });
        }
    }

    /**
     * 
     */
    protected class AddColorMapItem
            extends ActionItem {

        public AddColorMapItem( ItemContainer container ) {
            super( container );
            text.set( "ColorMap" );
            //icon.set( P4Plugin.images().svgImage( "grid2.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Add a new ColorMap style" );
            action.set( ev -> {
                DefaultStyle.fillColorMapStyle( featureStyle, editorInput.gridCoverage.get(), PredefinedColorMap.RAINBOW );
                list.refresh( true );
            });
        }
    }

}
