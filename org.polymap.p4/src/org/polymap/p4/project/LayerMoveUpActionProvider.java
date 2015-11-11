/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.project;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Tree;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.ILayer;
import org.polymap.model2.ManyAssociation;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.P4Plugin;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;

/**
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 */
public class LayerMoveUpActionProvider 
        extends ActionProvider {
	
    private static Log log = LogFactory.getLog( LayerMoveUpActionProvider.class );
    
    private final MapViewer<ILayer> mapViewer;
    
	/**
     * @param mapViewer
     */
    public LayerMoveUpActionProvider( MapViewer<ILayer> mapViewer ) {
        this.mapViewer = mapViewer;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.
	 * viewers.ViewerCell)
	 */
	@Override
	public void update(ViewerCell cell) {
	    if(((Tree) cell.getControl()).getItem( 0 ) != cell.getItem()) {
	        cell.setImage( P4Plugin.images().svgImage( "ic_keyboard_arrow_up_48px.svg", NORMAL24 ) );
	    }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.polymap.rhei.batik.toolkit.md.ActionProvider#perform(org.polymap.
	 * rhei.batik.toolkit.md.MdListViewer, java.lang.Object)
	 */
	@Override
    public void perform( MdListViewer viewer, Object element ) {
        if (element instanceof ILayer) {
            ILayer layer = (ILayer)element;
            try {
                UnitOfWork uow = ProjectRepository.newUnitOfWork();
                ManyAssociation<ILayer> layers = layer.parentMap.get().layers;
                Iterator<ILayer> iterator = layers.iterator();
                SortedMap<Integer, ILayer> map = new TreeMap<Integer, ILayer>();
                ILayer l;
                while(iterator.hasNext()) {
                    l = iterator.next();
                    if(l != layer) {
                        map.put( l.orderKey.get(), l );
                    }
                }
                List<ILayer> newLayerOrder = new ArrayList<ILayer>();
                layer.orderKey.set( 0 );
                newLayerOrder.add( layer );
                int i=1;
                for(ILayer lay : map.values()) {
                    lay.orderKey.set( i++ );
                    newLayerOrder.add( lay );
                }
                uow.commit();
                viewer.refresh();
                mapViewer.refresh();
            }
            catch (Throwable e) {
                log.error( e );
            }
        }
    }
}
