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

package com.transgressoft.itunesplaylistsutility.itunes;

import com.transgressoft.itunesplaylistsutility.util.ItunesParserLogger;
import com.transgressoft.itunesplaylistsutility.view.*;
import com.worldsworstsoftware.itunes.*;
import com.worldsworstsoftware.itunes.parser.ItunesLibraryParser;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;

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

        int count = 0;
        for (Map.Entry<String, List<Path>> entry : trackPathsByPlaylistName.entrySet()) {
            String playlistName = entry.getKey();
            List<Path> itunesFilePaths = entry.getValue();
            Path playlistPath = targetDestination.toPath().resolve(playlistName);
            if (! playlistPath.toFile().mkdir()) {
                count += entry.getValue().size();
                mainView.log("Unable to create directory " + playlistPath.toString());
            }
            else
                count = copyItunesTracks(itunesFilePaths, playlistPath, count, totalTracks);

        }
    }

    private Map<String, List<Path>> trackPathsByPlaylistName(List<ItunesPlaylist> playlists) {
        return playlists.stream()
                .collect(Collectors.toMap(ItunesPlaylist::getName, this::itunesFilePaths));
    }

    private int copyItunesTracks(List<Path> filePaths, Path targetDirectory, int tracksCount, int totalTracks) {
        int count = tracksCount;
        for (int i = 0; i < filePaths.size(); i++) {
            Path path = filePaths.get(i);
            try {
                String ensuredFileName = ensuredFileNameOnPath(targetDirectory, path.toFile().getName());
                Files.copy(path, targetDirectory.resolve(ensuredFileName), COPY_ATTRIBUTES);
                LOG.info("File copied: {}", path);
                mainView.log("File copied: " + path.toFile().getName());

            }
            catch (IOException exception) {
                LOG.info("Error copying file {}", path.toFile().getName());
                mainView.log("Error copying file " + path.toFile().getName());
            }

            mainView.updateProgress((1.0 * count++) / totalTracks);
        }
        return count;
    }

    /**
     * Ensures that the file name given is unique in the target directory, appending
     * (1), (2)... (n+1) to the file name in case it already exists
     *
     * @param fileName   The string of the file name
     * @param targetPath The path to check if there is a file with the name equals <tt>fileName</tt>
     *
     * @return The modified string
     */
    private String ensuredFileNameOnPath(Path targetPath, String fileName) {
        String newName = fileName;
        if (targetPath.resolve(fileName).toFile().exists()) {
            int pos = fileName.lastIndexOf('.');
            newName = fileName.substring(0, pos) + "(1)." + fileName.substring(pos + 1);
        }
        while (targetPath.resolve(newName).toFile().exists()) {
            int posL = newName.lastIndexOf('(');
            int posR = newName.lastIndexOf(')');
            int num = Integer.parseInt(newName.substring(posL + 1, posR));
            newName = newName.substring(0, posL + 1) + ++ num + newName.substring(posR);
        }
        return newName;
    }

    private List<Path> itunesFilePaths(ItunesPlaylist itunesPlaylist) {
        return ((List<Integer>) itunesPlaylist.getTrackIDs()).stream()
                .map(itunesLibrary::getTrackById)
                .filter(this::isValidItunesTrack)
                .map(track -> Paths.get(URI.create(track.getLocation())))
                .collect(Collectors.toList());
    }

    private boolean isValidItunesTrack(ItunesTrack itunesTrack) {
        return ! "URL".equals(itunesTrack.getTrackType()) && ! "Remote".equals(itunesTrack.getTrackType());
    }
}
