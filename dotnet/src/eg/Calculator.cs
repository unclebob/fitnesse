// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using fit;
using System;

namespace eg {
    public class Calculator : ColumnFixture {

        public double volts;
        public string key;

        public static HP35 hp = new HP35();

        public bool points() {
            return false;
        }

        public bool flash() {
            return false;
        }

        public double watts() {
            return 0.5;
        }

        public override void Reset () {
            key = null;
        }

        public override void Execute () {
            if (key != null) {
                hp.key(key);
            }
        }

        public ScientificDouble x() {
            return new ScientificDouble(hp.r[0]);
        }

        public ScientificDouble y() {
            return new ScientificDouble(hp.r[1]);
        }

        public ScientificDouble z() {
            return new ScientificDouble(hp.r[2]);
        }

        public ScientificDouble t() {
            return new ScientificDouble(hp.r[3]);
        }


        public class HP35 {

            public double[] r = {0,0,0,0};
            public double s=0;

            public void key(string key) {
                if (numeric(key))               {push(double.Parse(key));}
                else if (key == "enter")   {push();}
                else if (key == "+")       {push(pop()+pop());}
                else if (key == "-")       {double t=pop(); push(pop()-t);}
                else if (key == "*")       {push(pop()*pop());}
                else if (key == "/")       {double t=pop(); push(pop()/t);}
                else if (key == "x^y")     {push(Math.Exp(Math.Log(pop())*pop()));}
                else if (key == "clx")     {r[0]=0;}
                else if (key == "clr")     {r[0]=r[1]=r[2]=r[3]=0;}
                else if (key == "chs")     {r[0]=-r[0];}
                else if (key == "x<>y")    {double t=r[0]; r[0]=r[1]; r[1]=t;}
                else if (key == "r!")      {r[3]=pop();}
                else if (key == "sto")     {s=r[0];}
                else if (key == "rcl")     {push(s);}
                else if (key == "sqrt")    {push(Math.Sqrt(pop()));}
                else if (key == "ln")      {push(Math.Log(pop()));}
                else if (key == "sin")     {push(Math.Sin(pop() * Math.PI / 180));}
                else if (key == "cos")     {push(Math.Cos(pop() * Math.PI / 180));}
                else if (key == "tan")     {push(Math.Tan(pop() * Math.PI / 180));}
                else {
                    throw new Exception("can't do key: "+key);
                }
            }

            bool numeric (string key) {
                return key.Length>= 1 &&
                    (char.IsDigit(key[0]) ||
                    (key.Length >= 2 &&
                    (key[0] == '-' &&
                    char.IsDigit(key[1]))));
            }

            void push() {
                for (int i=3; i>0; i--) {
                    r[i] = r[i-1];
                }
            }

            void push(double value) {
                push();
                r[0] = value;
            }

            double pop() {
                double result = r[0];
                for (int i=0; i<3; i++) {
                    r[i] = r[i+1];
                }
                return result;
            }
        }
    }
}