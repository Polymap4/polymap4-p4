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
package org.polymap.p4.process;

import java.util.concurrent.atomic.AtomicReference;

import java.text.NumberFormat;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.data.process.ui.FieldViewerSite;
import org.polymap.core.data.process.ui.InputFieldSupplier;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.toolkit.SimpleDialog;

/**
 * Supplies a {@link ReferencedEnvelope}.
 *
 * @author Falko Bräutigam
 */
public class BoundingBoxSupplier
        extends InputFieldSupplier {

    private static final Log log = LogFactory.getLog( BoundingBoxSupplier.class );
    
    private NumberFormat    nf;
    
    private Button          btn;
    
    
    public BoundingBoxSupplier() {
        nf = NumberFormat.getNumberInstance( Polymap.getSessionLocale() );
        nf.setMaximumIntegerDigits( 100 );
        nf.setMaximumFractionDigits( 2 );
        nf.setMinimumIntegerDigits( 1 );
        nf.setMinimumFractionDigits( 2 );        
    }


    @Override
    public String label() {
        return "Bounds";
    }
    
    
    @Override
    public boolean init( @SuppressWarnings( "hiding" ) FieldViewerSite site ) {
        return super.init( site ) &&
                ReferencedEnvelope.class.isAssignableFrom( site.fieldInfo.get().type() );
    }


    @Override
    public void createContents( Composite parent ) {
        // shadows
        parent.setLayout( FormLayoutFactory.defaults().margins( 1, 2, 3, 2 ).create() );
        
        btn = new Button( parent, SWT.PUSH );
        btn.setText( "SELECT..." );
        btn.setToolTipText( "Select a bounding box from a map" );
        btn.addSelectionListener( UIUtils.selectionListener( ev -> {
            openBoundingBoxMap();
        }));
        FormDataFactory.on( btn ).fill();
    }

    
    protected void openBoundingBoxMap() {
        SimpleDialog dialog = new SimpleDialog();
        dialog.title.put( "Bounding box" );
        dialog.addCancelAction();
        
        AtomicReference<ReferencedEnvelope> bounds = new AtomicReference();
        dialog.setContents( parent -> {
            parent.setLayout( FormLayoutFactory.defaults().create() );
            BoundingBoxMapViewer mapViewer = new BoundingBoxMapViewer( parent, site.getFieldValue() ) {
                @Override
                protected void onBoundingBox( ReferencedEnvelope newBounds ) {
                    bounds.set( newBounds );
                }
            };
            FormDataFactory.on( mapViewer.getControl() ).fill().width( 450 ).height( 400 );
        });
        dialog.addOkAction( () -> {
            onBounds( bounds.get() );
            dialog.close();
            return true;
        });
        dialog.open();
    }


    protected void onBounds( ReferencedEnvelope bounds ) {
        log.info( "" + bounds );
        site.setFieldValue( bounds );
        
        StringBuilder buf = new StringBuilder( 256 )
                .append( nf.format( bounds.getMinX() ) ).append( ":" ).append( nf.format( bounds.getMaxX() ) )
                .append( " - " )
                .append( nf.format( bounds.getMinY() ) ).append( ":" ).append( nf.format( bounds.getMaxY() ) );
        btn.setText( buf.toString() );
        btn.setToolTipText( buf.toString() );
    }
    
}
