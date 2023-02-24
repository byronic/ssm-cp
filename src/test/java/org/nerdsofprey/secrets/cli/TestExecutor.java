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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestExecutor {

  @Test
  public void testMockExecutorCopy() {
    Executor.main(
        new String[] { "--mock-provider", "--source", "/some/source/", "--destination", "/some/destination/" });
  }

  @Test
  public void testMockExecutorDelete() {
    Executor.main(new String[] { "--mock-provider", "--source", "/some/source/", "--delete" });
  }

  @Test
  public void testMockExecutorMove() {
    Executor.main(new String[] { "--mock-provider", "--source", "/some/source/", "--destination", "/some/destination/",
        "--move" });
  }

  @Test
  public void testHelpExecutes() {
    Executor.main(new String[] { "--mock-provider", "--help" });
  }

  @Test
  public void testMockExecutorFailure() {
    Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
      Executor.main(new String[] { "--mock-provider", "--source", "/autofail/", "--destination", "/destination/" });
    });

    Assertions.assertEquals("Errors occurred during the operation; view the log for details.", exception.getMessage());
  }

  @Test
  public void testFailsOnMoveAndDelete() {
    Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
      Executor.main(new String[] { "--mock-provider", "--source", "/some/source/", "--move", "--delete" });
    });

    Assertions.assertEquals("Move and delete arguments may not be declared together", exception.getMessage());
  }

  @Test
  public void testFailsOnNoCopyDestination() {
    Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
      Executor.main(new String[] { "--mock-provider", "--source", "/some/source/" });
    });

    Assertions.assertEquals("For copy or move operations, you must declare a valid destination",
        exception.getMessage());
  }

  @Test
  public void testFailsOnNoMoveDestination() {
    Exception exception = Assertions.assertThrows(RuntimeException.class, () -> {
      Executor.main(new String[] { "--mock-provider", "--source", "/some/source/", "--move" });
    });

    Assertions.assertEquals("For copy or move operations, you must declare a valid destination",
        exception.getMessage());
  }
}
