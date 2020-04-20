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

package com.transgressoft.itunesplaylistsutility;

import com.transgressoft.itunesplaylistsutility.view.MainView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Octavio Calleya
 */
@SpringBootApplication
public class ItunesPlaylistsUtilityApplication extends AbstractJavaFxApplicationSupport {

    public static void main(String[] args) {
        launch(ItunesPlaylistsUtilityApplication.class, MainView.class, null, args);
    }
}
