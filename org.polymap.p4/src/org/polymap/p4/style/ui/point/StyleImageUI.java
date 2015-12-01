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
package org.polymap.p4.style.ui.point;

import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.p4.style.entities.StyleImage;
import org.polymap.p4.style.icon.IImageInfo;
import org.polymap.p4.style.icon.IconLibraryInitializer;
import org.polymap.p4.style.icon.ImageHelper;
import org.polymap.p4.style.icon.ImageInfo;
import org.polymap.p4.style.icon.ImagePanel;
import org.polymap.p4.style.ui.AbstractStylerFragmentUI;
import org.polymap.p4.util.PropertyAdapter;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IconFormField;
import org.polymap.rhei.field.ImageDescription;
import org.polymap.rhei.form.IFormPageSite;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class StyleImageUI
        extends AbstractStylerFragmentUI
        implements IFormFieldListener {

    private final IAppContext         context;

    private final IPanelSite          panelSite;

    private String                    DEFAULT_ICON_PATH = "sld_wellknown/circle.svg";

    private IconFormField             iconFormField;

    private final Context<IImageInfo> imageInfoInContext;

    private StyleImage                styleImage        = null;


    public StyleImageUI( IAppContext context, IPanelSite panelSite, Context<IImageInfo> imageInfoInContext ) {
        this.context = context;
        this.panelSite = panelSite;
        this.imageInfoInContext = imageInfoInContext;

        IconLibraryInitializer iconLibraryInitializer = new IconLibraryInitializer();

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setImageLibrary( iconLibraryInitializer.getImageLibrary() );
        imageInfo.setPathToImageDescription( iconLibraryInitializer.getPathToImageDescription() );
        imageInfoInContext.set( imageInfo );

        EventManager.instance().subscribe( imageInfo, ev -> ev.getSource() instanceof IImageInfo );
    }


    public void setModel( StyleImage styleImage ) {
        this.styleImage = styleImage;
    }


    @Override
    public Composite createContents( IFormPageSite site ) {
        Composite parent = site.getPageBody();
        parent.setLayout( ColumnLayoutFactory.defaults().spacing( 5 )
                .margins( panelSite.getLayoutPreference().getSpacing() / 2 ).create() );
        iconFormField = new IconFormField();
        if (imageInfoInContext.get().getImageDescription() != null) {
            iconFormField.setValue( imageInfoInContext.get().getImageDescription() );
        }
        else {
            ImageDescription imageDescription = new ImageHelper().createImageDescription( DEFAULT_ICON_PATH );
            iconFormField.setValue( imageDescription );
        }
        site.newFormField( new PropertyAdapter( styleImage.url ) ).label.put( "Marker icon" ).field.put( iconFormField ).tooltip
                .put( "" ).create();
        site.addFieldListener( this );
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
            if (ev.getSource() == iconFormField) {
                imageInfoInContext.get().setFormField( iconFormField );
                imageInfoInContext.get().setImageDescription( ev.getNewFieldValue() );
                final ServerPushSession pushSession = new ServerPushSession();
                pushSession.start();
                context.openPanel( panelSite.getPath(), ImagePanel.ID );
                pushSession.stop();
            }
        }
    }

}
