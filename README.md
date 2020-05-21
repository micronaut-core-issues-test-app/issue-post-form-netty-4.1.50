# Test app

This is a test application to reproduce an issue upgrading Micronaut to Netty 4.1.50.Final

- Run the test: `./gradlew check`
- The test fails
- Comment out https://github.com/micronaut-core-issues-test-app/issue-post-form-netty-4.1.50/blob/master/build.gradle#L22-L30
to use the Netty version included in Micronaut 1.3.5 (4.1.48.Final)
- Run the test again: `./gradlew check`
- The test passes

