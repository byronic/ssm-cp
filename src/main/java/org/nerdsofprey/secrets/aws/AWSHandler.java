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
package org.nerdsofprey.secrets.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import java.util.Map;
import java.util.stream.Collectors;

public class AWSHandler {
    private final static Logger log = LoggerFactory.getLogger(AWSHandler.class);
    private final SsmClient ssmClient;

    public AWSHandler() {
        ssmClient = DependencyFactory.ssmClient();
    }

    public void performDryRun(String source, String destination) {
        final String finalDestination = formatDestination(destination);
        final String prefix = getPrefix(source);

        // dry run -- don't actually copy, just collect the data, but do it as if it were
        GetParametersByPathRequest request = GetParametersByPathRequest.builder().path(source).recursive(true).withDecryption(true).build();
        GetParametersByPathResponse response = ssmClient.getParametersByPath(request);
        Map<String, String> toCopy = response.parameters().stream().collect(Collectors.toMap(Parameter::name,
            Parameter::value));
        while (response.nextToken() != null && response.nextToken() != "") {
            request = GetParametersByPathRequest.builder().nextToken(response.nextToken()).path(source).recursive(true).withDecryption(true).build();
            response = ssmClient.getParametersByPath(request);
            toCopy.putAll(response.parameters().stream().collect(Collectors.toMap(Parameter::name,
                Parameter::value)));
        }
        log.info("Dry run was selected, so I'll output the copy operation that _would_ be performed.");
        toCopy.keySet().forEach(key -> {
            log.info(String.format("copy '%s' -> '%s'", key, key.replace(prefix, finalDestination)));
        });
        log.info("Dry run was selected, so the above copies were _not_ executed.");
        log.info(String.format("Found %d total parameters to copy", toCopy.size()));
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
