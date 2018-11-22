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

package com.transgressoft.itunesplaylistsexporter.itunes;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.transgressoft.itunesplaylistsexporter.util.ItunesParserLogger;
import com.transgressoft.itunesplaylistsexporter.view.*;
import com.worldsworstsoftware.itunes.*;
import com.worldsworstsoftware.itunes.parser.ItunesLibraryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Octavio Calleya
 */
@Service
public class ItunesService {

    @Autowired
    private MainView mainView;
    @Autowired
    private ErrorDialogView errorDialogView;

    private ItunesLibrary itunesLibrary;
    private int playlistsAndTracksCount = 0;
    private int playlistsAndTracksTotal = 0;
    private CompletableFuture<List<ItunesPlaylist>> task;
    private List<String> errors = new ArrayList<>();

    public boolean isValidItunesFile(File tunesLibraryXmlFile) {
        boolean isValid;
        try {
            Source xmlFile = new StreamSource(tunesLibraryXmlFile);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(getClass().getResource("/config/PropertyList-1.0.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
            isValid = true;
        }
        catch (Exception exception) {
            errorDialogView.withTitle("Error trying to validate the iTunes Library file")
                           .withException(exception)
                           .show();
            isValid = false;
        }
        return isValid;
    }

    public void importItunesPlaylists(File itunesLibraryXmlFile) {
        task = CompletableFuture.supplyAsync(() -> parseItunesPlaylists(itunesLibraryXmlFile));
        task.thenAccept(mainView::setItunesPlaylists);
    }

    public void cancelImport() {
        task.cancel(true);
    }

    private List<ItunesPlaylist> parseItunesPlaylists(File itunesLibraryXmlFile) {
        playlistsAndTracksCount = 0;
        playlistsAndTracksTotal = 0;
        ItunesParserLogger itunesLogger = new ItunesParserLogger();
        itunesLibrary = ItunesLibraryParser.parseLibrary(itunesLibraryXmlFile.getPath(), itunesLogger);
        List<ItunesPlaylist> itunesPlaylists = (List<ItunesPlaylist>) itunesLibrary.getPlaylists();
        itunesPlaylists = itunesPlaylists.stream()
                                         .filter(this::isValidItunesPlaylist)
                                         .collect(ImmutableList.toImmutableList());
        for (ItunesPlaylist itunesPlaylist : itunesPlaylists)
            playlistsAndTracksTotal += itunesPlaylist.getTrackIDs().size();

        return itunesPlaylists;
    }

    private boolean isValidItunesPlaylist(ItunesPlaylist itunesPlaylist) {
        boolean notStrangeName = ! "####!####".equals(itunesPlaylist.getName());
        boolean notEmpty = ! itunesPlaylist.getPlaylistItems().isEmpty();
        return notStrangeName && notEmpty;
    }

    public void copyItunesPlaylists(File targetDirectoryFile, List<ItunesPlaylist> itunesPlaylists) {
        errors.clear();
        itunesPlaylists.forEach(itunesPlaylist -> {
            List<ItunesTrack> itunesTracks = getItunesTracks(itunesPlaylist);
            Path playlistFolderFile = targetDirectoryFile.toPath().resolve(itunesPlaylist.getName());
            if (playlistFolderFile.toFile().mkdir())
                copyTracks(targetDirectoryFile, itunesTracks);

            mainView.updateProgress(1.0 * playlistsAndTracksCount++ / playlistsAndTracksTotal);
        });

        if (! errors.isEmpty()) {
            errorDialogView.withTitle("Error trying to validate the iTunes Library file")
                           .withDetails(Joiner.on("\n").join(errors))
                           .show();
        }
    }

    private List<ItunesTrack> getItunesTracks(ItunesPlaylist itunesPlaylist) {
        List<ItunesTrack> itunesTracks = new ArrayList<>();
        for (Object id : itunesPlaylist.getTrackIDs()) {
            ItunesTrack track = itunesLibrary.getTrackById((Integer) id);
            itunesTracks.add(track);
        }

        return itunesTracks;
    }

    private void copyTracks(File targetDirectoryFile, List<ItunesTrack> itunesTracks) {
        itunesTracks.stream()
                    .filter(this::isValidItunesTrack)
                    .forEach(itunesTrack -> {
                        String location = itunesTrack.getLocation();
                        try {
                            FileCopyUtils.copy(new File(location), targetDirectoryFile);
                        }
                        catch (IOException exception) {
                            errors.add("[" + location + "]:" + exception.getMessage());
                        }

                        playlistsAndTracksCount++;
                    });
    }

    private boolean isValidItunesTrack(ItunesTrack itunesTrack) {
        return ! "URL".equals(itunesTrack.getTrackType()) && ! "Remote".equals(itunesTrack.getTrackType());
    }
}
