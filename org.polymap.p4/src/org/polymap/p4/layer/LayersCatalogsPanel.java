/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.p4.layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerCell;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.IMetadataCatalog;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.ui.MetadataContentProvider;
import org.polymap.core.catalog.ui.MetadataLabelProvider;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.help.HelpAwarePanel;
import org.polymap.rhei.batik.toolkit.ActionText;
import org.polymap.rhei.batik.toolkit.ClearTextAction;
import org.polymap.rhei.batik.toolkit.DefaultToolkit;
import org.polymap.rhei.batik.toolkit.Snackbar.Appearance;
import org.polymap.rhei.batik.toolkit.TextActionItem;
import org.polymap.rhei.batik.toolkit.TextActionItem.Type;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.TreeExpandStateDecorator;

import org.polymap.p4.Messages;
import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.AllResolver;
import org.polymap.p4.catalog.MetadataIconProvider;
import org.polymap.p4.catalog.ResourceInfoPanel;
import org.polymap.p4.map.ProjectMapPanel;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class LayersCatalogsPanel
        extends P4Panel
        implements HelpAwarePanel {

    private static final Log log = LogFactory.getLog( LayersCatalogsPanel.class );

    protected static final IMessages    i18n = Messages.forPrefix( "LayersCatalogsPanel" );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "layersCatalogs" );

    private static final String         MEMENTO_WEIGHTS = "sashWeights";
    
    /** Inbound: */
    @Scope( P4Plugin.Scope )
    private Context<IMap>       map;
    
    /** Outbound: */
    @Scope( P4Plugin.Scope )
    private Context<IResourceInfo> selectedResource;
    
    /** Outbound: */
    @Scope( P4Plugin.Scope )
    private Context<IMetadata>  selectedMetadata;

    private SashForm            sashForm;

    private Composite           layersParent;

    private Composite           catalogsParent;

    private LayersPanel         layersPanel;

    private MdListViewer        catalogsList;

    
    @Override
    public String helpCmsPath() {
        // XXX Arena has no access to help CMS yet
        return "ui/layersCatalogs-welcome.md";
    }


    @Override
    public boolean beforeInit() {
        if (parentPanel().orElse( null ) instanceof ProjectMapPanel) {
            site().icon.set( P4Plugin.images().svgImage( "layers.svg", P4Plugin.HEADER_ICON_CONFIG ) );
            site().tooltip.set( LayersPanel.i18n.get( "tooltip" ) );
            site().title.set( "" );
            return true;
        }
        return false;
    }

    
    @Override
    public void dispose() {
        if (sashForm != null && !sashForm.isDisposed()) {
            int[] weights = sashForm.getWeights();
            site().memento().putInteger( MEMENTO_WEIGHTS+"0", weights[0]);
            site().memento().putInteger( MEMENTO_WEIGHTS+"1", weights[1]);
        }
        super.dispose();
    }


    @Override
    public void createContents( Composite parent ) {
        parent.setLayout( new FillLayout() );
        sashForm = new SashForm( parent, SWT.VERTICAL|SWT.SMOOTH );
        sashForm.setSashWidth( 6 );
        createLayersContents();
        createCatalogsContents();

        int[] weights = {1, 1};
        site().memento().optInteger( MEMENTO_WEIGHTS+"0" ).ifPresent( v -> {
            weights[0] = site().memento().getInteger( MEMENTO_WEIGHTS+"0" );
            weights[1] = site().memento().getInteger( MEMENTO_WEIGHTS+"1" );
        });
        sashForm.setWeights( weights );
        
        site().title.set( LayersPanel.i18n.get( "title" ) );
        site().setSize( SIDE_PANEL_WIDTH/2, SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH2 );
    }


    protected void createLayersContents() {
        layersParent = tk().createComposite( sashForm );
        layersPanel = new LayersPanel();
        getContext().propagate( layersPanel );
        layersPanel.setSite( site(), getContext() );  // ???
        layersPanel.init();
        layersPanel.createContents( layersParent );
        
        layersPanel.getViewer().addOpenListener( new IOpenListener() {
            @Override
            public void open( OpenEvent ev ) {
                SelectionAdapter.on( ev.getSelection() ).forEach( elm -> {
                    onLayerOpen( (ILayer)elm );
                });
            }
        });
    }

    
    protected void createCatalogsContents() {
        catalogsParent = tk().createComposite( sashForm );
        catalogsParent.setLayout( FormLayoutFactory.defaults().create() );
        
        // title
        Label sectionTitle = tk().createLabel( catalogsParent, "Data sources" );
        UIUtils.setVariant( sectionTitle, DefaultToolkit.CSS_SECTION_TITLE  );

        // tree/list viewer
        catalogsList = tk().createListViewer( catalogsParent, SWT.VIRTUAL, SWT.FULL_SELECTION, SWT.SINGLE );
        catalogsList.linesVisible.set( true );
        catalogsList.setContentProvider( new XMetadataContentProvider( P4Plugin.allResolver() ) );
        catalogsList.firstLineLabelProvider.set( new TreeExpandStateDecorator( catalogsList, new MetadataLabelProvider() )
                .luminanceDelta.put( -3f ).saturationDelta.put( 0f ) );
        catalogsList.iconProvider.set( new MetadataIconProvider() );
        catalogsList.firstSecondaryActionProvider.set( new ChevronOpenAction() );
        catalogsList.secondSecondaryActionProvider.set( new CreateLayerAction() );
        catalogsList.setInput( P4Plugin.catalogs() );

        catalogsList.addOpenListener( ev -> {
            SelectionAdapter.on( ev.getSelection() ).forEach( elm -> {
                //catalogsViewer.setSelection( new StructuredSelection() );
                catalogsList.collapseAllNotInPathOf( elm );
                catalogsList.toggleItemExpand( elm );

                if (elm instanceof IResourceInfo) {
                    selectedResource.set( (IResourceInfo)elm );
                    getContext().openPanel( getSite().getPath(), ResourceInfoPanel.ID );                        
                    onResourceOpen( (IResourceInfo)elm );
                }
//                if (elm instanceof IMetadata) {
//                    selectedMetadata.set( (IMetadata)elm );
//                    getContext().openPanel( getSite().getPath(), MetadataInfoPanel.ID );                        
//                }
//                else if (elm instanceof IResourceInfo) {
//                }
            });
        });
        
        ActionText search = tk().createActionText( catalogsParent, "" );
        search.getText().setEnabled( false );
        new TextActionItem( search, Type.DEFAULT )
                .action.put( ev -> doSearch( search.getText().getText() ) )
                .text.put( "Search..." )
                .tooltip.put( "Fulltext search. Use * as wildcard.<br/>&lt;ENTER&gt; starts the search." )
                .icon.put( P4Plugin.images().svgImage( "magnify.svg", SvgImageRegistryHelper.DISABLED12 ) );
        new ClearTextAction( search );

        // layout
        FormDataFactory.on( sectionTitle ).fill().noBottom();
        FormDataFactory.on( search.getControl() ).fill().top( sectionTitle ).noBottom();
        FormDataFactory.on( catalogsList.getTree() ).fill().top( search.getControl() );
    }


    protected void doSearch( String query ) {
        log.info( "doSearch(): ..." );
        
        MetadataContentProvider cp = (MetadataContentProvider)catalogsList.getContentProvider();
        cp.catalogQuery.set( query );
        cp.flush();
        
        // otherwise preserveSelection() fails because of no getParent()
        catalogsList.setSelection( new StructuredSelection() );
        catalogsList.setInput( P4Plugin.catalogs() );
        catalogsList.expandToLevel( 2 );
    }

    


    protected void onLayerOpen( ILayer layer ) {
        new UIJob( "" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                IResourceInfo resInfo = AllResolver.instance().resInfo( layer, monitor )
                        .orElseThrow( () -> new IllegalStateException( "No resource found for layer: " + layer ) );

                MetadataContentProvider provider = (MetadataContentProvider)catalogsList.getContentProvider();
                TreePath treePath = provider.treePathOf( resInfo );

                UIThreadExecutor.async( () -> {
                    catalogsList.setSelection( new StructuredSelection() );
                    catalogsList.collapseAll();
                });
                
                Thread.sleep( 2000 );  // let the child panel open up
                for (int i=0; i<treePath.getSegmentCount()-1; i++) {
                    Object segment = treePath.getSegment( i );
                    UIThreadExecutor.async( () -> {
                        log.debug( "expanding: " + segment.getClass().getSimpleName() );
                        catalogsList.expandToLevel( segment, 1 );
                    });
                    log.debug( "waiting for: " + segment.getClass().getSimpleName() );
                    Thread.sleep( 1000 );  // FIXME
//                    while (sync( () -> viewer.getExpandedState( segment ) ).get() == false) {
//                        log.info( "waiting for: " + segment.getClass().getSimpleName() );
//                        Thread.sleep( 100 );
//                    }
                }
                UIThreadExecutor.async( () -> {
                    catalogsList.setSelection( new StructuredSelection( resInfo ), true );
                    catalogsList.reveal( resInfo );
                    catalogsList.getTree().showSelection();
                });
            }
        }.scheduleWithUIUpdate();
    }


    protected void onResourceOpen( IResourceInfo resInfo ) {
        // search connected layers
        String resId = AllResolver.resourceIdentifier( resInfo );
        List<ILayer> layers = layersPanel.map.get().layers.stream()
                .filter( layer -> resId.equals( layer.resourceIdentifier.get() ) )
                .collect( Collectors.toList() );

        // change selection
        MdListViewer viewer = layersPanel.getViewer();
        viewer.setSelection( new StructuredSelection( layers ) );
        viewer.getTree().showSelection();
    }
    
    
    /**
     * No catalogs level, show services at top level. 
     */
    protected class XMetadataContentProvider
            extends MetadataContentProvider {

        public XMetadataContentProvider( IMetadataResourceResolver resolver ) {
            super( resolver );
        }

        @Override
        protected void updateMetadataCatalogs( Collection<IMetadataCatalog> elm, int currentChildCount ) {
            updateChildrenLoading( elm );
            UIJob job = new UIJob( "Query catalogs" ) {
                @Override
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    List children = new ArrayList( 32 );
                    for (IMetadataCatalog catalog : elm) {
                        try {
                            catalog.query( catalogQuery.get(), monitor )
                                    .maxResults.put( maxResults.get() )
                                    .execute().stream()
                                    .forEach( child -> children.add( child ) );
                        }
                        catch (Exception e) {
                            log.warn( e );
                        }
                    }
                    updateChildren( elm, children.toArray(), currentChildCount );
                }
            };
            job.scheduleWithUIUpdate();
        }
    }
    

    /**
     * 
     */
    protected class CreateLayerAction
            extends ActionProvider {

        @Override
        public void update( ViewerCell cell ) {
            Object elm = cell.getElement();
            if (elm instanceof IResourceInfo) {
                cell.setImage( P4Plugin.images().svgImage( "plus-circle.svg", SvgImageRegistryHelper.OK24 ) );
            }
        }

        @Override
        public void perform( MdListViewer viewer, Object elm ) {
            IResourceInfo res = (IResourceInfo)elm;
            NewLayerContribution.createLayer( res, map.get(), ev -> {
                if (ev.getResult().isOK()) {
                    //BatikApplication.instance().getContext().closePanel( site().path() );
                    tk().createSnackbar( Appearance.FadeIn, "Layer has been created" );
                }
                else {
                    StatusDispatcher.handleError( "Unable to create new layer.", ev.getResult().getException() );
                }
            });
        }    
    }


    /**
     * 
     */
    protected class ChevronOpenAction
            extends ActionProvider {
        
        @Override 
        public void update( ViewerCell cell ) {
            if (cell.getElement() instanceof IResourceInfo) {
                cell.setImage( P4Plugin.images().svgImage( "chevron-right.svg", SvgImageRegistryHelper.NORMAL24 ) );
            }
        }
        
        @Override 
        public void perform( MdListViewer viewer, Object elm ) {
            selectedResource.set( (IResourceInfo)elm );
            getContext().openPanel( getSite().getPath(), ResourceInfoPanel.ID );                        
        }
    }
    
}
