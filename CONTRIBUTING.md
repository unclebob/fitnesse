# Bylaws for the committers of FitNesse.

1.  Use the FitNesse coding style.

    If you're using IntelliJ IDEA, move [extra/idea/fitnesse.xml](https://github.com/unclebob/fitnesse/blob/master/extra/idea/fitnesse.xml) 
    into the `codestyles` directory somewhere beneath IntelliJ config home. I found it on my MAC in 
    `~/Library/Preferences/IntelijIDEA8/codestyles`, or on Linux under ~/.IntelliJIdea12/config/codestyles .

    If you're using Eclipse, use [extra/eclipse/fitnesse-code-formatter-eclipse.xml](https://github.com/unclebob/fitnesse/blob/master/extra/eclipse/fitnesse-code-formatter-eclipse.xml) 

    If you can't use these files in your IDE then look carefully then you can emulate any of the source files in FitNesse.
    Remember that **indentation level is 2 spaces**, and we **never use tabs**.  Braces follow K&R style:
    ```
      void f() {
        //
      }
    ```

2.  Never commit changes without running BOTH the unit tests AND the acceptance tests.  The ant target 'all' in
    the build.xml file will run them both, but it's slow.  I just use the IDE to run the unit tests, and then run the
    acceptance tests with FitNesse.

3.  Please don't rush.  You are never in a hurry with FitNesse.  There are no deadlines.  Keep this code as clean
    as you can.  Maintain the highest pride in your workmanship.  Don't make messes.  See the "Clean Code" book for more.

4.  Whenever you make a change, add a few sentences to the changeLog.txt file describing your changes.  We used this file
    to build the release notes.

5.  If you have any questions, ask [Uncle Bob](https://github.com/unclebob).

