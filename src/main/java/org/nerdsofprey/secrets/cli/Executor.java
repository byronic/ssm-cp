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
import org.nerdsofprey.secrets.provider.CloudProvider;
import org.nerdsofprey.secrets.provider.aws.AWSProvider;
import org.nerdsofprey.secrets.provider.aws.DependencyFactory;
import org.nerdsofprey.secrets.provider.mock.MockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Executor {
  private static final Logger log = LoggerFactory.getLogger(Executor.class);

  // command line arguments. See the documentation at http://jcommander.org
  @Parameter(names = { "--dry-run",
      "-d" }, description = "Logs actions that would be taken by ssm-cp with respect to the other arguments provided, but does not perform any actions. Defaults to false")
  private boolean dryRun = false;

  @Parameter(names = { "--source",
      "-src" }, required = true, description = "Source SSM path. A single variable should be specified with its full name or a 'directory'/prefix can be specified by including a trailing forward slash (/). '--source /path/to/SPECIFIC_VARIABLE' would choose a single variable as the source, while '--source /path/to/some/prefix/' would specify all variables recursively that begin with the prefix /path/to/some/prefix")
  private String source;

  @Parameter(names = { "--destination",
      "-dest" }, description = "Destination SSM path. This should be specified as a prefix (e.g. '/path/to/some/prefix/') -- if you neglect to include a trailing slash one will be provided for you")
  private String destination;

  @Parameter(names = { "--move",
      "-mv" }, description = "Perform a move (copy to the destination and delete the original) rather than a straight copy. Defaults to false")
  private boolean move = false;

  @Parameter(names = { "--delete", "-rm" }, description = "Perform a deletion on the source path. Defaults to false")
  private boolean delete = false;

  @Parameter(names = {
      "--mock-provider" }, description = "Use an in-memory mock of a cloud provider rather than AWS. Defaults to false")
  private boolean mock = false;

  @Parameter(names = "--help", help = true, description = "Display this help message and exit")
  private boolean help;

  @Parameter(names = {
      "--overwrite" }, description = "Overwrite the destination parameter if it exists. Defaults to false")
  private boolean overwrite = false;

  public static void main(String[] args) {
    log.info("ssm-cp");
    Executor exec = new Executor();

    JCommander cliargs = JCommander.newBuilder().addObject(exec).build();
    cliargs.parse(args);

    if (exec.help) {
      cliargs.usage();
    } else {
      String validateMessage = exec.validateParameters();
      if (validateMessage.isBlank()) {
        exec.run();
      } else {
        throw new RuntimeException(validateMessage);
      }
    }
  }

  private void run() {
    CloudProvider provider;
    if (mock) {
      provider = new MockProvider();
    } else {
      provider = new AWSProvider(DependencyFactory.ssmClient());
    }

    boolean success = false;
    if (move) {
      success = provider.performMove(source, destination, overwrite, dryRun);
    } else if (delete) {
      success = provider.performDelete(source, dryRun);
    } else {
      // copy
      success = provider.performCopy(source, destination, overwrite, dryRun);
    }

    if (success) {
      log.info("The operation completed successfully.");
    } else {
      throw new RuntimeException("Errors occurred during the operation; view the log for details.");
    }
  }

  private String validateParameters() {
    if (move && delete) {
      return "Move and delete arguments may not be declared together";
    }

    if (!delete && (destination == null || destination.isBlank())) {
      return "For copy or move operations, you must declare a valid destination";
    }

    return "";
  }
}
