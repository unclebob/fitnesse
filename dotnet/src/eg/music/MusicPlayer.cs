// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Read license.txt in this directory.

using System;

namespace eg.music {
    public class MusicPlayer {

        internal static Music playing = null;
        internal static double paused = 0;

        // Controls /////////////////////////////////

        internal static void play(Music m) {
            if (paused == 0) {
                Music.status = "loading";
                double seconds = m == playing ? 0.3 : 2.5 ;
                Simulator.nextPlayStarted = Simulator.schedule(seconds);
            } else {
                Music.status = "playing";
                Simulator.nextPlayComplete = Simulator.schedule(paused);
                paused = 0;
            }
        }

        internal static void pause() {
            Music.status = "pause";
            if (playing != null && paused == 0) {
                paused = (Simulator.nextPlayComplete - Simulator.time) / 1000.0;
                Simulator.nextPlayComplete = 0;
            }
        }

        internal static void stop() {
            Simulator.nextPlayStarted = 0;
            Simulator.nextPlayComplete = 0;
            playComplete();
        }

        // Status ///////////////////////////////////

        internal static double secondsRemaining() {
            if (paused != 0) {
                return paused;
            } else if (playing != null) {
                return (Simulator.nextPlayComplete - Simulator.time) / 1000.0;
            } else {
                return 0;
            }
        }

        internal static double minutesRemaining() {
            return Math.Round(secondsRemaining() / .6) / 100.0;
        }

        // Events ///////////////////////////////////

        internal static void playStarted() {
            Music.status = "playing";
            playing = MusicLibrary.looking;
            Simulator.nextPlayComplete = Simulator.schedule(playing.seconds);
        }

        internal static void playComplete() {
            Music.status = "ready";
            playing = null;
        }
    }
}