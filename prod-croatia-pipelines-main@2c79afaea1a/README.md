# croatia-pipelines

This repository is the single source for the definitions, and runable jenkins declarative pipelines for Croatia.

The structure of the repository is split into `definitions`, and `library` folders.

Anyone looking to implement new pipelines, should look into the definitions subsection for explanations & how-tos.

## Structure

```
./
├── definitions                         # Holds the definitions for all pipelines as generated in Jenkins via the job-dsl plugin. Explained in detail in [Definitions](#definitions)
├── gradle                              # Gradle wrapper files (do not edit)
├── library                             # Jenkins shared library code, containing full pipelines and helper functions. Explained in detail in [Library](#library)
├── tactical                            # A replica of this structure for the in decomissioning tactical estate
└── test                                # Groovy tests for the job-dsl definitions, and project pipelines
```

## Definitions

The `definitions` folder is setup to leverage the [job-dsl](https://plugins.jenkins.io/job-dsl/) plugin, and as such, broadly contains groovy scripts designed to work only within this DSL context.

The reference that can be used to determine what parameters and options are available, can always be located at the dsl full api reference, located at: https://jenkins.projectcroatia.cloud/plugin/job-dsl/api-viewer/index.html

### Structure

```
./definitions
├── alpha               
│   ├── adapters.groovy             # Generates all adapter pipelines
│   ├── alphatemplateapp.groovy     # Generates the pipeline for the creation/templating of new Alpha repositories
│   ├── automation_tester.groovy    # Generates the per environment pipelines for automation testing
│   ├── charts.groovy               # Generated the job to store & versions the alpha microservices chart
│   ├── cut_release.groovy          # Generates the job for cutting release branches
|   └── libraries.groovy            # Generates all adapter pipelines
├── mobile 
│   ├── android.groovy              # Generates the job building and testing the android application
│   ├── ios.groovy                  # Generates the job building and testing the ios application
|   └── mobile.groovy               # Generates the meta pipeline for mobile, which uses android and ios as child jobs
├── _folders.groovy                 # Generates all folders (including) children, used in jenkins
├── argocd_enrol.groovy             # Generates the job for enrolling a cluster in ArgoCD
├── backstage.groovy                # Generates the job for building and updating the Backstage application
├── jenkins_agents.groovy           # Generates the jobs for building and publishing the Jenkins agent container images
├── kustomize.groovy                # Generates the jobs for validating kustomization repositories
├── pipelines.groovy                # Generates the job for validating and testing this repository
└── vault_enrol.groovy              # Generates the job for enrolling a cluster in Vault
```

### How to add a new pipeline

For microservices, to add a new adapter, this involved opening up the file `./definitions/alpha/adapters`, and adding the name of the bitbucket repository to the `projects` object at the top of the file.

## Library

The `library` folder is setup to act as a jenkins [shared library](https://www.jenkins.io/doc/book/pipeline/shared-libraries/), and within this subfolder, exactly matches the pattern as specified in the linked documentation.

Any files within this folder are implicitly added to jenkins using the `main` branch, and are therefore immediately available for use within any `Jenkinsfile` immediately, with no other modifications.

### Structure

```
./library
├── src/com/croatia/pipelines/helpers
│   ├── BitbucketPaginatedAPICall.groovy    # A class to handle a simple paginated rest GET request against Bitbucket
│   ├── ConventionalCommits.groovy          # A class for parsing conventional commit messages from a string
│   ├── RestAPICall.groovy                  # A basic implementation of a json rest api call
│   ├── SemanticVersion.groovy              # A class to build and increment semantic versioning strings
|   └── VersionIncrement.groovy             # Enums for possible semantic versioning increments
├── vars
│   ├── alphaAdapter.groovy                 # Contains the pipelines for verifying, releasing, and deploying alpha microservices
│   ├── alphaLibrary.groovy                 # Contains the pipelines for verifying and releasing Java code libraries
│   ├── androidBuild.groovy                 # Contains the pipeline for verifying and publishing the android app
│   ├── argocdEnrol.groovy                  # Contains the pipeline for enrolling a cluster into ArgoCD
│   ├── backstage.groovy                    # Contains the pipeline for deploying and updating Backstage
│   ├── cutRelease.groovy                   # Contains the pipeline for cutting a release branch for all alpha microservices
│   ├── iosBuild.groovy                     # Contains the pipeline for verifying and publishing the android app
│   ├── kustomization.groovy                # Contains the pipeline for verifying and checking kustomize repositories
│   ├── mobile.groovy                       # Contains the pipeline to orchestrate the mobile build and release
│   ├── templateapp.groovy                  # Contains the pipeline for generating new alpha microservice repositories
│   ├── vaultEnrol.groovy                   # Contains the pipeline for enrolling a cluster into Vault
|   └── versioning.groovy                   # Contains a helper funtion to run semantic versioning against a specific repository
```
