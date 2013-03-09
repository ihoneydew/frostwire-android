/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(R). All rights reserved.
 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostwire.search;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import org.gudy.azureus2.core3.torrent.TOTorrent;
import org.gudy.azureus2.core3.torrent.TOTorrentException;
import org.gudy.azureus2.core3.torrent.TOTorrentFile;
import org.gudy.azureus2.core3.util.TorrentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public abstract class TorrentSearchPerformer extends CrawlPagedWebSearchPerformer<TorrentWebSearchResult> {

    private static final Logger LOG = LoggerFactory.getLogger(TorrentSearchPerformer.class);

    private static final int TORRENT_DOWNLOAD_TIMEOUT = 10000; // 10 seconds

    public TorrentSearchPerformer(long token, String keywords, int timeout, int pages) {
        super(token, keywords, timeout, pages);
    }

    @Override
    public List<? extends SearchResult> crawlResult(TorrentWebSearchResult sr) {
        List<TorrentDeepSearchResult> list = new LinkedList<TorrentDeepSearchResult>();

        if (sr.getTorrentURI().startsWith("http")) {
            TOTorrent torrent = downloadTorrent(sr.getTorrentURI(), sr.getDetailsUrl());

            if (torrent != null) {
                TOTorrentFile[] files = torrent.getFiles();

                for (int i = 0; !isStopped() && i < files.length; i++) {
                    TOTorrentFile file = files[i];
                    list.add(new TorrentDeepSearchResult(sr, file));
                }
            }
        }

        return list;
    }

    protected TOTorrent downloadTorrent(String url, String referrer) {
        TOTorrent torrent = null;
        try {
            LOG.debug("Downloading torrent: " + url);
            byte[] data = fetchBytes(url, referrer, TORRENT_DOWNLOAD_TIMEOUT);
            torrent = TorrentUtils.readFromBEncodedInputStream(new ByteArrayInputStream(data));
        } catch (TOTorrentException e) {
            LOG.warn("Failed to download torrent: " + url + ", e=" + e.getMessage());
        }
        return torrent;
    }
}
