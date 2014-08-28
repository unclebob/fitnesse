/**
 Jasmine Reporter that outputs test results to the browser console.
 Useful for running in a headless environment such as PhantomJs, ZombieJs etc.

 Usage:
 // From your html file that loads jasmine:
 jasmine.getEnv().addReporter(new jasmine.ConsoleReporter());
 jasmine.getEnv().execute();
 */

(function(jasmine, console) {
    if (!jasmine) {
        throw "jasmine library isn't loaded!";
    }

    var ConsoleReporter = function() {
        if (!console || !console.log) { throw "console isn't present!"; }
        this.status = this.statuses.stopped;
    };

    var proto = ConsoleReporter.prototype;
    proto.statuses = {
        stopped : "stopped",
        running : "running",
        fail    : "fail",
        success : "success"
    };

    proto.reportRunnerStarting = function(runner) {
        this.status = this.statuses.running;
        this.start_time = (new Date()).getTime();
        this.executed_specs = 0;
        this.passed_specs = 0;
    };

    proto.reportRunnerResults = function(runner) {
        var failed = this.executed_specs - this.passed_specs;
        var spec_str = this.executed_specs + (this.executed_specs === 1 ? " spec, " : " specs, ");
        var fail_str = failed + (failed === 1 ? " failure in " : " failures in ");
        var dur = (new Date()).getTime() - this.start_time;

        this.status = (failed > 0)? this.statuses.fail : this.statuses.success;
    };


    proto.reportSpecStarting = function(spec) {
        this.executed_specs++;
    };

    proto.reportSpecResults = function(spec) {
        if (spec.results().passed()) {
            this.passed_specs++;
            return;
        }

        var items = spec.results().getItems()
        for (var i = 0; i < items.length; i++) {
            var trace = items[i].trace.stack || items[i].trace;
            console.log(trace);
        }
    };

    proto.reportSuiteResults = function(suite) {
        if (suite.parentSuite) { return; }
        var results = suite.results();
        var failed = results.totalCount - results.passedCount;
        console.log(suite.getFullName() + ": " + results.passedCount + " of " + results.totalCount + " passed.");
    };

    jasmine.ConsoleReporter = ConsoleReporter;
})(jasmine, console);
