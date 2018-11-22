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

package com.transgressoft.itunesplaylistsexporter.view;

import com.transgressoft.itunesplaylistsexporter.itunes.ItunesService;
import com.transgressoft.itunesplaylistsexporter.view.custom.ItunesPlaylistListCell;
import com.worldsworstsoftware.itunes.ItunesPlaylist;
import de.felixroske.jfxsupport.FXMLController;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.stage.FileChooser.ExtensionFilter;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.*;

/**
 * @author Octavio Calleya
 */
@FXMLController
public class MainViewController {

    private final Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private MainView mainView;

    @Autowired
    private ItunesService itunesService;

    @FXML
    private ListView<ItunesPlaylist> sourcePlaylists;
    @FXML
    private ListView<ItunesPlaylist> targetPlaylists;
    @FXML
    private Label filePathLabel;
    @FXML
    private Label targetPathLabel;
    @FXML
    private Button chooseFileButton;
    @FXML
    private Button selectTargetDirectoryButton;
    @FXML
    private Button copyButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Button addSelectedButton;
    @FXML
    private Button removeSelectedButton;
    @FXML
    private Button addAllButton;
    @FXML
    private Button removeAllButton;
    @FXML
    private Button closeButton;

    private File targetDirectoryFile;

    @FXML
    public void initialize() {
        addSelectedButton.setOnAction(e -> moveSelected(sourcePlaylists, targetPlaylists));
        removeSelectedButton.setOnAction(e -> moveSelected(targetPlaylists, sourcePlaylists));
        addAllButton.setOnAction(e -> moveAll(sourcePlaylists, targetPlaylists));
        removeAllButton.setOnAction(e -> moveAll(targetPlaylists, sourcePlaylists));
        sourcePlaylists.setCellFactory(cell -> new ItunesPlaylistListCell(this));
        sourcePlaylists.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        targetPlaylists.setCellFactory(cell -> new ItunesPlaylistListCell(this));
        targetPlaylists.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        closeButton.setOnAction(e -> mainView.hide());
        chooseFileButton.setOnAction(this::chooseFile);
        selectTargetDirectoryButton.setOnAction(this::selectTargetDirectory);
        copyButton.setOnAction(e -> {
            if (copyButton.getText().equals("Copy")) {
                itunesService.copyItunesPlaylists(targetDirectoryFile, targetPlaylists.getItems());
                copyButton.setText("Cancel");
            }
            else {
                itunesService.cancelImport();
                copyButton.setText("Copy");
            }
        });
    }

    private void moveSelected(ListView<ItunesPlaylist> from, ListView<ItunesPlaylist> to) {
        ObservableList<ItunesPlaylist> selectedItems = from.getSelectionModel().getSelectedItems();
        to.getItems().addAll(selectedItems);
        from.getItems().removeAll(selectedItems);
        FXCollections.sort(to.getItems(), Comparator.comparing(ItunesPlaylist::getName));
    }

    private void moveAll(ListView<ItunesPlaylist> from, ListView<ItunesPlaylist> to) {
        to.getItems().addAll(from.getItems());
        from.getItems().clear();
        FXCollections.sort(to.getItems(), Comparator.comparing(ItunesPlaylist::getName));
    }

    public void movePlaylist(ItunesPlaylist playlist) {
        if (sourcePlaylists.getItems().contains(playlist)) {
            targetPlaylists.getItems().add(playlist);
            sourcePlaylists.getItems().remove(playlist);
            FXCollections.sort(targetPlaylists.getItems(), Comparator.comparing(ItunesPlaylist::getName));
        }
        else if (targetPlaylists.getItems().contains(playlist)) {
            sourcePlaylists.getItems().add(playlist);
            targetPlaylists.getItems().remove(playlist);
            FXCollections.sort(sourcePlaylists.getItems(), Comparator.comparing(ItunesPlaylist::getName));
        }
    }

    private void selectTargetDirectory(ActionEvent e) {
        LOG.debug("Choosing target directory");
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select target directory");
        targetDirectoryFile = chooser.showDialog(mainView.getView().getScene().getWindow());
        if (targetDirectoryFile != null) {
            targetPathLabel.setText(targetDirectoryFile.getAbsolutePath());
            copyButton.setDisable(false);
        }
    }

    private void chooseFile(ActionEvent e) {
        LOG.debug("Choosing Itunes xml file");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select 'iTunes Music Library.xml' file");
        chooser.getExtensionFilters().add(new ExtensionFilter("xml files (*.xml)", "*.xml"));
        File itunesLibraryXmlFile = chooser.showOpenDialog(mainView.getView().getScene().getWindow());
        if (itunesLibraryXmlFile != null && itunesService.isValidItunesFile(itunesLibraryXmlFile)) {
            filePathLabel.setText(itunesLibraryXmlFile.getAbsolutePath());
            selectTargetDirectoryButton.setDisable(false);
            itunesService.importItunesPlaylists(itunesLibraryXmlFile);
            progressBar.setProgress(- 1);
        }
    }

    public void updateProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public void setItunesPlaylists(List<ItunesPlaylist> itunesPlaylists) {
        sourcePlaylists.setItems(FXCollections.observableArrayList(itunesPlaylists));
        progressBar.setProgress(0);
    }
}
