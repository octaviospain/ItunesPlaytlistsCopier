/*
 * This file is part of ItunesPlaylistsUtility software.
 *
 * ItunesPlaylistsUtility software is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * ItunesPlaylistsUtility code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ItunesPlaylistsUtility. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2018 Octavio Calleya
 */

package com.transgressoft.itunesplaylistsutility.view;

import com.transgressoft.commons.view.*;
import de.felixroske.jfxsupport.*;
import javafx.event.*;
import javafx.scene.image.Image;

/**
 * @author Octavio Calleya
 * @version 1.0
 */
@FXMLView (value = "/view/ErrorDialogView.fxml", title = "Error", stageStyle = "DECORATED")
public class ErrorDialogView extends AbstractFxmlView {

    public ErrorDialogDecorator withTitle(String s) {
        return ((ErrorDialog) getPresenter()).withTitle(s);
    }
    
    public ErrorDialogDecorator withMessage(String s) {
        return ((ErrorDialog) getPresenter()).withMessage(s);
    }
    
    public ErrorDialogDecorator withDetails(String s) {
        return ((ErrorDialog) getPresenter()).withDetails(s);
    }

    public ErrorDialogDecorator withException(Throwable throwable) {
        return ((ErrorDialog) getPresenter()).withException(throwable);
    }
    
    public ErrorDialogDecorator withReportAction(String s, EventHandler<ActionEvent> eventHandler) {
        return ((ErrorDialog) getPresenter()).withReportAction(s, eventHandler);
    }
    
    public ErrorDialogDecorator withImage(Image image) {
        return ((ErrorDialog) getPresenter()).withImage(image);
    }
}