// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using fit;
using System;

namespace eg.music {
    public class Simulator {

        // This discrete event simulator supports three events
        // each of which is open coded in the body of the simulator.

        internal static Simulator system = new Simulator();
        internal static long time = new DateTime().Ticks;

        public static long nextSearchComplete = 0;
        public static long nextPlayStarted = 0;
        public static long nextPlayComplete = 0;

        internal long nextEvent(long bound) {
            long result = bound;
            result = sooner(result, nextSearchComplete);
            result = sooner(result, nextPlayStarted);
            result = sooner(result, nextPlayComplete);
            return result;
        }

        internal long sooner (long soon, long eventVar) {
            return eventVar > time && eventVar < soon ? eventVar : soon;
        }

        internal void perform() {
            if (time == nextSearchComplete)     {MusicLibrary.searchComplete();}
            if (time == nextPlayStarted)        {MusicPlayer.playStarted();}
            if (time == nextPlayComplete)       {MusicPlayer.playComplete();}
        }

        internal void advance (long future) {
            while (time < future) {
                time = nextEvent(future);
                perform();
            }
        }

        internal static long schedule(double seconds){
            return time + (long)(1000 * seconds);
        }

        internal void delay (double seconds) {
            advance(schedule(seconds));
        }

        public void waitSearchComplete() {
            advance(nextSearchComplete);
        }

        public void waitPlayStarted() {
            advance(nextPlayStarted);
        }

        public void waitPlayComplete() {
            advance(nextPlayComplete);
        }

        public void failLoadJam() {
            ActionFixture.actor = new Dialog("load jamed", ActionFixture.actor);
        }

    }
}