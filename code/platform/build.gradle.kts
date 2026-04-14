import de.itemis.mps.gradle.GitBasedVersioning
import de.itemis.mps.gradle.BuildLanguages
import de.itemis.mps.gradle.EnvironmentKind
import de.itemis.mps.gradle.RunAntScript
import de.itemis.mps.gradle.TestLanguages
import de.itemis.mps.gradle.tasks.MpsGenerate
import de.itemis.mps.gradle.tasks.MpsMigrate
import de.itemis.mps.gradle.tasks.Remigrate
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    base
    `maven-publish`
    id("de.itemis.mps.gradle.common")
}


val jbrToolchainLauncher: Provider<JavaLauncher> by project

val scriptsBasePath = rootProject.layout.buildDirectory.get().asFile.absolutePath
val artifactsDir = rootProject.layout.buildDirectory.dir("artifacts/platform").get().asFile
val mpsPluginsDir = rootProject.layout.buildDirectory.dir("mps-plugins").get().asFile.absolutePath

fun scriptFile(relativePath: String): File = File("$scriptsBasePath/$relativePath")

val script_test_mbeddrPlatform = File(scriptsBasePath, "com.mbeddr.platform/build-ts-tests.xml")
val script_mbeddrPlatform_sandboxes = File(scriptsBasePath, "com.mbeddr.platform/build-sandboxes.xml")

// Project group
group = "com.mbeddr"
version = rootProject.version

repositories {
    maven("https://artifacts.itemis.cloud/repository/maven-mps/")
    mavenCentral()
    // required for com.michaelbaranov:microba library
    maven("https://maven.atlassian.com/content/repositories/atlassian-public/")
}

val mpsLibraries by configurations.registering {
    isCanBeConsumed = false
}

dependencies {
    mpsLibraries(project(":", configuration = "mpsExtensionsZip"))
}

data class BundledDep(
    val name: String,
    val notations: List<String>,
    val modulePath: String,
    val depConfigClosure: Action<Configuration> = Action<Configuration> { isTransitive = false }) {

    val configName = name + "_bundled"
    val taskName = "resolve_" + configName
}

val bundledDeps = listOf(
        BundledDep("commonmark", listOf("org.commonmark:commonmark:0.27.1"), "com.mbeddr.doc/languages/com.mbeddr.doc.gen_markdown"),
        BundledDep("poi-ooxml", listOf("org.apache.poi:poi-ooxml:5.5.1"), "com.mbeddr.doc/solutions/com.mbeddr.spreadsheet.libs", {
            exclude(module = "commons-compress")
            exclude(module = "commons-math3")
            exclude(module = "SparseBitSet")
        }),
        BundledDep("jung", listOf("net.sf.jung:jung-algorithms:2.1.1",
                                "net.sf.jung:jung-visualization:2.1.1",
                                "net.sf.jung:jung-graph-impl:2.1.1"),
                "com.mbeddr.mpsutil/languages/com.mbeddr.mpsutil.jung/solutions/pluginSolution", {
            exclude(module = "guava")
        }),
        BundledDep("jfreechart", listOf("org.jfree:jfreechart:1.5.6"), "com.mbeddr.mpsutil/solutions/com.mbeddr.mpsutil.jfreechart.runtime", {}),
        BundledDep("plantuml", listOf("net.sourceforge.plantuml:plantuml:1.2026.2"), "com.mbeddr.mpsutil/solutions/com.mbeddr.mpsutil.plantuml/solutions/pluginSolution"),
        BundledDep("opencsv", listOf("au.com.bytecode:opencsv:2.4"), "com.mbeddr.mpsutil/solutions/com.opencsv"),
        BundledDep("mockito", listOf("org.mockito:mockito-core:5.22.0"), "com.mbeddr.mpsutil/solutions/org.mockito", {}),
        BundledDep("ecore", listOf("org.eclipse.emf:org.eclipse.emf.ecore.xcore:1.36.0"), "com.mbeddr.mpsutil/solutions/com.mbeddr.mpsutil.ecore.stubs", {
            exclude(module = "aopalliance")
            exclude(module = "antlr-runtime")
            exclude(module = "org.eclipse.osgi")
            exclude(module = "org.eclipse.xtend.lib")
            exclude(module = "guava")
        })
)

bundledDeps.forEach { bd ->
    val config = configurations.register(bd.configName, bd.depConfigClosure)
    bd.notations.forEach { depNotation -> dependencies.add(bd.configName, depNotation) }

    tasks.register(bd.taskName, Sync::class.java) {
        from(config)
        into("${bd.modulePath}/lib")

        // Strip version numbers from file names
        rename { filename ->
            val ra = config.get().resolvedConfiguration.resolvedArtifacts.first { ra -> ra.file.name == filename }

            if (ra.classifier != null) {
                "${ra.name}-${ra.classifier}.${ra.extension}"
            } else {
                "${ra.name}.${ra.extension}"
            }
        }
    }
}

val resolveBundledLibraries by tasks.registering {
    dependsOn(bundledDeps.map { it.taskName })
}

val resolveMpsLibraries by tasks.registering(Sync::class) {
    description = "Download the MPS libraries that are used in this project."
    dependsOn(mpsLibraries)
    from(mpsLibraries.map { it.files.map(project::zipTree) })
    into(rootProject.layout.buildDirectory.dir("dependencies"))
}

val build_allScripts by tasks.registering(MpsGenerate::class) {
    description = "Generates the build script that contains all other build scripts."
    javaLauncher = jbrToolchainLauncher
    environmentKind = EnvironmentKind.MPS

    val mpsHomeProvider: Provider<Directory> by project
    mpsHome = mpsHomeProvider


    projectLocation = file("com.mbeddr.platform.build")
    pluginRoots.from(tasks.named("resolveMpsLibraries", Sync::class.java).map { it.destinationDir })
    pluginRoots.from(mpsHome.dir("plugins"))

    folderMacros.put("mbeddr.github.core.home", rootProject.layout.projectDirectory)
}

val build_actionsfilter by tasks.registering(BuildLanguages::class) {
    dependsOn(build_allScripts)
    script = scriptFile("com.mbeddr.platform/actionsfilter.xml")
    description = "Builds the actions filter IntelliJ plugin."

    // see comment in build_allScripts, above, for reasons we need to explicitly state task output
    // to address NO-SOURCE failure for package_mbeddrPlatform
    outputs.dir(File(artifactsDir, "com.mbeddr.mpsutil.actionsfilter/"))
    outputs.upToDateWhen { false }
}

val build_platform by tasks.registering(BuildLanguages::class) {
    dependsOn(build_allScripts, build_actionsfilter, resolveBundledLibraries)
    script = scriptFile("com.mbeddr.platform/build.xml")
    description = "Build the mbeddr platform."
    outputs.dir(File(artifactsDir, "com.mbeddr.platform/"))
    outputs.upToDateWhen { false }
}

val install_actionsfilter by tasks.registering(Copy::class) {
    dependsOn(build_actionsfilter)
    description = "Copy the actions filter IntelliJ plugin to the MPS plugin\"s directory"
    from("$rootDir/artifacts/com.mbeddr.mpsutil.actionsfilter/")
    include("com.mbeddr.mpsutil.actionsfilter/")
    into("$mpsPluginsDir")
}

val generate_mbeddr_platform_tests by tasks.registering(RunAntScript::class) {
    dependsOn(build_platform)
    script = script_test_mbeddrPlatform
    description = "Build the mbeddr platform tests."
    targets = listOf("generate")
}

val generate_platform_sandboxes by tasks.registering(RunAntScript::class) {
    dependsOn(build_platform)
    script = script_mbeddrPlatform_sandboxes
    description = "Build the mbeddr platform sandboxes."
    targets = listOf("generate")
}

val generate_platform_languages by tasks.registering {
    dependsOn(build_platform, generate_mbeddr_platform_tests, generate_platform_sandboxes)
}

val test_mbeddr_platform by tasks.registering(TestLanguages::class) {
    dependsOn(build_platform, generate_mbeddr_platform_tests)
    script = script_test_mbeddrPlatform
    description = "Execute typesystem and generator tests for the mbeddr plaform."
    targets = listOf("check")
}

val test by tasks.registering {
    dependsOn(test_mbeddr_platform)
    description = "Run all tests in the mbeddr platform."
}

tasks.check {
    dependsOn(test)
    description = "Run all checks."
}

val build_platform_distribution by tasks.registering(BuildLanguages::class) {
    dependsOn(build_platform, test_mbeddr_platform)
    script = scriptFile("com.mbeddr.platform/build-distribution.xml")
    description = "Build the platform describution."
}

val package_mbeddrPlatform by tasks.registering(Zip::class) {
    dependsOn(build_platform)
    description = "Package the mbeddr platform."
    archiveFileName = "com.mbeddr.platform.zip"
    from(artifactsDir) {
        include("com.mbeddr.platform/**")
    }
}

artifacts.add("default", package_mbeddrPlatform)

val defaultWrapper by tasks.registering {
    dependsOn(build_platform)
    doFirst {
        println("####################################################################################")
        println("#                      THE DEFAULT TASK HAS BEEN CHANGED                           #")
        println("#                                                                                  #")
        println("# The default task now only builds the mbeddr platform and no longer all of mbeddr #")
        println("# including the C part. In order to build everything you will have to invoke the   #")
        println("# task build_mbeddr. This will give you the old behaviour of building everything.  #")
        println("####################################################################################")
    }
}

rootProject.defaultTasks("defaultWrapper")

val publishMbeddrPlatformToLocal by tasks.registering {
    dependsOn("publishMbeddrPlatformPublicationToMavenLocal")
    description = "Publish the mbeddr platform to the local Maven repository."
}

fun getPomsOfConfiguration(cfg: Configuration): List<File> {
    val componentIds =
            cfg.incoming.resolutionResult.allDependencies
                    .filterIsInstance<ResolvedDependencyResult>()
                    .map { it.selected.id }

    val resolutionResult = dependencies.createArtifactResolutionQuery()
            .forComponents(componentIds)
            .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
            .execute()

    return resolutionResult
            .resolvedComponents.flatMap { it.getArtifacts(MavenPomArtifact::class.java) }
            .filterIsInstance<ResolvedArtifactResult>()
            .map { it.file }
}

data class Coordinates(val groupId: String?, val artifactId: String?, val version: String?, val classifier: String?, val type: String?)

fun getProvidedDependenciesFromPom(pomFile: File): List<Coordinates> {
    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pomFile)

    val result = mutableListOf<Coordinates>()

    // Add actual <scope>provided</scope> deps
    val deps = doc.getElementsByTagName("dependency")
    for (i in 0..< deps.length) {
        val d = deps.item(i)

        val kids = d.childNodes
        var g: String? = null
        var a: String? = null
        var v: String? = null
        var c: String? = null
        var t: String? = null
        var s: String? = null

        for (k in 0 ..< kids.length) {
            when (kids.item(k).nodeName) {
                "groupId"    -> { g = kids.item(k).textContent.trim() }
                "artifactId" -> { a = kids.item(k).textContent.trim() }
                "version"    -> { v = kids.item(k).textContent.trim() }
                "classifier" -> { c = kids.item(k).textContent.trim() }
                "type"       -> { t = kids.item(k).textContent.trim() }
                "scope"      -> { s = kids.item(k).textContent.trim() }
            }
        }
        if (s == "provided") {
            result.add(Coordinates(g, a, v, c, t))
        }
    }

    return result
}

val mbeddrBuild = run {
    val branch = GitBasedVersioning.getGitBranch()
    when {
        branch == "master" || branch.endsWith("-master") -> "master"
        else -> branch.substringAfterLast("-")
    }
}

publishing {
    repositories {
        val mbeddrPlatformBuildNumber = rootProject.version.toString()
        maven {
            url = uri(
                if (mbeddrPlatformBuildNumber.endsWith("-SNAPSHOT"))
                    "https://artifacts.itemis.cloud/repository/maven-mps-snapshots/"
                else
                    "https://artifacts.itemis.cloud/repository/maven-mps-releases/"
            )
            if (rootProject.hasProperty("artifacts.itemis.cloud.user") && rootProject.hasProperty("artifacts.itemis.cloud.pw")) {
                credentials {
                    username = rootProject.findProperty("artifacts.itemis.cloud.user") as String?
                    password = rootProject.findProperty("artifacts.itemis.cloud.pw") as String?
                }
            }
        }
        if (mbeddrBuild == "master") {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/mbeddr/mbeddr.core")
                if (rootProject.hasProperty("gpr.token")) {
                    credentials {
                        username = rootProject.findProperty("gpr.user") as String?
                        password = rootProject.findProperty("gpr.token") as String?
                    }
                }
            }
        }
    }
    publications {
        create<MavenPublication>("mbeddrPlatform") {
            groupId = "com.mbeddr"
            artifactId = "platform"
            version = rootProject.version.toString()
            artifact(package_mbeddrPlatform)
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")

                val configurationsWithProvidedDependencies = buildList {
                    add(mpsLibraries.get())
                    addAll(bundledDeps.map { configurations.get(it.configName) })
                }

                val seen = mutableSetOf<ResolvedDependency>()
                val queue = ArrayDeque<ResolvedDependency>()

                // Visit each dependency including its transitive dependencies if they are included, so that the exact set
                // of used JARs ends up in the POM as provided dependencies.
                for (config in configurationsWithProvidedDependencies) {
                    queue.addAll(config.resolvedConfiguration.firstLevelModuleDependencies)

                    while (!queue.isEmpty()) {
                        val dep = queue.removeFirst()
                        if (seen.add(dep)) {
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", dep.moduleGroup)
                            dependencyNode.appendNode("artifactId", dep.moduleName)
                            dependencyNode.appendNode("version", dep.moduleVersion)
                            dependencyNode.appendNode("type", dep.moduleArtifacts.first().type)
                            dependencyNode.appendNode("scope", "provided")

                            queue.addAll(dep.children)
                        }
                    }
                }

                // Add provided dependencies of mpsLibraries (i.e. libraries bundled with MPS-extensions)
                val pomsOfMpsLibraries = getPomsOfConfiguration(mpsLibraries.get())
                val providedDependenciesOfMpsLibraries = pomsOfMpsLibraries.flatMap { getProvidedDependenciesFromPom(it) }

                providedDependenciesOfMpsLibraries.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", it.groupId)
                    dependencyNode.appendNode("artifactId", it.artifactId)
                    dependencyNode.appendNode("version", it.version)
                    if (it.classifier != null) {
                        dependencyNode.appendNode("classifier", it.classifier)
                    }
                    if (it.type != null) {
                        dependencyNode.appendNode("type", it.type)
                    }
                    dependencyNode.appendNode("scope", "provided")
                }
            }
            pom {
                licenses {
                    license {
                        name = "EPL-2.0"
                        url = "https://www.eclipse.org/legal/epl-v20.html"
                        comments = "Eclipse Public License - v 2.0"
                        distribution = "repo"
                    }
                }
                organization {
                    name = "itemis AG"
                    url = "https://www.itemis.com"
                }
            }
        }
    }
}

if (mbeddrBuild == "master") {
    tasks.named("publishMbeddrPlatformPublicationToMavenRepository") {
        dependsOn("publishMbeddrPlatformPublicationToGitHubPackagesRepository")
    }
}

tasks.register<MpsMigrate>("migrate") {
    dependsOn(build_allScripts)

    javaLauncher = jbrToolchainLauncher
    val mpsHomeProvider: Provider<Directory> by project
    mpsHome = mpsHomeProvider

    haltOnPrecheckFailure = true
    haltOnDependencyError = true

    projectDirectories.from(projectDir)
    pluginRoots.from(tasks.named("resolveMpsLibraries", Sync::class.java).map { it.destinationDir })
    pluginRoots.from(mpsHomeProvider.map { it.dir("plugins") })
    maxHeapSize = "4G"
}

tasks.register<Remigrate>("remigrate") {
    mustRunAfter("migrate")
    dependsOn(build_allScripts)

    javaLauncher = jbrToolchainLauncher
    val mpsHomeProvider: Provider<Directory> by project
    mpsHome = mpsHomeProvider

    projectDirectories.from(projectDir)
    pluginRoots.from(tasks.named("resolveMpsLibraries", Sync::class.java).map { it.destinationDir })
    pluginRoots.from(mpsHomeProvider.map { it.dir("plugins") })
    maxHeapSize = "4G"
}

tasks.build {
    dependsOn(package_mbeddrPlatform)
}
