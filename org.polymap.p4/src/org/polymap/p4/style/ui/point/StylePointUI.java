/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
 * @authors tag. All rights reserved.
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
package org.polymap.p4.style.ui.point;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.style.entities.StylePoint;
import org.polymap.p4.style.point.IStylePointInfo;
import org.polymap.p4.style.point.StyleFigurePanel;
import org.polymap.p4.style.point.StyleImagePanel;
import org.polymap.p4.style.point.StylePointInfo;
import org.polymap.p4.style.ui.AbstractStylerFragmentUI;
import org.polymap.p4.util.PropertyAdapter;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.AbstractDelegatingFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.field.SpinnerFormField;
import org.polymap.rhei.form.IFormPageSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StylePointUI
        extends AbstractStylerFragmentUI
        implements IFormFieldListener {

    private final IAppContext                     context;

    private final IPanelSite                      panelSite;

    private SpinnerFormField                      markerSizeField;

    private SpinnerFormField                      markerRotationFormField;

    private final Context<IStylePointInfo>        stylePointInfo;

    private Supplier<StylePoint>                  stylePointSupplier   = null;

    private UnitOfWork                            stylePointUnitOfWork = null;

    private StylePoint                            stylePoint           = null;

    private StylePointUIDelegatingFormField       markerFigureFormField;

    private StylePointUIDelegatingFormField       markerImageFormField;


    private static class StylePointUIDelegatingFormField
            extends AbstractDelegatingFormField<StylePoint> {

        private StylePoint stylePoint;


        StylePointUIDelegatingFormField( StylePoint stylePoint ) {
            this.stylePoint = stylePoint;
        }


        protected void handleSelectionEvent( IFormFieldSite site, org.eclipse.swt.events.SelectionEvent e ) {
            site.fireEvent( this, IFormFieldListener.VALUE_CHANGE, null );
        }


        @Override
        public IFormField setValue( Object value ) {
            if (value instanceof StylePoint) {
                setCurrentValue( (StylePoint)stylePoint );
            }
            return this;
        }


        @Override
        protected void processLoadedValue( Object loadedValue ) {
            if (loadedValue instanceof StylePoint) {
                setCurrentValue( (StylePoint)stylePoint );
            }
        }
    }


    public StylePointUI( IAppContext context, IPanelSite panelSite, Context<IStylePointInfo> stylePointInfo ) {
        this.context = context;
        this.panelSite = panelSite;
        this.stylePointInfo = stylePointInfo;
    }


    public void setModelFunction( Supplier<StylePoint> stylePointSupplier ) {
        this.stylePointSupplier = stylePointSupplier;
        this.stylePoint = null;
    }


    public void setUnitOfWork( UnitOfWork stylePointUnitOfWork ) {
        this.stylePointUnitOfWork = stylePointUnitOfWork;
    }


    @Override
    public Composite createContents( IFormPageSite site ) {
        Composite parent = site.getPageBody();
        parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 )
                .margins( panelSite.getLayoutPreference().getSpacing() / 2 ).create() );
        if (stylePoint == null) {
            stylePoint = stylePointSupplier.get();
        }
        markerSizeField = new SpinnerFormField( 1, 128, 12 );
        site.newFormField( new PropertyAdapter( stylePoint.markerSize ) ).label.put( "Marker size" ).field
                .put( markerSizeField ).tooltip.put( "" ).create();
        markerFigureFormField = new StylePointUIDelegatingFormField( stylePoint );
        site.newFormField( new PropertyAdapter( stylePoint.markerFigure ) ).label.put( "Marker Figure" ).field
                .put( markerFigureFormField ).tooltip.put( "Geometric figure to represent this point" )
                .create();
        markerImageFormField = new StylePointUIDelegatingFormField( stylePoint );
        site.newFormField( new PropertyAdapter( stylePoint.markerImage ) ).label.put( "Marker Image" ).field
                .put( markerImageFormField ).tooltip.put( "Image to represent this point" )
                .create();
        markerRotationFormField = new SpinnerFormField( -360, 360, 0 );
        markerRotationFormField.setEnabled( false );
        site.newFormField( new PropertyAdapter( stylePoint.markerRotation ) ).label.put( "Marker rotation" ).field
                .put( markerRotationFormField ).tooltip.put( "" ).create();
        
        site.addFieldListener( this );
        
        return site.getPageBody();
    }


    @Override
    public void submitUI() {
        if (stylePointUnitOfWork != null && stylePointUnitOfWork.isOpen()) {
            stylePointUnitOfWork.commit();
        }
    }


    @Override
    public void resetUI() {
        if (stylePointUnitOfWork != null && stylePointUnitOfWork.isOpen()) {
            stylePointUnitOfWork.close();
        }
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == VALUE_CHANGE) {
            if (ev.getSource() == markerFigureFormField) {
                StylePointInfo impl = new StylePointInfo( markerFigureFormField,
                        stylePointUnitOfWork.newUnitOfWork(), stylePoint );
                stylePointInfo.set( impl );
                context.openPanel( panelSite.getPath(), StyleFigurePanel.ID );
            }
            else if (ev.getSource() == markerImageFormField) {
                StylePointInfo impl = new StylePointInfo( markerImageFormField, stylePointUnitOfWork.newUnitOfWork(),
                        stylePoint );
                stylePointInfo.set( impl );
                context.openPanel( panelSite.getPath(), StyleImagePanel.ID );
            }
        }
    }
}
