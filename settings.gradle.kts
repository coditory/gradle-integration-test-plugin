rootProject.name = "integration-test-plugin"

plugins {
    id("com.gradle.enterprise").version("3.4.1")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        if (!System.getenv("CI").isNullOrEmpty()) {
            publishAlways()
            tag("CI")
        }
    }
}
