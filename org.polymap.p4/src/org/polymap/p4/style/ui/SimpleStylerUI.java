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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.Callback;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.style.SimpleStyler;
import org.polymap.p4.style.color.IColorInfo;
import org.polymap.p4.style.entities.FeatureType;
import org.polymap.p4.style.font.IFontInfo;
import org.polymap.p4.style.icon.IImageInfo;
import org.polymap.p4.style.label.IStyleLabelInfo;
import org.polymap.p4.style.point.IStylePointInfo;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.md.MdTabFolder;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class SimpleStylerUI
        extends AbstractStylerUI {

    private IPanelSite                   site;

    private final UnitOfWork             newSimpleStylerUnitOfWork;

    private final StylerUIFactory        stylerUIFactory;

    private final StylerContainerFactory stylerContainerFactory;

    private final StylerPageFactory      stylerPageFactory;

    private String                       lastOpenTab  = null;

    private SimpleStyler                 simpleStyler = null;

    private CurrentFeatureTypeProvider   currentFeatureTypeProvider;


    public SimpleStylerUI( IAppContext context, IPanelSite panelSite, UnitOfWork newSimpleStylerUnitOfWork,
            Context<IImageInfo> imageInfoInContext, Context<IColorInfo> colorInfoInContext,
            Context<IFontInfo> fontInfoInContext, Context<IStyleLabelInfo> styleLabelInfo,
            Context<IStylePointInfo> stylePointInfo ) {
        this.site = panelSite;
        this.newSimpleStylerUnitOfWork = newSimpleStylerUnitOfWork;
        stylerUIFactory = new StylerUIFactory( context, panelSite, newSimpleStylerUnitOfWork, imageInfoInContext,
                colorInfoInContext, fontInfoInContext, styleLabelInfo, stylePointInfo );
        currentFeatureTypeProvider = new CurrentFeatureTypeProvider();
        stylerPageFactory = new StylerPageFactory( stylerUIFactory, currentFeatureTypeProvider,
                newSimpleStylerUnitOfWork );
        stylerContainerFactory = new StylerContainerFactory( simpleStyler, (MdToolkit)site.toolkit(),
                stylerPageFactory, currentFeatureTypeProvider, stylerUIFactory );
    }


    private void updateUI( FeatureType ft ) {
        stylerContainerFactory.createStyleLabelUI( simpleStyler, ft );
        stylerUIFactory.updateGeometryUI( newSimpleStylerUnitOfWork, ft );
    }


    public void setModel( SimpleStyler simpleStyler ) {
        this.simpleStyler = simpleStyler;
    }


    public Composite createContents( Composite parent ) {
        stylerContainerFactory.createStyleIdentUI( simpleStyler.styleIdent.get() );
        stylerContainerFactory.createStyleLabelUI( simpleStyler );
        stylerContainerFactory
                .getOrCreateGeometryContainer( simpleStyler, newSimpleStylerUnitOfWork, FeatureType.POINT );
        return internalCreateContents( parent );
    }


    private Composite internalCreateContents( Composite parent ) {
        MdToolkit tk = (MdToolkit)site.toolkit();

        stylerContainerFactory.createStyleLabelUI( simpleStyler, currentFeatureTypeProvider.get() );
        updateUI( currentFeatureTypeProvider.get() );

        List<String> tabItems = new ArrayList<String>();
        tabItems.add( StylerUIConstants.IDENT_STR );
        tabItems.add( StylerUIConstants.LABEL_STR );
        tabItems.add( StylerUIConstants.STYLE_STR );
        Map<String,Function<Composite,Composite>> tabContents = new HashMap<String,Function<Composite,Composite>>();
        for (String tabItem : tabItems) {
            tabContents.put( tabItem, stylerContainerFactory.getStyleContainers().get( tabItem ) );
        }
        MdTabFolder tabFolder = tk.createTabFolder( parent, tabItems, tabContents );
        tabFolder.openTab( getLastOpenTab() );
        tabFolder.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                setLastOpenTab( ((Button)e.widget).getText() );
            }
        } );
        tabFolder.replaceTabContent( StylerUIConstants.STYLE_STR, stylerContainerFactory.getOrCreateGeometryContainer(
                simpleStyler, newSimpleStylerUnitOfWork, FeatureType.POINT ) );

        addIdentCallback( tabFolder );

        FormDataFactory.on( tabFolder ).left( 0 ).right( 100 );

        return tabFolder;
    }


    private void addIdentCallback( MdTabFolder tabFolder ) {
        Callback<FeatureType> identCallback = new Callback<FeatureType>() {

            @Override
            public void handle( FeatureType ft ) {
                if (ft.name().equals( currentFeatureTypeProvider.get().name() )) {
                    // already handled
                    return;
                }
                currentFeatureTypeProvider.set( ft );
                if (ft == FeatureType.TEXT) {
                    tabFolder.setTabVisibility( StylerUIConstants.STYLE_STR, false );
                }
                else {
                    tabFolder.setTabVisibility( StylerUIConstants.STYLE_STR, true );
                    tabFolder.replaceTabContent( StylerUIConstants.STYLE_STR, stylerContainerFactory
                            .getOrCreateGeometryContainer( simpleStyler, newSimpleStylerUnitOfWork, ft ) );
                }
                updateUI( ft );
                tabFolder.replaceTabContent( StylerUIConstants.LABEL_STR,
                        stylerContainerFactory.getStyleContainers().get( StylerUIConstants.LABEL_STR ) );
            }
        };
        stylerUIFactory.getIdentUI().addCallback( identCallback );
    }


    public String getLastOpenTab() {
        return this.lastOpenTab;
    }


    public void setLastOpenTab( String tabname ) {
        this.lastOpenTab = tabname;
    }


    @Override
    public void resetUI() {
        // TODO Auto-generated method stub

    }


    @Override
    public void submitUI() {
        // TODO Auto-generated method stub

    }
}
