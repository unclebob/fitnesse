// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002, 2003 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using fit;

namespace eg.music {
    public class Browser : Fixture {


        // Library //////////////////////////////////

        public void library (string path) {
            MusicLibrary.load(path);
        }

        public int totalSongs() {
            return MusicLibrary.library.Length;
        }

        // Select Detail ////////////////////////////

        public string playing () {
            return MusicPlayer.playing.title;
        }

        public void select (int i) {
            MusicLibrary.select(MusicLibrary.library[i-1]);
        }

        public string title() {
            return MusicLibrary.looking.title;
        }

        public string artist() {
            return MusicLibrary.looking.artist;
        }

        public string album() {
            return MusicLibrary.looking.album;
        }

        public int year() {
            return MusicLibrary.looking.year;
        }

        public double time() {
            return MusicLibrary.looking.time();
        }

        public string track() {
            return MusicLibrary.looking.track();
        }

        // Search Buttons ///////////////////////////

        public void sameAlbum() {
            MusicLibrary.findAlbum(MusicLibrary.looking.album);
        }

        public void sameArtist() {
            MusicLibrary.findArtist(MusicLibrary.looking.artist);
        }

        public void sameGenre() {
            MusicLibrary.findGenre(MusicLibrary.looking.genre);
        }

        public void sameYear() {
            MusicLibrary.findYear(MusicLibrary.looking.year);
        }

        public int selectedSongs() {
            return MusicLibrary.displayCount();
        }

        public void showAll() {
            MusicLibrary.findAll();
        }

        // Play Buttons /////////////////////////////

        public void play() {
            MusicPlayer.play(MusicLibrary.looking);
        }

        public void pause() {
            MusicPlayer.pause();
        }

        public string status() {
            return Music.status;
        }

        public double remaining() {
            return MusicPlayer.minutesRemaining();
        }

    }
}