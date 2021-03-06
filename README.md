# ratpack-s3upload-example
[![Build Status](https://travis-ci.org/gregwhitaker/ratpack-s3upload-example.svg?branch=master)](https://travis-ci.org/gregwhitaker/ratpack-s3upload-example)

An example of doing a streaming upload of a file to [Amazon S3](https://aws.amazon.com/s3/) using [Ratpack](https://ratpack.io).

## Prerequisites
This example requires the following:

1. AWS Account
2. AWS Access Key ID and AWS Secret Access Key configured on your machine
3. S3 bucket configured in your AWS account that can be accessed by your keys

## Running the Example

1. Update the `bucket` configuration option in `config.yml` to your S3 bucket name.

2. Run the following command to start the application:

        ./gradlew run

3. Run the following curl command to upload the `cat.jpeg` image to S3:

        curl -T ./cat.jpeg -H "Content-Type:image/jpeg" http://localhost:5050/api/v1/upload
        
    If successful, you will now see that `cat.jpeg` has been uploaded to S3 with a UUID for a filename. Download the file from S3 and
    open it to validate that it was uploaded correctly.

## Bugs and Feedback
For bugs, questions, and discussions please use the [Github Issues](https://github.com/gregwhitaker/ratpack-s3upload-example/issues).

## License
MIT License

Copyright (c) 2018 Greg Whitaker

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.