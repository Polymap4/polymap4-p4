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
package org.polymap.p4.style.ui.label;

import org.eclipse.swt.widgets.Composite;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.p4.style.entities.StyleCoord;
import org.polymap.p4.style.entities.StyleLabelPointPlacement;
import org.polymap.p4.style.ui.AbstractStylerFragmentUI;
import org.polymap.p4.util.PropertyAdapter;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.CoordFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.SpinnerFormField;
import org.polymap.rhei.form.IFormPageSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelPointPlacementUI
        extends AbstractStylerFragmentUI
        implements IFormFieldListener {

    private final IAppContext        context;

    private final IPanelSite         panelSite;

    // http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#anchorpoint
    private CoordFormField           labelAnchorFormField;

    // http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#displacement
    private CoordFormField           labelOffsetFormField;

    // http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#rotation
    private SpinnerFormField         labelRotationFormField;

    // http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#maxdisplacement
    private SpinnerFormField         labelMaxDisplacementFormField;

    private StyleLabelPointPlacement styleLabelPointPlacement = null;


    public StyleLabelPointPlacementUI( IAppContext context, IPanelSite panelSite ) {
        this.context = context;
        this.panelSite = panelSite;
    }


    public void setModel( StyleLabelPointPlacement styleLabelPointPlacement ) {
        this.styleLabelPointPlacement = styleLabelPointPlacement;
    }


    @Override
    public Composite createContents( IFormPageSite site ) {
        Composite parent = site.getPageBody();
        parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 )
                .margins( panelSite.getLayoutPreference().getSpacing() / 2 ).create() );
        labelAnchorFormField = new CoordFormField( new SpinnerFormField( 0, 1, 0.1, 0.0, 1 ), new SpinnerFormField( 0,
                1, 0.1, 0.5, 1 ) ) {

            protected boolean isExpectedFieldValueType() throws Exception {
                return site.getFieldValue() instanceof StyleCoord;
            }

            
            protected void handleExpectedFieldValueType() throws Exception {
                loadedValue = new Object [2];
                loadedValue[0] = ((StyleCoord) site.getFieldValue()).x.get();
                loadedValue[1] = ((StyleCoord) site.getFieldValue()).y.get();
                field1.load();
                field2.load();
            }
            
            @Override
            public void store() throws Exception {
                field1.store();
                field2.store();
                StyleCoord anchor = styleLabelPointPlacement.anchor.get();
                if(anchor == null) {
                    anchor = styleLabelPointPlacement.anchor.createValue( null );
                }
                anchor.x.set( (Double) super.newValue1 );
                anchor.y.set( (Double) super.newValue2 );
                site.setFieldValue(anchor);
            }
        };
        site.newFormField( new PropertyAdapter( styleLabelPointPlacement.anchor ) ).label.put( "Label anchor" ).field
                .put( labelAnchorFormField ).tooltip.put( "" ).create();
        labelOffsetFormField = new CoordFormField( new SpinnerFormField( -128, 128, 0.1, 0, 1 ), new SpinnerFormField(
                -128,
                128, 0.1, 0, 1 ) ) {

            protected boolean isExpectedFieldValueType() throws Exception {
                return site.getFieldValue() instanceof StyleCoord;
            }

            
            protected void handleExpectedFieldValueType() throws Exception {
                loadedValue = new Object [2];
                loadedValue[0] = ((StyleCoord) site.getFieldValue()).x.get();
                loadedValue[1] = ((StyleCoord) site.getFieldValue()).y.get();
                field1.load();
                field2.load();
            }
            
            @Override
            public void store() throws Exception {
                field1.store();
                field2.store();
                StyleCoord offset = styleLabelPointPlacement.offset.get();
                if(offset == null) {
                    offset = styleLabelPointPlacement.offset.createValue( null );
                }
                offset.x.set( (Double) super.newValue1 );
                offset.y.set( (Double) super.newValue2 );
                site.setFieldValue(offset);
            }
        };

        site.newFormField( new PropertyAdapter( styleLabelPointPlacement.offset ) ).label.put( "Label offset" ).field
                .put( labelOffsetFormField ).tooltip.put( "" ).create();
        labelRotationFormField = new SpinnerFormField( -360, 360, 0.1, 0, 1 );
        site.newFormField( new PropertyAdapter( styleLabelPointPlacement.rotation ) ).label.put( "Label rotation" ).field
                .put( labelRotationFormField ).tooltip.put( "" ).create();
        labelMaxDisplacementFormField = new SpinnerFormField( 0, 30, 0.1, 0, 1 );
        site.newFormField( new PropertyAdapter( styleLabelPointPlacement.maxDisplacement ) ).label
                .put( "Label max displacement" ).field.put( labelMaxDisplacementFormField ).tooltip.put(
                "The allowed maximum displacement of the label from "
                        + "the actually calculated label position at the point"
                        + " when resolving conflicts with other labels " ).create();

        return site.getPageBody();
    }


    @Override
    public void submitUI() {
        // TODO Auto-generated method stub

    }


    @Override
    public void resetUI() {
        // TODO Auto-generated method stub

    }


    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.getEventCode() == VALUE_CHANGE) {
        }
    }
}
