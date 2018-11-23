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

package com.transgressoft.itunesplaylistscopier.view;

import com.transgressoft.commons.view.*;
import de.felixroske.jfxsupport.FXMLController;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

/**
 * Controller class an error alert window.
 * It has methods that customizes the content of the window, including an expandable text area
 * that could contain an exception stack trace, or a custom text, and an hyperlink with a custom action.
 *
 * @author Octavio Calleya
 */
@FXMLController
public class ErrorDialogController implements ErrorDialog {

    private final Logger LOG = LoggerFactory.getLogger(getClass().getName());
    private final Image COMMON_ERROR_IMAGE = new Image(getClass().getResourceAsStream("/images/common-error.png"));
    private final ErrorDialogDecorator DECORATOR = new ErrorDialogController.SimpleErrorDialogDecorator(this);

    private ErrorDialogView errorDialogView;

    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private ImageView errorImageView;
    @FXML
    private Button okButton;
    @FXML
    private Label titleLabel;
    @FXML
    private Hyperlink seeDetailsHyperlink;
    @FXML
    private ToggleButton seeDetailsToggleButton;
    @FXML
    private BorderPane titleMessageBorderPane;
    @FXML
    private BorderPane bottomBorderPane;
    @FXML
    private HBox seeDetailsHBox;
    @FXML
    private VBox messageVBox;
    @FXML
    private TextArea detailsTextArea;
    @FXML
    private Hyperlink reportHyperlink;
    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        messageVBox.getChildren().remove(reportHyperlink);
        messageVBox.getChildren().remove(messageLabel);
        rootBorderPane.getChildren().remove(detailsTextArea);
        bottomBorderPane.getChildren().remove(seeDetailsHBox);

        seeDetailsToggleButton.setOnAction(this::handleSeeDetails);
        seeDetailsHyperlink.setOnAction(this::handleSeeDetails);
        okButton.setOnAction(event -> {
            okButton.getScene().getWindow().hide();
            reset();
        });
    }

    private void handleSeeDetails(ActionEvent event) {
        if (! rootBorderPane.getChildren().contains(detailsTextArea)) {
            rootBorderPane.setCenter(detailsTextArea);
            seeDetailsToggleButton.setSelected(false);
            LOG.debug("Showing expandable area");
        }
        else {
            rootBorderPane.getChildren().remove(detailsTextArea);
            seeDetailsToggleButton.setSelected(true);
            LOG.debug("Expandable area hided");
        }
        messageVBox.getScene().getWindow().sizeToScene();
    }

    @Override
    public ErrorDialogDecorator withTitle(String title) {
        if (title != null)
            titleLabel.setText(title);
        return DECORATOR;
    }

    @Override
    public ErrorDialogDecorator withMessage(String message) {
        if (message != null && ! message.isEmpty()) {
            messageLabel.setText(message);
            if (! messageVBox.getChildren().contains(messageLabel)) {
                messageVBox.getChildren().add(0, messageLabel);
                if (messageVBox.getScene() != null)
                    messageVBox.getScene().getWindow().sizeToScene();
            }
        }
        return DECORATOR;
    }

    @Override
    public ErrorDialogDecorator withDetails(String details) {
        if (details != null && ! details.isEmpty()) {
            detailsTextArea.clear();
            detailsTextArea.setText(details);
            placeExpandableButtons();
        }
        else {
            removeDetailsArea();
            removeExpandableButtons();
        }
        return DECORATOR;
    }

    private void placeExpandableButtons() {
        if (! bottomBorderPane.getChildren().contains(seeDetailsHBox)) {
            bottomBorderPane.setLeft(seeDetailsHBox);
            if (messageVBox.getScene() != null)
                messageVBox.getScene().getWindow().sizeToScene();
        }
    }

    private void removeDetailsArea() {
        if (titleMessageBorderPane.getChildren().contains(detailsTextArea))
            titleMessageBorderPane.getChildren().remove(detailsTextArea);
    }

    private void removeExpandableButtons() {
        if (bottomBorderPane.getChildren().contains(seeDetailsHBox))
            bottomBorderPane.getChildren().remove(seeDetailsHBox);
    }

    @Override
    public ErrorDialogDecorator withException(Throwable throwable) {
        if (throwable != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            detailsTextArea.clear();
            detailsTextArea.setText(stringWriter.toString());
            placeExpandableButtons();
        }
        else {
            removeDetailsArea();
            removeExpandableButtons();
        }
        return DECORATOR;
    }

    @Override
    public ErrorDialogDecorator withReportAction(String linkTitle, EventHandler<ActionEvent> reportAction) {
        if (! linkTitle.isEmpty() && reportAction != null) {
            reportHyperlink.setText(linkTitle);
            reportHyperlink.setOnAction(reportAction);
            if (! messageVBox.getChildren().contains(reportHyperlink)) {
                int position = messageVBox.getChildren().contains(messageLabel) ? 1 : 0;
                messageVBox.getChildren().add(position, reportHyperlink);
                if (messageVBox.getScene() != null)
                    messageVBox.getScene().getWindow().sizeToScene();
            }
        }
        return DECORATOR;
    }

    @Override
    public ErrorDialogDecorator withImage(Image image) {
        if (! COMMON_ERROR_IMAGE.equals(image))
            errorImageView.setImage(image);
        return DECORATOR;
    }

    private void internalShow() {
        errorDialogView.showView(Modality.APPLICATION_MODAL);
    }

    private void internalShow(Window window) {
        errorDialogView.showView(window, Modality.APPLICATION_MODAL);
        window.sizeToScene();
    }

    private void reset() {
        titleLabel.setText("");
        messageLabel.setText("");
        detailsTextArea.setText("");
        reportHyperlink.setText("");
        messageVBox.getChildren().remove(messageLabel);
        messageVBox.getChildren().remove(reportHyperlink);
        removeDetailsArea();
        removeExpandableButtons();
        errorImageView.setImage(COMMON_ERROR_IMAGE);
    }

    public String getErrorTitle() {
        return titleLabel.getText();
    }

    public String getErrorContent() {
        return messageLabel.getText().isEmpty() ? reportHyperlink.getText() : messageLabel.getText();
    }

    public String getDetailsAreaText() {
        return detailsTextArea.getText();
    }

    public Image getErrorImage() {
        return errorImageView.getImage();
    }

    public Hyperlink getReportHyperlink() {
        return reportHyperlink;
    }

    public boolean isShowingReportLink() {
        return messageVBox.getChildren().contains(reportHyperlink);
    }

    public boolean isShowingMessage() {
        return messageVBox.getChildren().contains(messageLabel);
    }

    @Autowired
    public void setErrorDialogView(ErrorDialogView errorDialogView) {
        this.errorDialogView = errorDialogView;
    }

    private class SimpleErrorDialogDecorator implements ErrorDialogDecorator {

        private ErrorDialog errorDialog;

        private SimpleErrorDialogDecorator(ErrorDialog errorDialog) {
            this.errorDialog = errorDialog;
        }

        @Override
        public void show() {
            internalShow();
        }

        @Override
        public void show(Window window) {
            internalShow(window);
        }

        @Override
        public ErrorDialogDecorator withTitle(String title) {
            return errorDialog.withTitle(title);
        }

        @Override
        public ErrorDialogDecorator withMessage(String message) {
            return errorDialog.withMessage(message);
        }

        @Override
        public ErrorDialogDecorator withDetails(String details) {
            return errorDialog.withDetails(details);
        }

        @Override
        public ErrorDialogDecorator withException(Throwable throwable) {
            return errorDialog.withException(throwable);
        }

        @Override
        public ErrorDialogDecorator withReportAction(String linkTitle, EventHandler<ActionEvent> reportAction) {
            return errorDialog.withReportAction(linkTitle, reportAction);
        }

        @Override
        public ErrorDialogDecorator withImage(Image image) {
            return errorDialog.withImage(image);
        }
    }
}