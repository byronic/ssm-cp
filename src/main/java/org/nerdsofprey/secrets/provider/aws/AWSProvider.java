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
package org.nerdsofprey.secrets.provider.aws;

import org.nerdsofprey.secrets.provider.CloudProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;

import java.util.ArrayList;
import java.util.List;

public class AWSProvider implements CloudProvider {
  private final static Logger log = LoggerFactory.getLogger(AWSProvider.class);
  private final SsmClient ssmClient;

  public AWSProvider(SsmClient ssmClient) {
    this.ssmClient = ssmClient;
  }

  @Override public boolean performCopy(String source, String destination, boolean overwrite, boolean dryRun) {
    return handleRequest(source, destination, dryRun, overwrite, true, false, false);
  }

  @Override public boolean performDelete(String source, boolean dryRun) {
    return handleRequest(source, "", dryRun, false, false, false, true);
  }

  @Override public boolean performMove(String source, String destination, boolean overwrite, boolean dryRun) {
    return handleRequest(source, destination, dryRun, overwrite, false, true, false);
  }

  private boolean handleRequest(String source, String destination, boolean dryRun, boolean overwrite, boolean copy,
      boolean move, boolean delete) {
    final String finalDestination = formatDestination(destination);
    final String prefix = getPrefix(source);

    List<Parameter> sourceParameters = compileParameterList(source);

    log.info(String.format("Found %d total parameters in source path", sourceParameters.size()));

    if (copy) {
      return performCopyHelper(sourceParameters, prefix, finalDestination, overwrite, dryRun);
    }

    if (move) {
      if (performCopyHelper(sourceParameters, prefix, finalDestination, overwrite, dryRun)) {
        return performDeleteHelper(sourceParameters, dryRun);
      }
    }

    if (delete) {
      return performDeleteHelper(sourceParameters, dryRun);
    }

    throw new RuntimeException("Invalid parameters were provided, no action was taken");
  }

  private boolean performDeleteHelper(List<Parameter> sourceParameters, boolean dryRun) {
    if (dryRun) {
      return performDryRunDelete(sourceParameters);
    }

    long errors = sourceParameters.stream().filter(parameter -> {
      log.info(String.format("Preparing to delete '%s'", parameter.name()));
      DeleteParameterRequest deleteRequest = DeleteParameterRequest.builder().name(parameter.name()).build();
      DeleteParameterResponse deleteResponse = ssmClient.deleteParameter(deleteRequest);
      boolean failed = !deleteResponse.sdkHttpResponse().isSuccessful();
      if (failed) {
        log.error(String.format("Failed to delete parameter %s", parameter.name()));
      }
      return failed;
    }).count();

    if (errors > 0) {
      log.error(String.format(
          "Found %d total parameters to delete, but encountered %d errors. The above error log includes the names of the parameters that failed to delete",
          sourceParameters.size(), errors));
      return false; // failed to delete the requested parameters
    }

    return true; // success!
  }

  private boolean performCopyHelper(List<Parameter> toCopy, String prefix, String destinationPrefix, boolean overwrite,
      boolean dryRun) {
    if (dryRun) {
      return performDryRunCopy(toCopy, prefix, destinationPrefix);
    }

    long errors = toCopy.stream().filter(parameter -> {
      String resultKey = parameter.name().replace(prefix, destinationPrefix);
      log.info(String.format("Preparing to copy '%s' -> '%s'", parameter.name(), resultKey));
      PutParameterRequest putRequest = PutParameterRequest
          .builder()
          .name(resultKey)
          .value(parameter.value())
          .dataType(parameter.dataType())
          .type(parameter.type())
          .overwrite(overwrite)
          .build();
      PutParameterResponse putResponse = ssmClient.putParameter(putRequest);
      boolean failed = !putResponse.sdkHttpResponse().isSuccessful();
      if (failed) {
        log.error(String.format("Failed to create parameter %s", resultKey));
      }
      return failed;
    }).count();
    if (errors > 0) {
      log.error(String.format(
          "Found %d total parameters to copy, but encountered %d errors. The above error log includes the names of the parameters that failed to create",
          toCopy.size(), errors));
      return false; // failed to copy all of the parameters
    }

    return true; // success!
  }

  private boolean performDryRunCopy(List<Parameter> toCopy, String prefix, String destinationPrefix) {
    toCopy.forEach(parameter -> {
      log.info(String.format("Would copy '%s' -> '%s'", parameter.name(),
          parameter.name().replace(prefix, destinationPrefix)));
    });
    log.info(
        String.format("Dry run was selected, so the above %d copy operations were _not_ executed.", toCopy.size()));
    return true;
  }

  private boolean performDryRunDelete(List<Parameter> sourceParameters) {
    sourceParameters.forEach(parameter -> {
      log.info(String.format("Would delete '%s'", parameter.name()));
    });
    log.info(String.format("Dry run was selected, so the above %d delete operations were _not_ executed.",
        sourceParameters.size()));
    return true;
  }

  private List<Parameter> compileParameterList(String source) {
    GetParametersByPathRequest request = GetParametersByPathRequest
        .builder()
        .path(source)
        .recursive(true)
        .withDecryption(true)
        .build();
    GetParametersByPathResponse response = ssmClient.getParametersByPath(request);
    List<Parameter> toCopy = new ArrayList<>(response.parameters());
    while (response.nextToken() != null && response.nextToken() != "") {
      request = GetParametersByPathRequest
          .builder()
          .nextToken(response.nextToken())
          .path(source)
          .recursive(true)
          .withDecryption(true)
          .build();
      response = ssmClient.getParametersByPath(request);
      toCopy.addAll(response.parameters());
    }
    return toCopy;
  }

  private String getPrefix(String source) {
    if (!source.endsWith("/")) {
      return source.substring(0, source.lastIndexOf('/'));
    }
    return source;
  }

  private String formatDestination(String destination) {
    if (!destination.endsWith("/")) {
      return String.format("%s/", destination);
    }
    return destination;
  }
}
