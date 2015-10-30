/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
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
package org.polymap.p4.imports.utils.prompt;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.imports.utils.ISelectionAware;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public abstract class AbstractPromptBuilder<T, U>
        implements PromptUIBuilder {

    /**
     * 
     */
    private static final int     MAX_LIST_SIZE = 100;

    private ISelectionAware<T,U> selectionAware;

    private U                    value;

    private Label                hintLabel;


    public AbstractPromptBuilder( ISelectionAware<T,U> selectionAware ) {
        this.selectionAware = selectionAware;
    }


    @Override
    public void createContents( ImporterPrompt prompt, Composite parent ) {
        if(selectionAware.getSelected() != null) {
            setValue(transformToValueObject( selectionAware.getSelected() ));
        } else {
            setValue(selectionAware.getDefault());
        }
        if (selectionAware.getSelectable().size() < 5) {
            createAsRadioButtonList( parent );
        }
        else {
            createAsList( parent );
        }
    }


    protected abstract U transformToValueObject( T selected );


    protected void setValue( U value ) {
        this.value = value;
    }


    protected U getValue() {
        return value;
    }


    protected ISelectionAware<T,U> getProvider() {
        return selectionAware;
    }


    private void createAsRadioButtonList( Composite parent ) {
        boolean selected = false;
        for (U cs : selectionAware.getSelectable()) {
            Button btn = new Button( parent, SWT.RADIO );
            btn.setText( transformToDisplayValue( cs ) );
            btn.addSelectionListener( new SelectionAdapter() {

                @Override
                public void widgetSelected( SelectionEvent ev ) {
                    setValue( cs );
                }
            } );
            selected = getInitialSelection( cs );
            btn.setSelection( selected );
        }
    }


    protected abstract boolean getInitialSelection( U cs );


    private void createAsList( Composite parent ) {
        parent.setLayout( new GridLayout( 1, false ) );
        Composite filterComp = new Composite( parent, SWT.NULL );
        filterComp.setLayout( new GridLayout( 2, false ) );
        Label label = new Label( filterComp, SWT.NONE );
        label.setText( "Filter:" );
        Text filterText = new Text( filterComp, SWT.BORDER );
        filterText.setLayoutData( createHorizontalFill() );
        filterComp.setLayoutData( createHorizontalFill() );

        hintLabel = new Label( parent, SWT.NONE );
        hintLabel.setLayoutData( createHorizontalFill() );
        hintLabel.setForeground( parent.getDisplay().getSystemColor( SWT.COLOR_RED ) );

        org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List( parent, SWT.V_SCROLL );
        String selectedDisplayValue = null;
        if(selectionAware.getSelected() == null) {
            selectedDisplayValue = transformToDisplayValue( selectionAware.getDefault() );  
        } else if (getValue() != null) {
            selectedDisplayValue = transformToDisplayValue( getValue() );  
        }
        adjustListToMakeSelectedValueVisible( list, filterText, 0, selectedDisplayValue );
        list.setLayoutData( createHorizontalFillWithHeightHint( 200 ) );
        list.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                String displayName = list.getItem( list.getSelectionIndex() );
                setValue( transformFromDisplayValue( displayName ) );
            }
        } );
        filterText.addModifyListener( new ModifyListener() {

            @Override
            public void modifyText( ModifyEvent event ) {
                List<U> filteredAll = filterSelectable( filterText.getText() );
                String hint = setListContent( list, filteredAll, MAX_LIST_SIZE );
                if (list.getItems().length > 0) {
                    list.select( 0 );
                    handleSelection( list );
                }
                updateHint( hint );
            }

        } );
    }

    private void handleSelection( org.eclipse.swt.widgets.List list ) {
        list.setTopIndex( list.getSelectionIndex() );
        setValue( transformFromDisplayValue( list.getItem( list.getSelectionIndex() ) ) );
        list.showSelection();
    }

    private List<U> filterSelectable( String text ) {
        List<U> filteredAll = selectionAware.getSelectable().stream().filter( selectable -> {
            String name = transformToDisplayValue( selectable );
            if (text.startsWith( "*" )) {
                return name != null && name.contains( text.substring( 1 ) );
            }
                else {
                    return name != null && name.startsWith( text );
                }
            } ).collect( Collectors.toList() );
        return filteredAll;
    }


    private void adjustListToMakeSelectedValueVisible( org.eclipse.swt.widgets.List list, 
            Text filterText, int subIndex, String defaultDisplayValue ) {
        if (subIndex < defaultDisplayValue.length()) {
            String filterStr = defaultDisplayValue.substring( 0, subIndex );
            List<U> filtered = filterSelectable( filterStr );
            String hint = setListContent( list, filtered, MAX_LIST_SIZE );
            list.setSelection( new String[] { defaultDisplayValue } );
            int index = list.getSelectionIndex();
            if (index == -1) {
                adjustListToMakeSelectedValueVisible( list, filterText, subIndex + 1, defaultDisplayValue );
            }
            else {
                filterText.setText( filterStr );
                handleSelection( list );
                updateHint( hint );
            }
        }
    }


    private GridData createHorizontalFill() {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        return gridData;
    }


    private GridData createHorizontalFillWithHeightHint( int heightHint ) {
        GridData gridData = createHorizontalFill();
        gridData.heightHint = 200;
        return gridData;
    }


    private String setListContent( org.eclipse.swt.widgets.List list, List<U> all, int max ) {
        List<U> subList = null;
        String hint = "";
        if (all.size() >= max) {
            subList = all.subList( 0, max );
            hint = "Attention: Only first " + max + " elements of " + all.size()
                    + " elements \nare shown, use filter to reduce result count.";
        }
        else {
            subList = all;
        }
        list.setItems( new String[] {} );
        subList.stream().forEach( selectable -> list.add( transformToDisplayValue( selectable ) ) );
        return hint;
    }


    protected void updateHint( String hint ) {
        if (hintLabel != null && !hintLabel.isDisposed()) {
            hintLabel.setText( hint );
            hintLabel.pack();
        }
    }


    protected abstract U transformFromDisplayValue( String listEntry );


    protected abstract String transformToDisplayValue( U value );
}
