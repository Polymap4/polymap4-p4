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
package org.polymap.p4.style;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.LineStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.PolygonStyle;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StyleGroup;
import org.polymap.core.style.model.TextStyle;

import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class FeatureStyleLabelProvider
        extends CellLabelProvider {

    private static Log log = LogFactory.getLog( FeatureStyleLabelProvider.class );

    private MdToolkit           tk;

    
    public FeatureStyleLabelProvider( MdToolkit tk ) {
        this.tk = tk;
    }


    @Override
    public void update( ViewerCell cell ) {
        Object elm = cell.getElement();
        if (elm instanceof FeatureStyle) {
            cell.setText( "Feature style" );
            cell.setImage( P4Plugin.images().svgImage( "buffer.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            return;
        }
        
        // default title
        String title = ((Style)elm).title.get();
//        title = sanitize( title );
//        title = tk.markdown( title, cell.getItem() );
        
        // Style images
        if (elm instanceof StyleGroup) {
            cell.setText( title != null ? title : "Group" );
            cell.setImage( P4Plugin.images().svgImage( "buffer.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
        }
        else if (elm instanceof PolygonStyle) {
            cell.setText( title != null ? title : "Polygon" );
            cell.setImage( P4Plugin.images().svgImage( "vector-polygon.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );            
        }
        else if (elm instanceof PointStyle) {
            cell.setText( title != null ? title : "Point/Mark" );
            cell.setImage( P4Plugin.images().svgImage( "map-marker.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
        }
        else if (elm instanceof TextStyle) {
            cell.setText( title != null ? title : "Text" );
            // XXX we need a text icon here
            cell.setImage( P4Plugin.images().svgImage( "format-title.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
        }
        else if (cell.getElement() instanceof LineStyle) {
            cell.setText( title != null ? title : "Line" );
            cell.setImage( P4Plugin.images().svgImage( "vector-polyline.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
        }
    }
    
}
