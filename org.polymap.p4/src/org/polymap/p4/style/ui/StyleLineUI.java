/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
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
package org.polymap.p4.style.ui;

import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Composite;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.style.color.IColorInfo;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.LineCapType;
import org.polymap.p4.style.entities.StyleLine;
import org.polymap.p4.style.icon.IImageInfo;
import org.polymap.p4.style.icon.ShapeFigureLibraryInitializer;
import org.polymap.p4.style.point.IStylePointInfo;
import org.polymap.p4.style.ui.point.StylePointUI;
import org.polymap.p4.util.PropertyAdapter;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.ColorFormField;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.SpinnerFormField;
import org.polymap.rhei.form.IFormPageSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLineUI
        extends AbstractStylerFragmentUI {

    private final IAppContext              context;

    private final IPanelSite               panelSite;

    private SpinnerFormField               lineWidthField;

    private ColorFormField                 colorFormField;

    private SpinnerFormField               lineTransparencyField;

    private final Context<IImageInfo>      imageInfoInContext;

    private final Context<IColorInfo>      colorInfoInContext;

    private final Context<IStylePointInfo> stylePointInfoInContext;

    private PicklistFormField              lineCapFormField;

    private Supplier<StyleLine>            styleLineSupplier   = null;

    private UnitOfWork                     styleLineUnitOfWork = null;

    private StyleLine                      styleLine           = null;

    private boolean                        border              = false;


    public StyleLineUI( IAppContext context, IPanelSite panelSite, Context<IImageInfo> imageInfoInContext,
            Context<IColorInfo> colorInfoInContext, Context<IStylePointInfo> stylePointInfoInContext ) {
        this.context = context;
        this.panelSite = panelSite;
        this.imageInfoInContext = imageInfoInContext;
        this.colorInfoInContext = colorInfoInContext;
        this.stylePointInfoInContext = stylePointInfoInContext;
    }


    public void setModelFunction( Supplier<StyleLine> styleLineSupplier ) {
        this.styleLineSupplier = styleLineSupplier;
        this.styleLine = null;
    }


    public void setUnitOfWork( UnitOfWork styleLineUnitOfWork ) {
        this.styleLineUnitOfWork = styleLineUnitOfWork;
    }


    @Override
    public Composite createContents( IFormPageSite site ) {
        Composite parent = site.getPageBody();
        parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 )
                .margins( panelSite.getLayoutPreference().getSpacing() / 2 ).create() );
        if (styleLine == null) {
            styleLine = styleLineSupplier.get();
        }
        lineWidthField = new SpinnerFormField( 1, 128, 12 );
        String extraLabel = "";
        if (border) {
            extraLabel = "border ";
        }
        site.newFormField( new PropertyAdapter( styleLine.lineWidth ) ).label.put( "Line " + extraLabel + "width" ).field
                .put( lineWidthField ).tooltip.put( "" ).create();
        colorFormField = new ColorFormField();
        if (colorInfoInContext.get() != null && colorInfoInContext.get().getColor() != null) {
            colorFormField.setValue( colorInfoInContext.get().getColor() );
        }
        if (styleLine.lineSymbol.get() != null) {
            ShapeFigureLibraryInitializer shapeFigureLibraryInitializer = new ShapeFigureLibraryInitializer();
            stylePointInfoInContext.get().setFigureLibraryInitializer( shapeFigureLibraryInitializer );
            StylePointUI ui = new StylePointUI( context, panelSite, stylePointInfoInContext );
            ui.setUnitOfWork( styleLineUnitOfWork.newUnitOfWork() );
            ui.setModelFunction( ( ) -> styleLine.lineSymbol.get() );
            ui.createContents( site );
        }
        lineTransparencyField = new SpinnerFormField( 0, 1, 0.1, 1, 1 );
        site.newFormField( new PropertyAdapter( styleLine.lineTransparency ) ).label.put( "Line " + extraLabel
                + "transparency" ).field.put( lineTransparencyField ).tooltip.put( "" ).create();

        List<String> orderedLabel = FeatureType.getOrdered().stream().map( value -> value.getLabel() )
                .collect( Collectors.toList() );
        Comparator<String> comparator = ( String ft1Label, String ft2Label ) -> Integer.valueOf(
                orderedLabel.indexOf( ft1Label ) ).compareTo( orderedLabel.indexOf( ft2Label ) );
        SortedMap<String,Object> orderFeatureTypes = new TreeMap<String,Object>( comparator );
        LineCapType.getOrdered().stream().forEach( value -> orderFeatureTypes.put( value.getLabel(), value ) );
        lineCapFormField = new PicklistFormField( ( ) -> orderFeatureTypes );
        if (styleLine.lineCap.get() == null) {
            styleLine.lineCap.set( LineCapType.BUTT );
        }
        site.newFormField( new PropertyAdapter( styleLine.lineCap ) ).label.put( "Line " + extraLabel + "cap" ).field
                .put( lineCapFormField ).tooltip.put( "" ).create();
        if (extraLabel.isEmpty()) {
            StyleLineUI borderUI = new StyleLineUI( context, panelSite, imageInfoInContext, colorInfoInContext, stylePointInfoInContext );
            borderUI.setBorder( true );
            borderUI.setUnitOfWork( styleLineUnitOfWork.newUnitOfWork() );
            borderUI.setModelFunction( ( ) -> styleLine.border.get() == null ? styleLine.border.createValue( null )
                    : styleLine.border.get() );
            borderUI.createContents( site );
        }
        return site.getPageBody();
    }


    private void setBorder( boolean b ) {
        this.border = b;
    }


    @Override
    public void submitUI() {
        if (styleLineUnitOfWork != null && styleLineUnitOfWork.isOpen()) {
            styleLineUnitOfWork.commit();
        }
    }


    @Override
    public void resetUI() {
        if (styleLineUnitOfWork != null && styleLineUnitOfWork.isOpen()) {
            styleLineUnitOfWork.close();
        }
    }
}
