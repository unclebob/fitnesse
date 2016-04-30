This section will contain custom resources (css, templates).

NOTE: You can not upload files to files/fitnesse via FitNesse. Instead add
files directly.

You can override files in files/fitnesse or add your own (e.g. a custom
theme). Both the render engine (Velocity) and the FileResponder check
files/fitnesse and the fitnesse.resources package when looking for a particular
file.

Say you want to add a custom theme called "custom". What do you do?

 1. Create FitNesseRoot/files/fitnesse/css/custom.css for styling
 2. Create FitNesseRoot/files/fitnesse/javascript/custom.js for behaviour
    (optional)
 3. Define the style in plugins.properties
 
        Theme=custom
        
 4. Edit the style (e.g. to match the corporate standard)

If you're using Maven for example, you might want to distribute the style to
FitNesse projects. In that case you can create a jar with the styles in it:

        /fitnesse/resources/css/custom.css
        /fitnesse/resources/javascript/custom.js

and provide it as a dependency when launching FitNesse. You still have to
define the theme in plugins.properties.
