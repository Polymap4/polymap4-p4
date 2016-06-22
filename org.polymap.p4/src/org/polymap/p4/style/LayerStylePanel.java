/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.style;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.eclipse.ui.forms.widgets.ExpandableComposite.TREE_NODE;
import static org.polymap.core.runtime.UIThreadExecutor.async;
import static org.polymap.core.runtime.event.TypeEventFilter.ifType;
import static org.polymap.core.ui.FormDataFactory.on;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.geotools.data.FeatureStore;
import org.opengis.feature.type.FeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.DefaultStyle;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StyleComposite;
import org.polymap.core.style.model.StyleGroup;
import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.ui.StylePropertyField;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.style.ui.UIOrderComparator;
import org.polymap.core.ui.ColumnDataFactory;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.toolkit.ActionItem;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.ItemContainer;
import org.polymap.rhei.batik.toolkit.Snackbar.Appearance;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.CheckboxActionProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2;

import org.polymap.model2.Property;
import org.polymap.model2.runtime.PropertyInfo;
import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.layer.LayerInfoPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @author Steffen Stundzig
 */
public class LayerStylePanel
        extends P4Panel {

    private static Log log = LogFactory.getLog( LayerStylePanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "layerStyle" );
    
    public static final String          TOOLBAR = LayerStylePanel.class.getSimpleName();
    
    private FeatureStyle                featureStyle;

    private MdListViewer                list;

    private MdToolbar2                  toolbar;
    
    private IPanelSection               editorSection;

    private Button                      fab;

    private final static IMessages      i18nStyle = Messages.forPrefix( "Field" );

    @Mandatory
    @Scope( P4Plugin.StyleScope )
    protected Context<StyleEditorInput> styleEditorInput;

    private List<StylePropertyField> fields = Lists.newArrayList();

    @Override
    public boolean beforeInit() {
        IPanel parent = getContext().getPanel( getSite().getPath().removeLast( 1 ) );
        if (parent instanceof LayerInfoPanel) {
            site().title.set( "" );
            site().tooltip.set( "Edit styling" );
            site().icon.set( P4Plugin.images().svgImage( "palette.svg", P4Plugin.HEADER_ICON_CONFIG ) );
            return true;
        }
        return false;
    }


    @Override
    public void init() {
        super.init();
        try {
            featureStyle = P4Plugin.styleRepo().featureStyle( styleEditorInput.get().styleIdentifier() )
                    .orElseThrow( () -> new IllegalStateException( "Layer has no style.") );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "Unable to get style of layer.", e );
        }
    }


    @Override
    public void createContents( Composite parent ) {
        site().title.set( "Styling" );  // + ": " + layer.get().label.get() );
        ContributionManager.instance().contributeTo( this, this );
        
        // toolbar
        toolbar = tk().createToolbar( parent, SWT.TOP );
        new AddTextItem( toolbar );
        new AddPointItem( toolbar );
        new AddPolygonItem( toolbar );
        new AddLineItem( toolbar );
        ContributionManager.instance().contributeTo( toolbar, this, TOOLBAR );
        
        // style list
        list = tk().createListViewer( parent, SWT.SINGLE, SWT.FULL_SELECTION );
        list.setContentProvider( new FeatureStyleContentProvider() );
        list.iconProvider.set( new FeatureStyleLabelProvider( tk() ) );
        list.firstLineLabelProvider.set( new FeatureStyleLabelProvider( tk() ) );
        list.secondLineLabelProvider.set( new FeatureStyleDescriptionProvider( tk() ) );
        list.secondSecondaryActionProvider.set( new ActiveActionProvider() );
        list.firstSecondaryActionProvider.set( new RemoveActionProvider() );
        list.setComparer( new StyleIdentityComparer() );
        list.addSelectionChangedListener( ev -> {
            Optional<?> elm = org.polymap.core.ui.SelectionAdapter.on( ev.getSelection() ).first();
            if (!elm.isPresent()) {
                editorSection.setTitle( "" );
                UIUtils.disposeChildren( editorSection.getBody() );
            }
            else if (elm.get() instanceof StyleGroup) {
                // ...
            }
            else if (elm.get() instanceof Style) {
                createStyleEditor( editorSection.getBody(), (Style)elm.get() );   
            }
        });
        list.setInput( featureStyle );
        list.expandAll();
        if (!featureStyle.members().isEmpty()) {
            async( () -> list.setSelection( new StructuredSelection( featureStyle.members().iterator().next() ) ) );
        }
        
        //
        editorSection = tk().createPanelSection( parent, ""/*, SWT.BORDER*/ );
        
        // fab
        fab = tk().createFab();
        fab.setVisible( false );
        fab.setToolTipText( "Save changes" );
        fab.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent ev ) {
                featureStyle.store();
                tk().createSnackbar( Appearance.FadeIn, "Saved" );
                fab.setEnabled( false );
                //fab.setVisible( false );
                
                ILayer layer = featureSelection.get().layer();
                //layer.userSettings.get().visible.set( false );
                //layer.userSettings.get().visible.set( true );
                
                layer.styleIdentifier.set( layer.styleIdentifier.get() );
            }
        });
        
        // listen to StylePropertyChange
        EventManager.instance().subscribe( this, ifType( StylePropertyChange.class, 
                ev -> ev.getSource() == featureStyle ) );
        
        // layout
        parent.setLayout( FormLayoutFactory.defaults().margins( 0, 8 ).spacing( 8 ).create() );
        on( toolbar.getControl() ).left( 0, 3 ).right( 100, -3 ).top( 0 );
        on( list.getControl() ).fill().top( toolbar.getControl() ).bottom( 23 );
        on( editorSection.getControl() ).fill().top( list.getControl() );
    }
    
    
    @EventHandler( display=true, delay=100 )
    protected void featureStyleCanged( List<StylePropertyChange> evs ) {
        if (fab != null && !fab.isDisposed()) {
            boolean enabled = fields.stream().allMatch( field -> field.isValid() );
            fab.setEnabled( enabled );
            fab.setVisible( enabled );
        }
        else {
            EventManager.instance().unsubscribe( this );
        }
    }
    
    protected void createStyleEditor( Composite parent, Style style ) {
        editorSection.setTitle( defaultString( style.title.get(), "Style settings" ) );
        UIUtils.disposeChildren( parent );
        
        parent.setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).margins( 0, 0 ).spacing( 8 ).create() );
        
        //Color bg = parent.getBackground();
        Color fg = parent.getForeground();
        java.awt.Color brighter = new java.awt.Color( fg.getRed(), fg.getGreen(), fg.getBlue() )
                .brighter().brighter();

        Composite headLine = tk().createComposite( parent, SWT.NONE );
        headLine.setLayout( FormLayoutFactory.defaults().margins( 0, 0 ).spacing( 5 ).create() );
        
        // title
        Text title = new Text( headLine, SWT.BORDER );
        //title.setBackground( bg );
        title.setText( style.title.get() );
        title.addModifyListener( new ModifyListener() {
            @Override
            public void modifyText( ModifyEvent ev ) {
                // XXX sanitize user input string (?)
                style.title.set( title.getText() );
                list.update( style, null );
                editorSection.setTitle( style.title.get() );
            }
        });
        title.setToolTipText( i18nStyle.get( "styleNameTooltip" ) );
        
        // description
        Text descr = new Text( headLine, SWT.BORDER );
        //descr.setBackground( bg );
        descr.setForeground( UIUtils.getColor( brighter.getRed(), brighter.getGreen(), brighter.getBlue() ) );
        descr.setText( style.description.get() );
        descr.addModifyListener( new ModifyListener() {
            @Override
            public void modifyText( ModifyEvent ev ) {
                // XXX sanitize user input string (?)
                style.description.set( descr.getText() );
                list.update( style, null );
            }
        } );
        descr.setToolTipText( i18nStyle.get("styleDescriptionTooltip") );

        on( title ).left(0).right( 30 );
        on( descr ).left( title, 3 ).right( 100 );

        fields.clear();
        java.awt.Color sectionBackgroundColor = new java.awt.Color( 235,  235, 235);
        createEditorFields( parent, styleEditorInput.get().featureType(), styleEditorInput.get().featureStore(), style, 1, sectionBackgroundColor );
        site().layout( true );
    }


    private void createEditorFields( final Composite parent, final FeatureType featureType, final FeatureStore featureStore,
            final org.polymap.model2.Composite style, final int level, final java.awt.Color sectionBackgroundColor ) {
        SortedSet<PropertyInfo<? extends org.polymap.model2.Composite>> propInfos = new TreeSet<PropertyInfo<? extends org.polymap.model2.Composite>>(new UIOrderComparator());
        propInfos.addAll( style.info().getProperties());
        for (PropertyInfo<? extends org.polymap.model2.Composite> propInfo : propInfos) {
            if (StylePropertyValue.class.isAssignableFrom( propInfo.getType() )) {
                StylePropertyFieldSite fieldSite = new StylePropertyFieldSite();
                fieldSite.prop.set( (Property<StylePropertyValue>)propInfo.get( style ) );
                fieldSite.featureStore.set( featureStore );
                fieldSite.featureType.set(featureType );
                StylePropertyField field = new StylePropertyField( fieldSite );
                fields .add( field );
                Control control = field.createContents( parent );

                // the widthHint is a minimal width; without the fields expand
                // the enclosing section
                control.setLayoutData( ColumnDataFactory.defaults()
                        .widthHint( site().preferredWidth.get() - (level * 10) ).create() );
            }
            else if (StyleComposite.class.isAssignableFrom( propInfo.getType() )) {
                Section section = tk().createSection( parent, 
                        i18nStyle.get( propInfo.getDescription().orElse( propInfo.getName() ) ), 
                        TREE_NODE, Section.SHORT_TITLE_BAR, Section.FOCUS_TITLE, SWT.BORDER );
                section.setToolTipText( i18nStyle.get( propInfo.getDescription().orElse( propInfo.getName() ) + "Tooltip" ) );
                section.setExpanded( level == 1 );
                section.setBackground( UIUtils.getColor( sectionBackgroundColor.getRed() * (11-level) /10, sectionBackgroundColor.getGreen()* (11-level) /10, sectionBackgroundColor.getBlue()* (11-level) /10) );
                ((Composite)section.getClient()).setLayout( ColumnLayoutFactory.defaults()
                        .columns( 1, 1 ).margins( 0, 0, 5, 0 ).spacing( 5 ).create() );
                
                createEditorFields( 
                        (Composite)section.getClient(), featureType, featureStore, 
                        ((Property<? extends org.polymap.model2.Composite>)propInfo.get( style )).get(), level + 1, sectionBackgroundColor );
            }
        }
    }

    /**
     * 
     */
    class ActiveActionProvider
            extends CheckboxActionProvider {

        
        public ActiveActionProvider() {
            super( P4Plugin.images().svgImage( "eye.svg", P4Plugin.TOOLBAR_ICON_CONFIG ), P4Plugin.images().svgImage( "eye-off.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
        }
        
        @Override
        protected boolean initSelection( MdListViewer viewer, Object elm ) {
            return ((Style)elm).active.get();
        }


        @Override
        protected void onSelectionChange( MdListViewer viewer, Object elm ) {
            ((Style)elm).active.set( isSelected( elm ) );
        }
    }
    
    
    /**
     * 
     */
    class RemoveActionProvider
            extends ActionProvider {

        @Override
        public void perform( MdListViewer viewer, Object elm ) {
            ((Style)elm).removed.set( true );
            //list.getTree().deselectAll();
            list.remove( elm );
            //list.getTree().layout( true );            
        }


        @Override
        public void update( ViewerCell cell ) {
            cell.setImage( P4Plugin.images().svgImage( "delete.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
        }
    }
    
    
    /**
     * 
     */
    class AddPointItem
            extends ActionItem {

        public AddPointItem( ItemContainer container ) {
            super( container );
            icon.set( P4Plugin.images().svgImage( "map-marker.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Create a new Point/Marker render description" );
            action.set( ev -> {
                DefaultStyle.fillPointStyle( featureStyle );
                list.refresh( true );
            });
        }
    }

    
    /**
     * 
     */
    class AddPolygonItem
            extends ActionItem {

        public AddPolygonItem( ItemContainer container ) {
            super( container );
            icon.set( P4Plugin.images().svgImage( "vector-polygon.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Create a new Polygon render description" );
            action.set( ev -> {
                DefaultStyle.fillPolygonStyle( featureStyle );
                list.refresh( true );
            } );
        }
    }


    class AddLineItem
            extends ActionItem {

        public AddLineItem( ItemContainer container ) {
            super( container );
            icon.set( P4Plugin.images().svgImage( "vector-polyline.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Create a new Line render description" );
            action.set( ev -> {
                DefaultStyle.fillLineStyle( featureStyle );
                list.refresh( true );
            } );
        }
    }


    /**
     * 
     */
    class AddTextItem
            extends ActionItem {

        public AddTextItem( ItemContainer container ) {
            super( container );
            // XXX we need a text icon here
            icon.set( P4Plugin.images().svgImage( "format-title.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Create a new Text render description" );
            action.set( ev -> {
                DefaultStyle.fillTextStyle( featureStyle, styleEditorInput.get().featureType() );
                list.refresh( true );
            } );
        }
    }


    /**
     * 
     */
    protected class StyleIdentityComparer
            implements IElementComparer {

        @Override
        public int hashCode( Object elm ) {
            return elm.hashCode();
        }


        @Override
        public boolean equals( Object a, Object b ) {
            return a == b;
        }
    }

}
