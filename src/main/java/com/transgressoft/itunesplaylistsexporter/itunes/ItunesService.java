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

import com.transgressoft.itunesplaylistsexporter.util.ItunesParserLogger;
import com.transgressoft.itunesplaylistsexporter.view.*;
import com.worldsworstsoftware.itunes.*;
import com.worldsworstsoftware.itunes.parser.ItunesLibraryParser;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Octavio Calleya
 */
@Service
public class ItunesService {

    private static final Logger LOG = LoggerFactory.getLogger(ItunesService.class);

    @Autowired
    private MainView mainView;
    @Autowired
    private ErrorDialogView errorDialogView;

    private ItunesLibrary itunesLibrary;
    private CompletableFuture<ItunesLibrary> task;

    public boolean isValidItunesLibraryXmlFile(File itunesLibraryXmFile) {
        boolean isValid;
        try {
            Source xmlFile = new StreamSource(itunesLibraryXmFile);
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

    public void cancelImport() {
        task.cancel(false);
    }

    public void importItunesLibrary(File itunesLibraryXmlFile) {
        task = CompletableFuture.supplyAsync(
                () -> itunesLibrary = ItunesLibraryParser.parseLibrary(itunesLibraryXmlFile.getPath(), new ItunesParserLogger()));
        task.thenAccept(
                itunesLibrary -> {
                    List<ItunesPlaylist> validPlaylists = ((List<ItunesPlaylist>) itunesLibrary.getPlaylists()).stream()
                            .filter(this::isValidItunesPlaylist)
                            .collect(Collectors.toList());
                    mainView.setItunesPlaylists(validPlaylists);
                });
    }

    private boolean isValidItunesPlaylist(ItunesPlaylist itunesPlaylist) {
        boolean notStrangeName = ! "####!####".equals(itunesPlaylist.getName());
        boolean notEmpty = ! itunesPlaylist.getPlaylistItems().isEmpty();
        return notStrangeName && notEmpty;
    }


    public void copyItunesPlaylists(List<ItunesPlaylist> playlistsToCopy, File targetDestination) {
        Map<String, List<Path>> trackPathsByPlaylistName = trackPathsByPlaylistName(playlistsToCopy);

        int totalTracks = trackPathsByPlaylistName.values().stream().mapToInt(List::size).sum();

        trackPathsByPlaylistName.forEach((playlistName, itunesFilePaths) -> {

            Path playlistPath = targetDestination.toPath().resolve(playlistName);
            if (! playlistPath.toFile().mkdir())
                mainView.log("Unable to create directory " + playlistPath.toString());
            else
                copyItunesTracks(itunesFilePaths, playlistPath, totalTracks);

        });
    }

    private Map<String, List<Path>> trackPathsByPlaylistName(List<ItunesPlaylist> playlists) {
        return playlists.stream()
                .collect(Collectors.toMap(ItunesPlaylist::getName, this::itunesFilePaths));
    }

    private void copyItunesTracks(List<Path> filePaths, Path targetDirectory, int totalTracks) {
        for (int i = 0; i < filePaths.size(); i++) {
            Path path = filePaths.get(i);
            try {
                FileCopyUtils.copy(path.toFile(), targetDirectory.toFile());
            }
            catch (IOException exception) {
                LOG.info("Error copying file {}: {}", path, exception.getCause());
                mainView.log("Error copying file " + path + ": " + "Error copying file " + path + ": " + exception.getMessage());
                StringWriter stringWriter = new StringWriter();
                exception.getCause().printStackTrace(new PrintWriter(stringWriter));
                mainView.log(stringWriter.toString());
            }

            mainView.updateProgress((1.0 * i / filePaths.size()) / totalTracks);
        }
    }

    private List<Path> itunesFilePaths(ItunesPlaylist itunesPlaylist) {
        return ((List<Integer>) itunesPlaylist.getTrackIDs()).stream()
                .map(itunesLibrary::getTrackById)
                .filter(this::isValidItunesTrack)
                .map(track -> Paths.get(track.getLocation()))
                .collect(Collectors.toList());
    }

    private boolean isValidItunesTrack(ItunesTrack itunesTrack) {
        return ! "URL".equals(itunesTrack.getTrackType()) && ! "Remote".equals(itunesTrack.getTrackType());
    }
}
