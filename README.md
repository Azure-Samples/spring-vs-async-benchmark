---
page_type: sample to benchmark
languages:
- java
products:
- cosmosdb
description: "This sample will demonstrate the comparison with Spring SDK vs Async SDK of Azure Cosmos DB"
urlFragment: "update-this-to-unique-url-stub"
---

# Official Microsoft Sample

<!-- 
Guidelines on README format: https://review.docs.microsoft.com/help/onboard/admin/samples/concepts/readme-template?branch=master

Guidance on onboarding samples to docs.microsoft.com/samples: https://review.docs.microsoft.com/help/onboard/admin/samples/process/onboarding?branch=master

Taxonomies for products and languages: https://review.docs.microsoft.com/new-hope/information-architecture/metadata/taxonomies?branch=master
-->

Give a short description for your sample here. What does it do and why is it important?

## Contents

Outline the file contents of the repository. It helps users navigate the codebase, build configuration and any related assets.

| File/folder       | Description                                |
|-------------------|--------------------------------------------|
| `src`             | Sample source code.                        |
| `.gitignore`      | Define what to ignore at commit time.      |
| `CHANGELOG.md`    | List of changes to the sample.             |
| `CONTRIBUTING.md` | Guidelines for contributing to the sample. |
| `README.md`       | This README file.                          |
| `LICENSE`         | The license for the sample.                |

## Prerequisites

- Access to Azure Cosmos DB
- JAVA
- Spring SDK

## Setup

The project is having two sub projects (1) Spring & (2) Async, navigate to their respective folder and modify application.properties

## Running the sample

To run Async application, execute the following command:
```
java -jar async-loadtest.jar <number of concurrent operations>

```
To run Spring application, open command/shell and navigate to the spring sample folder then execute the following command:
```
mvnw clean spring-boot:run
```

## Key concepts

Following code piece will control the execution for Async
```
int numberOfOperations = args.length > 0 ? Integer.parseInt(args[0]) : _objConfig.NumberOfOperations;

			for (int i = 0; i < numberOfOperations;) {
				long insertLatency = objCosmos.insertDocuments(i);
				
				long readLatency = objCosmos.readDocuments(i);
			
				System.out.println(i + "\t" + insertLatency + "\t" + readLatency);
				
				if (i < 100)
					i += 10;
				else
					i += 100;
				}
```

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
