plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "INFO901_Project"
include("domain")
include("communication")
include("app")
