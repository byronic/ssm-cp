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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.*;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAWSProvider {
  private static AWSProvider test;
  private static AWSProvider failer;

  @BeforeAll()
  public static void setupBeforeClass() {
    SsmClient mockSsm = mock();
    Parameter mockParameter = mock();
    List<Parameter> list = List.of(mockParameter);
    GetParametersByPathResponse response = mock();
    DeleteParameterResponse deleteResponse = mock();
    PutParameterResponse putResponse = mock();
    SdkHttpResponse httpResponse = mock();

    when(httpResponse.isSuccessful()).thenReturn(true);
    when(deleteResponse.sdkHttpResponse()).thenReturn(httpResponse);
    when(putResponse.sdkHttpResponse()).thenReturn(httpResponse);
    when(mockParameter.name()).thenReturn("mock_key");
    when(mockParameter.value()).thenReturn("mock_value");
    when(response.parameters()).thenReturn(list);
    when(mockSsm.getParametersByPath((GetParametersByPathRequest) any())).thenReturn(response);
    when(mockSsm.putParameter((PutParameterRequest) any())).thenReturn(putResponse);
    when(mockSsm.deleteParameter((DeleteParameterRequest) any())).thenReturn(deleteResponse);

    test = new AWSProvider(mockSsm);

    SsmClient failSsm = mock();
    SdkHttpResponse failHttpResponse = mock();
    DeleteParameterResponse deleteFailResponse = mock();
    PutParameterResponse putFailResponse = mock();
    GetParametersByPathResponse getMultipleParamsResponse = mock();
    GetParametersByPathResponse getLastParamsResponse = mock();

    when(getMultipleParamsResponse.nextToken()).thenReturn("TOKEN");
    when(getMultipleParamsResponse.parameters()).thenReturn(list);
    when(getLastParamsResponse.parameters()).thenReturn(list);
    when(failHttpResponse.isSuccessful()).thenReturn(false);
    when(deleteFailResponse.sdkHttpResponse()).thenReturn(failHttpResponse);
    when(putFailResponse.sdkHttpResponse()).thenReturn(failHttpResponse);
    when(failSsm.putParameter((PutParameterRequest) any())).thenReturn(putFailResponse);
    when(failSsm.deleteParameter((DeleteParameterRequest) any())).thenReturn(deleteFailResponse);
    when(failSsm.getParametersByPath((GetParametersByPathRequest) any())).thenReturn(getMultipleParamsResponse,
        getLastParamsResponse);

    failer = new AWSProvider(failSsm);
  }

  @Test
  public void dryRunCopyShouldSucceed() {
    Assertions.assertTrue(test.performCopy("/source/", "/destination/", false, true));
  }

  @Test
  public void dryRunMoveShouldSucceed() {
    Assertions.assertTrue(test.performMove("/source", "/destination", true, true));
  }

  @Test
  public void dryRunDeleteShouldSucceed() {
    Assertions.assertTrue(test.performDelete("/source", true));
  }

  @Test
  public void copyAndDeleteViaMoveShouldSucceed() {
    Assertions.assertTrue(test.performMove("/source", "/destination", true, false));
  }

  @Test
  public void testCopyFailure() {
    Assertions.assertFalse(failer.performCopy("/source", "/destination", false, false));
  }

  @Test
  public void testDeleteFailure() {
    Assertions.assertFalse(failer.performDelete("/source", false));
  }

}
