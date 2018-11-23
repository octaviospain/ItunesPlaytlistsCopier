/*
 * This file is part of ItunesPlaylistsCopier software.
 *
 * ItunesPlaylistsCopier software is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * ItunesPlaylistsCopier code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ItunesPlaylistsCopier. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2018 Octavio Calleya
 */

package com.transgressoft.itunesplaylistscopier.view.custom;

import com.transgressoft.commons.util.ByteSizeRepresentation;
import com.transgressoft.itunesplaylistscopier.view.*;
import com.worldsworstsoftware.itunes.ItunesPlaylist;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.math.RoundingMode;

/**
 * Custom {@link ListCell} that defines the behaviour of an {@link ItunesPlaylist}
 * in the {@link MainView} window.
 *
 * @author Octavio Calleya
 */
public class ItunesPlaylistListCell extends ListCell<ItunesPlaylist> {

    private MainViewController playlistsPickerController;

    public ItunesPlaylistListCell(MainViewController mainViewController) {
        super();
        this.playlistsPickerController = mainViewController;
        setOnMouseClicked(this::onMouseClicked);
    }

    private void onMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2 && ! isEmpty())
            playlistsPickerController.movePlaylist(getItem());
    }

    @Override
    protected void updateItem(ItunesPlaylist itunesPlaylist, boolean empty) {
        super.updateItem(itunesPlaylist, empty);
        if (empty || itunesPlaylist == null)
            setGraphic(null);
        else
            setGraphic(new Label(getPlaylistString(itunesPlaylist)));
    }

    private String getPlaylistString(ItunesPlaylist itunesPlaylist) {
        int numTracks = itunesPlaylist.getTrackIDs().size();
        long totalSize = itunesPlaylist.getTotalSize();
        String sizeString = new ByteSizeRepresentation(totalSize).withMaximumDecimals(1, RoundingMode.CEILING);
        String name = itunesPlaylist.getName();
        return name + " [" + numTracks + " tracks]" + " [" + sizeString + "]";
    }
}
