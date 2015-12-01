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

import org.eclipse.swt.widgets.Composite;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.p4.style.entities.StyleLabelLinePlacement;
import org.polymap.p4.style.ui.AbstractStylerFragmentUI;
import org.polymap.p4.util.PropertyAdapter;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.SpinnerFormField;
import org.polymap.rhei.form.IFormPageSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleLabelLinePlacementUI
        extends AbstractStylerFragmentUI
        implements IFormFieldListener {

    private final IAppContext       context;

    private final IPanelSite        panelSite;

    // http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#perpendicularoffset
    private SpinnerFormField        labelPerpendicularOffsetFormField;

    // GeoServer extension
    // http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#followline
    private CheckboxFormField       followLineFormField;

    // GeoServer extension
    // requires followLine set to true
    // http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#maxAngleDelta
    private SpinnerFormField        maxAngleDeltaFormField;

    // GeoServer extension
    // http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#repeat
    private SpinnerFormField        repeatFormField;

    // http://docs.geoserver.org/stable/en/user/styling/sld-reference/labeling.html#maxdisplacement
    private SpinnerFormField        maxDisplacementFormField;

    private StyleLabelLinePlacement styleLabelLinePlacement = null;


    public StyleLabelLinePlacementUI( IAppContext context, IPanelSite panelSite ) {
        this.context = context;
        this.panelSite = panelSite;
    }


    public void setModel( StyleLabelLinePlacement styleLabelLinePlacement ) {
        this.styleLabelLinePlacement = styleLabelLinePlacement;
    }


    @Override
    public Composite createContents( IFormPageSite site ) {
        Composite parent = site.getPageBody();
        parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 )
                .margins( panelSite.getLayoutPreference().getSpacing() / 2 ).create() );
        labelPerpendicularOffsetFormField = new SpinnerFormField( -128, 128, 0 );
        site.newFormField( new PropertyAdapter( styleLabelLinePlacement.perpendicularOffset ) ).label
                .put( "Label perpendicular offset" ).field.put( labelPerpendicularOffsetFormField ).tooltip.put(
                "Positive values puts the label above the line, negative values places the label below the line" )
                .create();
        followLineFormField = new CheckboxFormField();
        site.newFormField( new PropertyAdapter( styleLabelLinePlacement.followLine ) ).label.put( "Label follow line" ).field
                .put( followLineFormField ).tooltip.put( "If checked, the label is rotate to the angle of the line" )
                .create();
        maxAngleDeltaFormField = new SpinnerFormField( 0, 360, 30 );
        site.newFormField( new PropertyAdapter( styleLabelLinePlacement.maxAngleDelta ) ).label
                .put( "Label max angle delta" ).field.put( maxAngleDeltaFormField ).tooltip.put(
                "The angle a label should be rotated at maximum when following a line. "
                        + "This property applies only when follow line property is enabled." ).create();
        repeatFormField = new SpinnerFormField( 0, 128, 1 );
        site.newFormField( new PropertyAdapter( styleLabelLinePlacement.repeat ) ).label.put( "Label repeat" ).field
                .put( repeatFormField ).tooltip.put( "How often a label should be repeated at a line." ).create();
        maxDisplacementFormField = new SpinnerFormField( 0, 128, 1 );
        site.newFormField( new PropertyAdapter( styleLabelLinePlacement.maxDisplacement ) ).label
                .put( "Label max displacement" ).field.put( maxDisplacementFormField ).tooltip.put(
                "The allowed maximum displacement of the label from "
                        + "the actually calculated label position at the line"
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
