// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002, 2003 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using System;
using System.Globalization;
using System.IO;

namespace eg.music {
    public class Music {

        internal static String status = "ready";

        public string title;
        public string artist;
        public string album;
        public string genre;
        public long size;
        public int seconds;
        public int trackNumber;
        public int trackCount;
        public int year;
        public DateTime date;
        public bool selected = false;

        // Accessors ////////////////////////////////

        public string track() {
            return trackNumber + " of " + trackCount;
        }

        public double time() {
            return Math.Round(seconds / 0.6) / 100.0;
        }

        public override string ToString() {
            if (title != null) {
                return title;
            } 
            else {
                return base.ToString();
            }
        }


        // Factory //////////////////////////////////

        internal static string dateFormat = "M/d/yy h:mm a";

        internal static Music parse(string text) {
            Music m = new Music();
            string[] tokens = text.Split(new char[] {'\t'});
            m.title =       tokens[0];
            m.artist =      tokens[1];
            m.album =       tokens[2];
            m.genre =       tokens[3];
            m.size =        long.Parse(tokens[4]);
            m.seconds =     int.Parse(tokens[5]);
            m.trackNumber = int.Parse(tokens[6]);
            m.trackCount =  int.Parse(tokens[7]);
            m.year =        int.Parse(tokens[8]);
            m.date =        DateTime.Parse(tokens[9]);
            return m;
        }


    }
}