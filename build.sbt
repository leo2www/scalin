// inspired by Spire build.sbt file

val scala210Version = "2.10.7"
val scala211Version = "2.11.12"
val scala212Version = "2.12.0"
val disciplineVersion = "0.8"
val scalaCheckVersion = "1.13.5"
val scalaMacrosVersion = "2.1.0"
val scalaTestVersion = "3.0.5"
val spireVersion = "0.16.0"
// val alascVersion = "0.16.0.3"
val alascVersion = "0.16.0.4-SNAPSHOT-withlaw"

lazy val scalin = (project in file("."))
  .settings(moduleName := "scalin")
  .settings(scalinSettings: _*)
  .settings(noPublishSettings)
  .aggregate(macros, core, alasc)
  .dependsOn(macros, core, alasc)

lazy val macros = (project in file("macros"))
  .settings(moduleName := "scalin-macros")
  .settings(scalinSettings: _*)
  .settings(commonJvmSettings: _*)

lazy val core = (project in file("core"))
  //.enablePlugins(TutPlugin)
  .settings(moduleName := "scalin-core")
  .settings(scalinSettings: _*)
  .settings(scalaTestSettings: _*)
  .settings(libraryDependencies += "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test")
  .settings(commonJvmSettings: _*)
  .dependsOn(macros)

lazy val alasc = (project in file("alasc"))
  .settings(moduleName := "scalin-alasc")
  .settings(scalinSettings: _*)
  .settings(scalaTestSettings: _*)
  .settings(libraryDependencies += "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test")
  .settings(libraryDependencies += "net.alasc" %% "alasc-core" % alascVersion)
  .settings(libraryDependencies += "net.alasc" %% "alasc-laws" % alascVersion)
  .settings(commonJvmSettings: _*)
  .dependsOn(macros, core)

lazy val scalinSettings = buildSettings ++ commonSettings ++ publishSettings

lazy val buildSettings = Seq(
  organization := "net.alasc",
  scalaVersion := scala212Version,
  crossScalaVersions := Seq(scala210Version, scala211Version, scala212Version)
)

lazy val commonSettings = Seq(
  scalacOptions ++= commonScalacOptions.diff(Seq(
    "-Xfatal-warnings", 
    "-language:existentials",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )),
  resolvers ++= Seq(
    "bintray/denisrosset/maven" at "https://dl.bintray.com/denisrosset/maven",
    Resolver.sonatypeRepo("snapshots"),
    //  常用仓库
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    // ---------------------------
    // 国内镜像源（加速下载）
    // ---------------------------
    "Aliyun Maven" at "https://maven.aliyun.com/repository/public",
    "Huawei Cloud" at "https://repo.huaweicloud.com/repository/maven",
    
    // ---------------------------
    // 官方中央仓库
    // ---------------------------
    "Maven Central" at "https://repo1.maven.org/maven2",
    
    // ---------------------------
    // Sonatype 仓库体系
    // ---------------------------
    "Sonatype Releases"  at "https://oss.sonatype.org/content/repositories/releases",
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    
    // ---------------------------
    // 常用第三方仓库
    // ---------------------------
    // JitPack（GitHub项目直连）
    "jitpack.io" at "https://jitpack.io",
    
    // JBoss 仓库
    "JBoss Repository" at "https://repository.jboss.org/nexus/content/repositories/releases",
    
    // Apache 快照仓库
    "Apache Snapshots" at "https://repository.apache.org/content/repositories/snapshots",
    
    // Clojars (Clojure 生态库)
    "Clojars" at "https://clojars.org/repo",
    
    // Spring 插件仓库
    // "Spring Plugins" at "https://repo.spring.io/plugins-release",
    
    // Gradle 插件仓库
    "Gradle Plugins" at "https://plugins.gradle.org/m2",
    
    // ---------------------------
    // 云厂商仓库
    // ---------------------------
    // AWS 仓库
    "AWS Release" at "https://aws.oss.sonatype.org/content/repositories/releases",
    
    // 腾讯云镜像（备用）
    "Tencent Cloud" at "https://mirrors.cloud.tencent.com/nexus/repository/maven-public",
    // 本地构建器
    Resolver.mavenLocal,
    Resolver.file("local-ivy", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
  ),
  libraryDependencies ++= Seq("org.typelevel" %% "spire" % spireVersion)
) ++ scalaMacroDependencies ++ warnUnusedImport

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/denisrosset/scalin")),
  licenses += ("GPL-3.0", url("http://opensource.org/licenses/GPL-3.0")),
  bintrayRepository := "maven",
  publishArtifact in Test := false
)

lazy val noPublishSettings = Seq(
  publish := (()),
  publishLocal := (()),
  publishArtifact := false
)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

lazy val commonJvmSettings = Seq(
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
) ++ selectiveOptimize
  // -optimize has no effect in scala-js other than slowing down the build

// do not optimize on Scala 2.10 because of optimizer bugs (cargo-cult setting
// from my experience with metal)
lazy val selectiveOptimize = 
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) => Seq()
      case Some((2, 11)) => Seq("-optimize")
      case Some((2, 12)) => Seq() // should be set for production builds
      case _ => sys.error("Unknown Scala version")
    }
  }

lazy val warnUnusedImport = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        Seq()
      case Some((2, n)) if n >= 11 =>
        Seq("-Ywarn-unused-import")
    }
  },
  scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
)

lazy val scalaMacroDependencies: Seq[Setting[_]] = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
      case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
      // in Scala 2.10, quasiquotes are provided by macro paradise
      case Some((2, 10)) =>
        Seq(
          compilerPlugin("org.scalamacros" % "paradise" % scalaMacrosVersion cross CrossVersion.full),
              "org.scalamacros" %% "quasiquotes" % scalaMacrosVersion cross CrossVersion.binary
        )
    }
  }
)

lazy val scalaTestSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "org.typelevel" %% "discipline" % disciplineVersion % "test",
    "org.typelevel" %% "spire-laws" % spireVersion % "test"
  )
)
