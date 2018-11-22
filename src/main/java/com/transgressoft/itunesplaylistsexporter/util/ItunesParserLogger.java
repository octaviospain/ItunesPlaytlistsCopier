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

package com.transgressoft.itunesplaylistsexporter.util;

import com.worldsworstsoftware.itunes.parser.logging.*;
import com.worldsworstsoftware.logging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class logger needed by iTunesUtilities library
 * to perform the itunes library parse.
 *
 * @author Octavio Calleya
 * @see <a href="https://github.com/codercowboy/iTunesUtilities">ItunesUtilities</a>
 */
public class ItunesParserLogger implements StatusUpdateLogger, ParserStatusUpdateLogger {

    private final Logger LOG = LoggerFactory.getLogger(getClass().getName());

    @Override
    public int getPlaylistParseUpdateFrequency() {
        return UPDATE_FREQUENCY_NEVER;
    }

    @Override
    public int getTrackParseUpdateFrequency() {
        return UPDATE_FREQUENCY_NEVER;
    }

    @Override
    public void debug(String arg0) {
        LOG.debug(arg0);
    }

    @Override
    public void fatal(String arg0, Exception arg1, boolean arg2) {
        String errorString = (arg2 ? "" : "NON ") + "RECOVERABLE FATAL ERROR: " + arg0 + ": " + arg1.getMessage();
        LOG.error(errorString);
    }

    @Override
    public void statusUpdate(int arg0, String arg1) {
        String errorString = "#" + arg0 + ": " + arg1;
        LOG.info(errorString);
    }

    @Override
    public void warn(String arg0, Exception arg1, boolean arg2) {
        error(arg0, arg1, arg2);
    }

    @Override
    public void error(String arg0, Exception arg1, boolean arg2) {
        String errorString = (arg2 ? "" : "NON ") + "RECOVERABLE ERROR: " + arg0 + ": " + arg1.getMessage();
        LOG.warn(errorString);
    }
}
