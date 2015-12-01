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
package org.polymap.p4.style.ui.label;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.style.color.ColorInfo;
import org.polymap.p4.style.color.ColorPanel;
import org.polymap.p4.style.color.IColorInfo;
import org.polymap.p4.style.entities.StyleColor;
import org.polymap.p4.style.entities.StyleLabel;
import org.polymap.p4.style.ui.AbstractStylerFragmentUI;
import org.polymap.p4.util.PropertyAdapter;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.ColorFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.field.SpinnerFormField;
import org.polymap.rhei.form.IFormPageSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelHaloUI
        extends AbstractStylerFragmentUI
        implements IFormFieldListener {

    private final IAppContext         context;

    private final IPanelSite          panelSite;

    private SpinnerFormField          labelHaloRadiusFormField;

    private StyleHaloColorFormField   labelHaloFillFormField;

    private StyleLabel                styleLabel               = null;

    private final Context<IColorInfo> colorInfoInContext;

    private UnitOfWork                styleLabelHaloUnitOfWork = null;


    private static class StyleHaloColorFormField extends ColorFormField {
        private final Property<StyleColor> styleColorProp;
        
        StyleHaloColorFormField(Property<StyleColor> styleColorProp) {
            this.styleColorProp = styleColorProp;
        }

        @Override
        protected void handleSelectionEvent( IFormFieldSite site, org.eclipse.swt.events.SelectionEvent e ) {
            site.fireEvent( this, IFormFieldListener.VALUE_CHANGE, null );
        }


        @Override
        protected void processLoadedValue( Object loadedValue ) {
            if (loadedValue instanceof StyleColor) {
                StyleColor styleColor = (StyleColor)loadedValue;
                RGB rgb = new RGB( styleColor.red.get(), styleColor.green.get(), styleColor.blue.get() );
                setCurrentValue( rgb );
            }
            else {
                setCurrentValue( null );
            }
        }


        @Override
        public void store() throws Exception {
            StyleColor styleColor = styleColorProp.get();
            RGB rgb = (RGB)currentValue;
            if (styleColor == null) {
                styleColor = styleColorProp.createValue( sc -> {
                    sc.red.set( rgb.red );
                    sc.green.set( rgb.green );
                    sc.blue.set( rgb.blue );
                    return sc;
                } );
            }
            else {
                styleColor.red.set( rgb.red );
                styleColor.green.set( rgb.green );
                styleColor.blue.set( rgb.blue );
            }
            this.site.setFieldValue( styleColor );
        }
    }


    public StyleLabelHaloUI( IAppContext context, IPanelSite panelSite, Context<IColorInfo> colorInfoInContext ) {
        this.context = context;
        this.panelSite = panelSite;
        this.colorInfoInContext = colorInfoInContext;
    }


    public void setModel( StyleLabel styleLabel ) {
        this.styleLabel = styleLabel;
    }


    @Override
    public Composite createContents( IFormPageSite site ) {
        Composite parent = site.getPageBody();
        parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 )
                .margins( panelSite.getLayoutPreference().getSpacing() / 2 ).create() );
        labelHaloRadiusFormField = new SpinnerFormField( 0, 128, 0.1, 10, 1 );
        site.newFormField( new PropertyAdapter( styleLabel.haloRadius ) ).label.put( "Label halo radius" ).field
                .put( labelHaloRadiusFormField ).tooltip.put(
                "The radius around the label to halo to improve readability" ).create();
        labelHaloFillFormField = new StyleHaloColorFormField(styleLabel.haloFill);
        site.newFormField( new PropertyAdapter( styleLabel.haloFill ) ).label.put( "Label halo fill" ).field
                .put( labelHaloFillFormField ).tooltip.put( "The halo fill color" ).create();

        site.addFieldListener( this );

        return site.getPageBody();
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == VALUE_CHANGE) {
            if (ev.getSource() == labelHaloFillFormField) {
                ColorInfo colorInfoImpl = new ColorInfo();
                EventManager.instance().subscribe( colorInfoImpl, e -> e.getSource() instanceof IColorInfo );
                colorInfoImpl.setFormField( labelHaloFillFormField );
                RGB rgb = (RGB)ev.getNewFieldValue();
                if (rgb != null) {
                    colorInfoImpl.setColor( rgb );
                }
                colorInfoInContext.set( colorInfoImpl );
                context.openPanel( panelSite.getPath(), ColorPanel.ID );
            }
        }
    }


    public void setUnitOfWork( UnitOfWork styleLabelHaloUnitOfWork ) {
        this.styleLabelHaloUnitOfWork = styleLabelHaloUnitOfWork;
    }


    @Override
    public void submitUI() {
        if (styleLabelHaloUnitOfWork != null && styleLabelHaloUnitOfWork.isOpen()) {
            styleLabelHaloUnitOfWork.commit();
        }
    }


    @Override
    public void resetUI() {
        if (styleLabelHaloUnitOfWork != null && styleLabelHaloUnitOfWork.isOpen()) {
            styleLabelHaloUnitOfWork.close();
        }
    }
}
