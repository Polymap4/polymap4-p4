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
package org.polymap.p4.style.ui;

import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.Callback;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.entities.StyleIdent;
import org.polymap.p4.util.PropertyAdapter;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.NotEmptyValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormPageSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleIdentUI
        extends AbstractStylerFragmentUI {

    private final IPanelSite  panelSite;

    private StyleIdent        styleIdent;

    private PicklistFormField picklistFormField;


    public StyleIdentUI( IAppContext context, IPanelSite panelSite ) {
        this.panelSite = panelSite;
    }


    public void setModel( StyleIdent styleIdent ) {
        this.styleIdent = styleIdent;
    }


    @Override
    public Composite createContents( IFormPageSite site ) {
        Composite parent = site.getPageBody();
        parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 )
                .margins( panelSite.getLayoutPreference().getSpacing() / 2 ).create() );
        site.newFormField( new PropertyAdapter( styleIdent.name ) ).label.put( "Style name" ).field
                .put( new StringFormField() ).tooltip.put( "Identifying style name" ).validator.put(
                new NotEmptyValidator<String,String>() ).create();
        site.newFormField( new PropertyAdapter( styleIdent.title ) ).label.put( "Style title" ).field
                .put( new StringFormField() ).tooltip.put( "Descriptive title of style" ).create();

        List<String> orderedLabel = FeatureType.getOrdered().stream().map( value -> value.getLabel() )
                .collect( Collectors.toList() );
        Comparator<String> comparator = ( String ft1Label, String ft2Label ) -> Integer.valueOf(
                orderedLabel.indexOf( ft1Label ) ).compareTo( orderedLabel.indexOf( ft2Label ) );
        SortedMap<String,Object> orderFeatureTypes = new TreeMap<String,Object>( comparator );
        FeatureType.getOrdered().stream().forEach( value -> orderFeatureTypes.put( value.getLabel(), value ) );
        picklistFormField = new PicklistFormField( ( ) -> orderFeatureTypes ) {

            protected Object getValue() {
                Object value = super.getValue();
                if (value == null) {
                    value = FeatureType.POINT;
                }
                return value;
            }
        };
        site.newFormField( new PropertyAdapter( styleIdent.description ) ).label.put( "Description" ).field
                .put( new TextFormField() ).tooltip.put( "Full description of style" ).create();
        site.newFormField( new PropertyAdapter( styleIdent.featureType ) ).label.put( "Feature type" ).field
                .put( picklistFormField ).tooltip.put( "The symbolizer the style should be applied to " ).create();
        return site.getPageBody();
    }


    public void addCallback( Callback<FeatureType> callback ) {
        picklistFormField.addModifyListener( event -> {
            if (callback != null) {
                try {
                    if (styleIdent != null) {
                        picklistFormField.store();
                        try {
                            callback.handle( styleIdent.featureType.get() );
                        }
                        catch (Exception e) {

                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } );
    }


    @Override
    public void submitUI() {
        // TODO Auto-generated method stub

    }


    @Override
    public void resetUI() {
        // TODO Auto-generated method stub

    }
}
