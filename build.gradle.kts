plugins {
    id("com.gradleup.nmcp.aggregation") version "1.4.3"
}

repositories {
    mavenCentral()
}

dependencies {
    allprojects {
        nmcpAggregation(project(path))
    }
}

nmcpAggregation {
    centralPortal {
        username = providers.gradleProperty("ossrhUsername").orNull
        password = providers.gradleProperty("ossrhPassword").orNull
        publishingType = "USER_MANAGED"
    }
}