# ssm-cp

`ssm-cp` is the AWS SSM parameter copy and move functionality missing from the AWS cli.

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/I2I6J9O0I)

## Getting Help / Troubleshooting

Feel free to create an issue on this project's [Issues page](https://github.com/byronic/ssm-cp/issues).

For expedited processing, include the issue number in your donation: [![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/I2I6J9O0I)

## Prerequisites

- Ensure you are signed in to the AWS CLI
- If you do not normally set a default `AWS_REGION` ensure that the environment variable is set

## Usage

On Mac or Linux, use the prebuilt `ssm-cp` binary located on the [releases page](https://github.com/byronic/ssm-cp/releases).

Alternatively, you may build and run the project locally with the included run.sh shell script. It performs a fresh build of the project and then passes along the command-line arguments you supply to the script.

Examples:

```
$ ssm-cp --help # or, ./run.sh --help if you are not using the precompiled image
10:19:08.397 [main] INFO  o.nerdsofprey.secrets.cli.Executor -- ssm-cp
Usage: ssm-cp [options]
  Options:
    --delete, -rm
      Perform a deletion on the source path. Defaults to false
      Default: false
    --destination, -dest
      Destination SSM path. This should be specified as a prefix (e.g.
      '/path/to/some/prefix/') -- if you neglect to include a trailing slash
      one will be provided for you
    --dry-run, -d
      Logs actions that would be taken by ssm-cp with respect to the other
      arguments provided, but does not perform any actions. Defaults to false
      Default: false
    --help
      Display this help message and exit
    --mock-provider
      Use an in-memory mock of a cloud provider rather than AWS. Defaults to
      false
      Default: false
    --move, -mv
      Perform a move (copy to the destination and delete the original) rather
      than a straight copy. Defaults to false
      Default: false
    --overwrite
      Overwrite the destination parameter if it exists. Defaults to false
      Default: false
  * --source, -src
      Source SSM path. A single variable should be specified with its full
      name or a 'directory'/prefix can be specified by including a trailing
      forward slash (/). '--source /path/to/SPECIFIC_VARIABLE' would choose a
      single variable as the source, while '--source /path/to/some/prefix/'
      would specify all variables recursively that begin with the prefix
      /path/to/some/prefix
```

```
$ ./run.sh --dry-run --source /some/source/path/ --destination /destination/path
# [build output elided]
15:40:17.617 [main] INFO  o.nerdsofprey.secrets.cli.Executor -- ssm-cp
15:40:17.636 [main] INFO  o.nerdsofprey.secrets.cli.Executor -- Setting up an instance of the AWS SSM client.
15:40:21.150 [main] INFO  o.nerdsofprey.secrets.aws.AWSHandler -- Dry run was selected, so I'll output the copy operation that _would_ be performed.
15:40:21.151 [main] INFO  o.nerdsofprey.secrets.aws.AWSHandler -- copy '/some/source/path/VARIABLE_NAME' -> '/destination/path/VARIABLE_NAME'
15:40:21.151 [main] INFO  o.nerdsofprey.secrets.aws.AWSHandler -- copy '/some/source/path/OTHER_VARIABLE' -> '/destination/path/OTHER_VARIABLE'
15:40:21.152 [main] INFO  o.nerdsofprey.secrets.aws.AWSHandler -- Dry run was selected, so the above copies were _not_ executed.
15:40:21.155 [main] INFO  o.nerdsofprey.secrets.aws.AWSHandler -- Found 2 total parameters to copy
```

