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
package org.polymap.p4.style.ui.label;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.style.color.IColorInfo;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.StyleLabel;
import org.polymap.p4.style.font.FontInfo;
import org.polymap.p4.style.font.FontPanel;
import org.polymap.p4.style.font.IFontInfo;
import org.polymap.p4.style.label.IStyleLabelInfo;
import org.polymap.p4.style.label.StyleLabelHaloPanel;
import org.polymap.p4.style.label.StyleLabelInfo;
import org.polymap.p4.style.label.StyleLabelLinePlacementPanel;
import org.polymap.p4.style.label.StyleLabelPointPlacementPanel;
import org.polymap.p4.style.ui.AbstractStylerFragmentUI;
import org.polymap.p4.util.PropertyAdapter;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.AbstractDelegatingFormField;
import org.polymap.rhei.field.FontFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.SpinnerFormField;
import org.polymap.rhei.form.IFormPageSite;

import com.google.common.collect.Sets;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelUI
        extends AbstractStylerFragmentUI
        implements IFormFieldListener {

    private final IAppContext                       context;

    private final IPanelSite                        panelSite;

    private PicklistFormField                       labelTextField;

    private FontFormField                           fontFormField;

    private AbstractDelegatingFormField<StyleLabel> pointPlacementFormField;

    private AbstractDelegatingFormField<StyleLabel> linePlacementFormField;

    private AbstractDelegatingFormField<StyleLabel> haloFormField;

    // GeoServer extension
    // http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#autowrap
    private SpinnerFormField                        autoWrapFormField;

    private final Context<IFontInfo>                fontInfoInContext;

    private final Context<IColorInfo>               colorInfoInContext;
    
    private final Context<IStyleLabelInfo>          styleLabelInfo;

    private Supplier<StyleLabel>                    styleLabelSupplier   = null;

    private UnitOfWork                              styleLabelUnitOfWork = null;

    private StyleLabel                              styleLabel           = null;

    private FeatureType                             featureType          = null;

    private List<IFormField>                        formFields;


    private static class StyleLabelUIDelegatingFormField
            extends AbstractDelegatingFormField<StyleLabel> {

        private StyleLabel styleLabel;


        StyleLabelUIDelegatingFormField( StyleLabel styleLabel ) {
            this.styleLabel = styleLabel;
        }


        protected void handleSelectionEvent( IFormFieldSite site, org.eclipse.swt.events.SelectionEvent e ) {
            site.fireEvent( this, IFormFieldListener.VALUE_CHANGE, null );
        }


        @Override
        public IFormField setValue( Object value ) {
            if (value instanceof StyleLabel) {
                setCurrentValue( (StyleLabel)styleLabel );
            }
            return this;
        }


        @Override
        protected void processLoadedValue( Object loadedValue ) {
            if (loadedValue instanceof StyleLabel) {
                setCurrentValue( (StyleLabel)styleLabel );
            }
        }
    }


    public StyleLabelUI( IAppContext context, IPanelSite panelSite, Context<IFontInfo> fontInfoInContext,
            Context<IColorInfo> colorInfoInContext, Context<IStyleLabelInfo> styleLabelInfo ) {
        this.context = context;
        this.panelSite = panelSite;
        this.fontInfoInContext = fontInfoInContext;
        this.colorInfoInContext = colorInfoInContext;
        this.styleLabelInfo = styleLabelInfo;

        FontInfo fontInfo = new FontInfo();
        fontInfoInContext.set( fontInfo );

        EventManager.instance().subscribe( fontInfo, ev -> ev.getSource() instanceof IFontInfo );
    }


    public void setModelFunction( Supplier<StyleLabel> styleLabelSupplier ) {
        this.styleLabelSupplier = styleLabelSupplier;
        this.styleLabel = null;
    }


    public void setUnitOfWork( UnitOfWork styleLabelUnitOfWork ) {
        this.styleLabelUnitOfWork = styleLabelUnitOfWork;
    }


    public void setFeatureType( FeatureType featureType ) {
        this.featureType = featureType;
    }


    @Override
    public Composite createContents( IFormPageSite site ) {
        Composite parent = site.getPageBody();
        parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 )
                .margins( panelSite.getLayoutPreference().getSpacing() / 2 ).create() );
        HashSet<String> labels = Sets.newHashSet( "", "<Placeholder>" );
        labelTextField = new PicklistFormField( labels );
        if (styleLabel == null) {
            styleLabel = styleLabelSupplier.get();
        }
        site.newFormField( new PropertyAdapter( styleLabel.labelText ) ).label.put( "Label text" ).field
                .put( labelTextField ).tooltip.put(
                "The actual label value can be assigned later when "
                        + "combining this style with a layer resp. a feature type of that layer. "
                        + "If you don't want to use labels, just leave this field blank." ).create();
        fontFormField = new FontFormField();
        site.newFormField( new PropertyAdapter( styleLabel.labelFont ) ).label.put( "Label font" ).field
                .put( fontFormField ).tooltip.put( "Font style to be applied to this style label" ).create();
        if (featureType == FeatureType.LINE_STRING) {
            linePlacementFormField = new StyleLabelUIDelegatingFormField( styleLabel );
            site.newFormField( new PropertyAdapter( styleLabel.linePlacement ) ).label.put( "Label line placement" ).field
                    .put( linePlacementFormField ).tooltip.put( "Label placement in relation to line symbolizer" )
                    .create();
        }
        else if (featureType != FeatureType.TEXT) {
            pointPlacementFormField = new StyleLabelUIDelegatingFormField( styleLabel );
            site.newFormField( new PropertyAdapter( styleLabel.pointPlacement ) ).label.put( "Label point placement" ).field
                    .put( pointPlacementFormField ).tooltip.put( "Label placement in relation to point symbolizer" )
                    .create();
        }
        haloFormField = new StyleLabelUIDelegatingFormField( styleLabel );
        site.newFormField( new PropertyAdapter( styleLabel.haloRadius ) ).label.put( "Label halo" ).field
                .put( haloFormField ).tooltip.put( "Halo effect configuration for label" ).create();
        autoWrapFormField = new SpinnerFormField( 0, 128, 60 );
        site.newFormField( new PropertyAdapter( styleLabel.autoWrap ) ).label.put( "Label auto wrap" ).field
                .put( autoWrapFormField ).tooltip.put( "When to add a line break in label text" ).create();

        collectFormFields();

        updateEnablementOfFormFields( styleLabel.labelText.get() );

        site.addFieldListener( this );

        return site.getPageBody();
    }


    private void collectFormFields() {
        formFields = new ArrayList<IFormField>();
        formFields.add( fontFormField );
        if (featureType == FeatureType.LINE_STRING) {
            formFields.add( linePlacementFormField );
        }
        else if (featureType != FeatureType.TEXT) {
            formFields.add( pointPlacementFormField );
        }
        formFields.add( haloFormField );
        formFields.add( autoWrapFormField );
    }


    private void updateEnablementOfFormFields( String labelText ) {
        boolean enabled = !StringUtils.isEmpty( labelText );
        formFields.stream().filter( formField -> formField != null )
                .forEach( formField -> formField.setEnabled( enabled ) );
    }


    @Override
    public void submitUI() {
        if (styleLabelUnitOfWork != null && styleLabelUnitOfWork.isOpen()) {
            styleLabelUnitOfWork.commit();
        }
    }


    @Override
    public void resetUI() {
        if (styleLabelUnitOfWork != null && styleLabelUnitOfWork.isOpen()) {
            styleLabelUnitOfWork.close();
        }
    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == VALUE_CHANGE) {
            if (ev.getSource() == labelTextField) {
                updateEnablementOfFormFields( ev.getNewFieldValue() );
            }
            else if (ev.getSource() == fontFormField) {
                fontInfoInContext.get().setFormField( fontFormField );
                Object[] array = (Object[])ev.getNewFieldValue();
                if (array != null && array.length == 2) {
                    fontInfoInContext.get().setFontData( new FontData[] { (FontData)array[0] } );
                    fontInfoInContext.get().setColor( (RGB)array[1] );
                }
                context.openPanel( panelSite.getPath(), FontPanel.ID );
            }
            else if (ev.getSource() == pointPlacementFormField) {
                StyleLabelInfo impl = new StyleLabelInfo( pointPlacementFormField,
                        styleLabelUnitOfWork.newUnitOfWork(), styleLabel );
                styleLabelInfo.set( impl );
                context.openPanel( panelSite.getPath(), StyleLabelPointPlacementPanel.ID );
            }
            else if (ev.getSource() == linePlacementFormField) {
                StyleLabelInfo impl = new StyleLabelInfo( linePlacementFormField, styleLabelUnitOfWork.newUnitOfWork(),
                        styleLabel );
                styleLabelInfo.set( impl );
                context.openPanel( panelSite.getPath(), StyleLabelLinePlacementPanel.ID );
            }
            else if (ev.getSource() == haloFormField) {
                StyleLabelInfo impl = new StyleLabelInfo( haloFormField, styleLabelUnitOfWork.newUnitOfWork(),
                        styleLabel );
                styleLabelInfo.set( impl );
                context.openPanel( panelSite.getPath(), StyleLabelHaloPanel.ID );
            }
        }
    }
}
