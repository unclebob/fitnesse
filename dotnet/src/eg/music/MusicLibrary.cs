// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Read license.txt in this directory.

using System.IO;
using System.Collections;

namespace eg.music {
    public class MusicLibrary {
        internal static Music looking = null;
        internal static Music[] library = {};

        internal static void load(string name) {
            ArrayList music = new ArrayList();
            StreamReader input = new StreamReader(name);
            input.ReadLine(); // skip column headings
            string line = input.ReadLine();
            while(line != null) {
                music.Add(Music.parse(line));
                line = input.ReadLine();
            }
            input.Close();
            library = (Music[])music.ToArray(typeof(Music));
        }

        internal static void select(Music m) {
            looking = m;
        }

        internal static void search(double seconds){
            Music.status = "searching";
            Simulator.nextSearchComplete = Simulator.schedule(seconds);
        }

        internal static void searchComplete() {
            Music.status = MusicPlayer.playing == null ? "ready" : "playing";
        }

        internal static void findAll() {
            search(3.2);
            for (int i=0; i<library.Length; i++) {
                library[i].selected = true;
            }
        }

        internal static void findArtist(string a) {
            search(2.3);
            for (int i=0; i<library.Length; i++) {
                library[i].selected = library[i].artist.Equals(a);
            }
        }

        internal static void findAlbum(string a) {
            search(1.1);
            for (int i=0; i<library.Length; i++) {
                library[i].selected = library[i].album.Equals(a);
            }
        }

        internal static void findGenre(string a) {
            search(0.2);
            for (int i=0; i<library.Length; i++) {
                library[i].selected = library[i].genre.Equals(a);
            }
        }

        internal static void findYear(int a) {
            search(0.8);
            for (int i=0; i<library.Length; i++) {
                library[i].selected = library[i].year == a;
            }
        }

        internal static int displayCount() {
            int count = 0;
            for (int i=0; i<library.Length; i++) {
                count += (library[i].selected ? 1 : 0);
            }
            return count;
        }

        internal static Music[] displayContents () {
            Music[] displayed = new Music[displayCount()];
                for (int i=0, j=0; i<library.Length; i++) {
                    if (library[i].selected) {
                        displayed[j++] = library[i];
                    }
                }
            return displayed;
        }

    }
}