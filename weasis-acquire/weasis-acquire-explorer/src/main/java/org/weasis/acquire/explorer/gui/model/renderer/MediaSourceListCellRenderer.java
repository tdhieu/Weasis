/*******************************************************************************
 * Copyright (c) 2016 Weasis Team and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 *******************************************************************************/
package org.weasis.acquire.explorer.gui.model.renderer;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.weasis.acquire.explorer.media.MediaSource;

public class MediaSourceListCellRenderer extends JLabel implements ListCellRenderer<MediaSource> {
    private static final long serialVersionUID = -8043563870643819771L;

    public MediaSourceListCellRenderer(JComboBox<MediaSource> combo) {
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MediaSource> list, MediaSource value, int index,
        boolean isSelected, boolean cellHasFocus) {

        if (value != null) {
            setIcon(value.getIcon());
            String str = value.getDisplayName();
            setText(str);
        } else {
            setIcon(null);
            setText("");
        }

        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

        return this;
    }
}