// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using fit;
using System;
using System.Reflection;

namespace eg.music {
    public class Realtime : TimedActionFixture {

        Simulator system = Simulator.system;

		public DateTime time () 
		{
			return Time();
		}

		public override DateTime Time () 
		{
			return new DateTime(Simulator.time);
		}

		public void pause () 
		{
            double seconds = double.Parse(cells.more.Text());
            system.delay(seconds);
        }

        public void await () {
            systemMethod("wait", cells.more);
        }

        public void fail () {
            systemMethod("fail", cells.more);
        }

		public new void enter() 
		{
			Enter();
		}

		public override void Enter() 
		{
			system.delay(0.8);
			base.enter();
		}

		public new void press() 
		{
			Press();
		}

		public override void Press() 
		{
			system.delay(1.2);
			base.press();
		}

        private void systemMethod(string prefix, Parse cell) 
		{
            string method = Camel(prefix+" "+cell.Text());
            Type[] empty = {};
            try {
                BindingFlags searchFlags = BindingFlags.IgnoreCase | BindingFlags.Instance | BindingFlags.Public;
                MethodInfo methodInfo = system.GetType().GetMethod(method, searchFlags, null, empty, null);
                methodInfo.Invoke(system,empty);
            } catch (Exception e) {
                Exception (cell, e);
            }
        }
    }
}