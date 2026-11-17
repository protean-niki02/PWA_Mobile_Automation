# PWA Mobile Automation

Playwright (Java) + TestNG tests for the Bima Sugam PWA on UAT, run as Android (Chromium) and iOS (WebKit)
device profiles. Requires JDK 21+ and Maven.

## Run

```
mvn test                                              # default smoke suite (testng.xml)
mvn test "-Dsuite=testng-sprint3-bd2m-767.xml"        # a sprint / ticket suite
mvn test "-Dtest=SC01_TC028_IndicativePremiumTagTest" "-Ddevice=android"   # one test (android | ios | desktop)
```

Suites: `testng-sprint1.xml`, `testng-sprint2-bd2m-683.xml`, `testng-sprint3.xml`,
`testng-sprint3-quotes-listing.xml` (BD2M-761), `testng-sprint3-bd2m-767.xml` (Confirm Plan),
`testng-sprint3-bd2m-770.xml` (Policy Details -> OVD).

## Settings

`src/test/resources/config.properties` holds the base URL, timeouts and the test data (test car
registration number, and for BD2M-770 `policies.mobileNumber` / `policies.otp` or `policyDetails.startUrl`).
Any key can be passed with `-Dkey=value`. Tests whose test data is empty are skipped, not failed.

## Evidence

Every test saves screenshots, video, trace and network logs to `test-evidence/<run>/...` (and the latest per
test case to `test-evidence/by-test-case/`). That folder is git-ignored.

## BrowserStack

Copy `browserstack.yml.example` to `browserstack.yml` (git-ignored) or set `BROWSERSTACK_USERNAME` and
`BROWSERSTACK_ACCESS_KEY`, then `mvn test -P browserstack`. Never commit credentials.
