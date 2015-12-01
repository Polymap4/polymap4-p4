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
package org.polymap.p4.style.icon;

import static org.polymap.rhei.batik.toolkit.md.dp.dp;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.polymap.core.runtime.Callback;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;
import org.polymap.p4.P4Plugin;
import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.md.MdTabFolder;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.field.ImageDescription;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ImagePanel
        extends DefaultPanel {

    /**
     * 
     */
    private static final String                   TABLE_IMAGE_SELECTION_STYLE = "imageSelection";

    public static final PanelIdentifier           ID                          = PanelIdentifier.parse( "image" );

    private static final int                      IMAGE_BOXES_IN_ROW          = 8;

    private static final int                      PALETTE_BOX_SIZE            = 64;

    private ImageDescription                      imageDescription;

    @Scope(P4Plugin.Scope)
    private Context<IImageInfo>                   imageInfo;

    private MdToolkit                             toolkit;

    private Image                                 loadingImage                = null;

    private Button                                applyButton;

    private Color                                 originalTableCellBackground = null;

    private ViewerCell                            lastSelectedTableCell       = null;

    private String                                tabWithSelection;

    private ConcurrentMap<ImageDescription,Image> imageMap                    = new ConcurrentHashMap<ImageDescription,Image>();

    private Map<ImageDescription,ViewerCell>      cellMapping                 = new HashMap<ImageDescription,ViewerCell>();


    public ImagePanel() {
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.polymap.rhei.batik.IPanel#createContents(org.eclipse.swt.widgets.Composite
     * )
     */
    @Override
    public void createContents( Composite panelBody ) {
        toolkit = (MdToolkit)getSite().toolkit();
        getSite().setTitle( "Icon Selection" );
        prepareOpen( panelBody );
    }


    /**
     * Returns the currently selected image in the receiver.
     *
     * @return the image, may be null
     */
    public ImageDescription getImageDescription() {
        return imageDescription;
    }


    /**
     * Sets the receiver's selected image to be the argument.
     *
     * @param image the new image value, may be null to let the platform select a
     *        default when open() is called
     */
    public void setImageDescription( ImageDescription imageDescription ) {
        this.imageDescription = imageDescription;
    }


    protected void prepareOpen( Composite panelBody ) {
        createControls( panelBody );
    }


    private void createControls( Composite panelBody ) {
        Composite comp = toolkit.createComposite( panelBody, SWT.NONE );
        comp.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        List<String> tabItems = new ArrayList<String>();
        Map<String,Function<Composite,Composite>> tabContents = new HashMap<String,Function<Composite,Composite>>();
        if (getImageDescription() == null) {
            selectFirstElementInFirstTab( imageInfo.get().getImageLibrary().keySet().iterator().next() );
        }

        Function<Composite,Composite> compFunction = null;
        for (Pair<String,String> nameAndLicenceText : imageInfo.get().getImageLibrary().keySet()) {
            tabItems.add( nameAndLicenceText.getLeft() );
            compFunction = createLazyTabContent( nameAndLicenceText );
            tabContents.put( nameAndLicenceText.getLeft(), compFunction );
        }
        MdTabFolder tabFolder = toolkit.createTabFolder( comp, tabItems, tabContents, SWT.NONE );
        tabFolder.openTab( tabWithSelection );

        FormDataFactory.on( tabFolder ).top( 0 ).left( 0 ).right( 100 );
        createApplyButton( comp );
        FormDataFactory.on( applyButton ).top( tabFolder, dp( 30 ).pix() ).right( 100, -5 );

        setImages();
    }


    private void setImages() {
        EventManager.instance().subscribe( this, ( EventObject eo ) -> eo.getSource() instanceof ImageDescription );
        Display display = Display.getCurrent();
        Callback<Map<String,ImageDescriptor>> callback = new Callback<Map<String,ImageDescriptor>>() {

            @Override
            public void handle( Map<String,ImageDescriptor> result ) {
                display.asyncExec( new Runnable() {

                    public void run() {
                        try {
                            if (result != null) {
                                for (Map.Entry<String,ImageDescriptor> entry : result.entrySet()) {
                                    Image image = entry.getValue().createImage();
                                    if (image != null) {
                                        ImageDescription imageDesc = imageInfo.get().getImageDescriptionByPath(
                                                entry.getKey() );
                                        if (imageDesc != null) {
                                            Image scaledImage = getScaledImage( image, PALETTE_BOX_SIZE,
                                                    PALETTE_BOX_SIZE );
                                            imageMap.put( imageDesc, scaledImage );
                                            EventManager.instance().publish( new EventObject( imageDesc ) );
                                        }
                                    }
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                } );
            }
        };
        ImageDescription.createImagesForSize( PALETTE_BOX_SIZE, display, callback );
    }


    private Function<Composite,Composite> createLazyTabContent( Pair<String,String> nameAndLicenceText ) {
        return new Function<Composite,Composite>() {

            @Override
            public Composite apply( Composite parent ) {
                Composite comp = toolkit.createComposite( parent, SWT.NONE );
                comp.setLayout( FormLayoutFactory.defaults().create() );

                TableViewer tableViewer = new TableViewer( comp, SWT.VIRTUAL | SWT.V_SCROLL );
                tableViewer.getTable().setHeaderVisible( false );
                UIUtils.setVariant( tableViewer.getTable(), TABLE_IMAGE_SELECTION_STYLE );
                tableViewer.getTable().addListener( SWT.MouseDown, createImageSelectionListener( tableViewer ) );
                createTableColumns( nameAndLicenceText.getLeft(), tableViewer );
                List<List<ImageDescription>> listOfLists = createTableInput( nameAndLicenceText );
                tableViewer.setContentProvider( createImageContentProvider( tableViewer, listOfLists ) );
                // tableViewer.setUseHashlookup(true);
                tableViewer.setItemCount( listOfLists.size() );
                tableViewer.setInput( listOfLists );
                tableViewer.getControl().setCursor( new Cursor( parent.getDisplay(), SWT.CURSOR_HAND ) );

                Label licenceLabel = createLicenceLabel( nameAndLicenceText, comp );

                FormDataFactory.on( tableViewer.getControl() ).top( 0 ).left( 0 ).right( 100 ).height( dp( 500 ).pix() );
                FormDataFactory.on( licenceLabel ).top( tableViewer.getControl(), dp( 30 ).pix() );

                return comp;
            }
        };
    }


    private void createApplyButton( Composite comp ) {
        applyButton = toolkit.createButton( comp, "Apply selection", SWT.PUSH );
        applyButton.setEnabled( getImageDescription() != null );
        applyButton.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                imageInfo.get().setImageDescription( getImageDescription() );
                PanelPath path = getSite().getPath();
                getContext().closePanel( path );
                EventManager.instance().publish( new EventObject( imageInfo.get() ) );
            }
        } );
    }


    private void selectFirstElementInFirstTab( Pair<String,String> key ) {
        if (key != null) {
            List<ImageDescription> images = imageInfo.get().getImageLibrary().get( key );
            if (images.size() > 0) {
                setImageDescription( images.get( 0 ) );
            }
        }
    }


    private Listener createImageSelectionListener( TableViewer tableViewer ) {
        return new Listener() {

            public void handleEvent( Event event ) {
                Point pt = new Point( event.x, event.y );
                ViewerCell cell = tableViewer.getCell( pt );
                if (cell != null && cell != lastSelectedTableCell) {
                    selectImageInTable( cell );
                }
            }
        };
    }


    void selectImageInTable( ViewerCell cell ) {
        if (originalTableCellBackground == null) {
            originalTableCellBackground = cell.getBackground();
        }
        if (lastSelectedTableCell != null && !lastSelectedTableCell.getItem().isDisposed()) {
            lastSelectedTableCell.setBackground( originalTableCellBackground );
        }
        // TODO: extract to CSS
        cell.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_YELLOW ) );
        List<?> list = (List<?>)cell.getElement();
        if (cell.getColumnIndex() < list.size()) {
            ImageDescription imgDesc = (ImageDescription)list.get( cell.getColumnIndex() );
            setImageDescription( imgDesc );
        }
        lastSelectedTableCell = cell;
        if (applyButton != null) {
            applyButton.setEnabled( true );
        }
    }


    private void createTableColumns( String tabName, TableViewer tableViewer ) {
        for (int i = 0; i < IMAGE_BOXES_IN_ROW; i++) {
            TableViewerColumn columnView = new TableViewerColumn( tableViewer, SWT.NONE );
            columnView.getColumn().setWidth( 80 );
            int currentIndex = i;
            columnView.setLabelProvider( new ColumnLabelProvider() {

                @SuppressWarnings("unused")
                private String dummy = init();


                private String init() {
                    return null;
                }

                private Image scaledLoadingImage = null;


                public void update( ViewerCell cell ) {
                    super.update( cell );
                    selectCellIfElementMatch( cell );
                    ImageDescription imageDesc = getTypedElement( cell.getElement() );
                    if (imageDesc != null) {
                        cellMapping.put( imageDesc, cell );
                    }
                }


                private void selectCellIfElementMatch( ViewerCell cell ) {
                    ImageDescription imageDesc = getTypedElement( cell.getElement() );
                    if (imageDesc != null && getImageDescription() == imageDesc) {
                        selectImageInTable( cell );
                        tabWithSelection = tabName;
                    }
                }


                private ImageDescription getTypedElement( Object element ) {
                    List<?> list = (List<?>)element;
                    if (currentIndex < list.size()) {
                        return (ImageDescription)((List<?>)element).get( currentIndex );
                    }
                    else {
                        return null;
                    }
                }


                @Override
                public Image getImage( Object element ) {
                    ImageDescription imageDesc = getTypedElement( element );
                    if (imageDesc != null) {
                        if (imageMap.containsKey( imageDesc )) {
                            return imageMap.get( imageDesc );
                        }
                        else {
                            if (scaledLoadingImage == null) {
                                loadingImage = ImageDescriptor.createFromURL(
                                        BatikPlugin.instance().getBundle().getResource( "resources/icons/loading.gif" ) ).createImage();
                                scaledLoadingImage = getScaledImage( loadingImage, PALETTE_BOX_SIZE, PALETTE_BOX_SIZE );
                            }
                            return scaledLoadingImage;
                        }
                    }
                    else {
                        return null;
                    }
                }
            } );
        }
    }


    @EventHandler(delay = 100, display = true)
    protected void imageAvailable( List<EventObject> evs ) {
        evs.forEach( ev -> {
            if (ev.getSource() instanceof ImageDescription) {
                ImageDescription imgDesc = (ImageDescription)ev.getSource();
                ViewerCell cell = cellMapping.get( imgDesc );
                if (cell != null) {
                    Image image = imageMap.get( imgDesc );
                    if (image != null) {
                        if(!cell.getItem().isDisposed()) {
                            cell.setImage( image );
                        }
                    }
                }
            }
        } );
    }


    private List<List<ImageDescription>> createTableInput( Pair<String,String> nameAndLicenceText ) {
        List<ImageDescription> list = imageInfo.get().getImageLibrary().get( nameAndLicenceText );
        List<List<ImageDescription>> listOfLists = new ArrayList<List<ImageDescription>>();
        int turns = list.size() / IMAGE_BOXES_IN_ROW;
        int rest = list.size() % IMAGE_BOXES_IN_ROW;
        for (int i = 0; i < turns; i++) {
            listOfLists.add( list.subList( i * IMAGE_BOXES_IN_ROW, (i + 1) * IMAGE_BOXES_IN_ROW ) );
        }
        listOfLists.add( list.subList( list.size() - rest - 1, list.size() - 1 ) );
        return listOfLists;
    }


    private IContentProvider createImageContentProvider( TableViewer tableViewer,
            List<List<ImageDescription>> listOfLists ) {
        return new ILazyContentProvider() {

            @Override
            public void dispose() {
            }


            @Override
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
            }


            @Override
            public void updateElement( int index ) {
                if (!tableViewer.isBusy()) {
                    List<ImageDescription> imageDescs = listOfLists.get( index );
                    tableViewer.replace( imageDescs, index );
                }
            }
        };
    }


    private Label createLicenceLabel( Pair<String,String> nameAndLicenceText, Composite comp ) {
        Label licenceLabel = new Label( comp, SWT.NONE );
        licenceLabel.setText( nameAndLicenceText.getRight() );
        licenceLabel.setData( RWT.MARKUP_ENABLED, true );
        licenceLabel.setData( /*
                               * MarkupValidator.MARKUP_VALIDATION_DISABLED
                               */"org.eclipse.rap.rwt.markupValidationDisabled", false );
        return licenceLabel;
    }


    private Image getScaledImage( Image image, int width, int height ) {
        ImageData imageData = image.getImageData();
        Rectangle bounds = image.getBounds();
        int newHeight = -1;
        int newWidth = -1;
        if (bounds.width > bounds.height) {
            newHeight = Double.valueOf( width * bounds.height / bounds.width ).intValue();
            newWidth = width;
        }
        else {
            newWidth = Double.valueOf( height * bounds.width / bounds.height ).intValue();
            newHeight = height;
        }
        ImageData scaledImageData = imageData.scaledTo( newWidth, newHeight );
        return new Image( Display.getDefault(), scaledImageData );
    }
}
