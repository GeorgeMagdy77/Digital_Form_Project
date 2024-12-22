## Croatia Infrastructure Testing Framework


To run tests against deployed environment

```bash
./gradlew clean test --tests "com.croatia.infra.testing.oci.*"

```

## Authenticating to run tests
1. Install OCI CLI -- [instructions](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm)
2. Use command `oci setup config`
   1. Oracle ID: 
      1. `Click profile on top right-hand corner`
      2. `Click on first link that contains your email`
      3. `Copy OCID from "User Information"`
   2. Tenancy ID: 
      1. `Click profile on top right-hand corner`
      2. `Click "Tenancy: ___" link`
      3. `Copy OCID in "Tenancy Information"`
   3. Region should be set to desired one
   4. Enter a path to the directory to store your keys in (or leave blank)
   5. Input a key name (or leave blank)
   6. Input passphrase (or leave blank)
3. Add your public `.pem` key to your user profile in OCI console
4. Check that the fingerprint under your API key in the console matches the one found in your `.oci/config` file