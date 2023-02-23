/*
 * Copyright © 2019 admin (admin@infrastructurebuilder.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nerdsofprey.secrets.cli;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.nerdsofprey.secrets.aws.AWSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Executor {
    private static final Logger log = LoggerFactory.getLogger(Executor.class);

    // command line arguments. See the documentation at http://jcommander.org
    @Parameter(names={"--dry-run", "-d"}, description = "Logs actions that would be taken by ssm-cp with respect to the other arguments provided, but does not perform any actions. Defaults to false")
    boolean dryRun = false;

    @Parameter(names={"--source", "-src"}, required = true, description = "Source SSM path. A single variable should be specified with its full name or a 'directory'/prefix can be specified by including a trailing forward slash (/). '--source /path/to/SPECIFIC_VARIABLE' would choose a single variable as the source, while '--source /path/to/some/prefix/' would specify all variables recursively that begin with the prefix /path/to/some/prefix")
    String source;

    @Parameter(names={"--destination", "-dest"}, required = true, description = "Destination SSM path. This should be specified as a prefix (e.g. '/path/to/some/prefix/') -- if you neglect to include a trailing slash one will be provided for you")
    String destination;

    @Parameter(names={"--mv"}, description = "Perform a move (copy to the destination and delete the original) rather than a straight copy. Defaults to false")
    boolean move = false;

    @Parameter(names={"--mock-provider"}, description = "Use an in-memory mock of a cloud provider rather than AWS. Defaults to false")
    boolean mock = false;

    @Parameter(names = "--help", help = true)
    private boolean help;


    public static void main(String[] args) {
        log.info("ssm-cp");
        Executor exec = new Executor();

        JCommander cliargs = JCommander.newBuilder().addObject(exec).build();
        cliargs.parse(args);

        if (exec.help) {
            cliargs.usage();
        } else {
            // TODO: VALIDATE PARAMS HERE
            exec.run();
        }
    }

    private void run() {
        if (mock) {
            log.info("Mock provider is not yet implemented.");
        } else { // AWS
            if (move) {
                log.info("mv argument is not yet implemented.");
            } else if (dryRun) {
                log.info("Setting up an instance of the AWS SSM client.");
                AWSHandler handler = new AWSHandler();
                handler.performDryRun(source, destination);
            } else {
                log.info("copy argument without dryRun set is not yet implemented.");
            }
        }
    }
}
