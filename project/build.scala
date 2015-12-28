import sbt._
import Keys._


object MyBuild extends Build
{
	val old_rootSettings: Seq[Setting[_]] = Defaults.defaultSettings ++ Seq[Setting[_]](
		sourceGenerators in Compile <+= sourceManaged in Compile map { outRoot: File =>
			val outDir = outRoot + "/ca/uwaterloo/gsd/rangeFix/"
			def callJFlex(src:String):Int = {
				val srcPath = new File(src).getAbsolutePath()
				val tgtPath = new File(outDir).getAbsolutePath()
				val jflex = "java -cp tools/jflex/lib/JFlex.jar JFlex.Main -d \"%s\" \"%s\""
				jflex.format(tgtPath, srcPath) !
			}
			def callCup(src:String, packageName:String, prefix:String):Int = {
				val cup = "java -jar tools/java-cup-11a.jar -destdir \"%s\" -parser %s -symbols %s %s"
				cup.format(new File(outDir).getAbsolutePath(), prefix + "Parser", prefix + "Symbol", src) !
			}
			def callBison(src:String, tgt:String):Int = {
				val bisonRoot = "tools/bison/"
				val bison = bisonRoot + "bison.exe -v --language=java -o \"%s\" \"%s\""
				val bison_pkgdatadir = "tools/bison/share/bison"
				val path = "tools/bison"
				Process(bison.format(new File(tgt).getAbsolutePath, new File(src).getAbsolutePath()), new File("."), "Path"->new File(path).getAbsolutePath, "BISON_PKGDATADIR" -> bison_pkgdatadir) !
			}
			def IsSrcNewer(src:String, tgt:String) = {
				if (!new File(tgt).exists()) true
				else new File(src).lastModified > new File(tgt).lastModified
			}
			def executeIfNewer(src:String, tgt:String, runProcess: => Int ) {
				if (IsSrcNewer(src, tgt)) {
					println("##################################")
					println("Compiling " + src)
					if (runProcess != 0)
						error("Compiling " + src + " failed.")
					println("##################################")
				}
			}
			val eccLexerSrc = "src/main/ecc.flex"
			val eccLexerTgt = outDir + "EccLexer.java"
			val eccParserSrc = "src/main/ecc.y"
			val eccParserTgt = outDir + "EccParser.java"
			executeIfNewer(eccLexerSrc, eccLexerTgt, callJFlex(eccLexerSrc))
			executeIfNewer(eccParserSrc, eccParserTgt, callBison(eccParserSrc, eccParserTgt))
			val eccFullLexerSrc = "src/main/eccFullInternal.flex"
			val eccFullLexerTgt = outDir + "EccFullInternalLexer.java"
			val eccFullValuesLexerSrc = "src/main/eccFullValues.flex"
			val eccFullValuesLexerTgt = outDir + "EccFullValuesLexer.java"
			val eccFullParserSrc = "src/main/eccFull.y"
			val eccFullParserTgt = outDir + "EccFullParser.java"
			executeIfNewer(eccFullLexerSrc, eccFullLexerTgt, callJFlex(eccFullLexerSrc))
			executeIfNewer(eccFullValuesLexerSrc, eccFullValuesLexerTgt, callJFlex(eccFullValuesLexerSrc))
			executeIfNewer(eccFullParserSrc, eccFullParserTgt, callBison(eccFullParserSrc, eccFullParserTgt))
			val annLexerSrc = "src/main/annotation.flex"
			val annLexerTgt = outDir + "AnnotationLexer.java"
			val annParserSrc = "src/main/annotation.y"
			val annParserTgt = outDir + "AnnotationParser.java"
			executeIfNewer(annLexerSrc, annLexerTgt, callJFlex(annLexerSrc))
			executeIfNewer(annParserSrc, annParserTgt, callBison(annParserSrc, annParserTgt))
			// val imlLexerSrc = "src/main/iml.flex"
			// val imlLexerTgt = outDir + "ImlLexer.java"
			// val imlParserSrc = "src/main/iml.cup"
			// val imlParserTgt = outDir + "ImlParser.java"
			// val imlParserTgt2 = outDir + "ImlSymbol.java"
			// executeIfNewer(imlLexerSrc, imlLexerTgt, callJFlex(imlLexerSrc))
			// executeIfNewer(imlParserSrc, imlParserTgt, callCup(imlParserSrc, "ca.uwaterloo.gsd.rangeFix", "Iml"))
			Seq(new File(eccLexerTgt), new File(eccParserTgt), new File(eccFullLexerTgt), new File(eccFullParserTgt), new File(eccFullValuesLexerTgt), new File(annLexerTgt), new File(annParserTgt)/*, new File(imlLexerTgt), new File(imlParserTgt), new File(imlParserTgt2)*/)
		},
		scalaVersion := "2.9.2",
		compileOrder := CompileOrder.Mixed
	)
	
	val rootSettings = Defaults.defaultSettings ++ Seq[Setting[_]](
		unmanagedSourceDirectories in Compile <+= baseDirectory(_ / "generated"),
		scalaVersion := "2.9.2",
		compileOrder := CompileOrder.Mixed,
		parallelExecution in Test := true,
		scalacOptions ++= Seq("-unchecked", "-deprecation"),
		// libraryDependencies += "tools.jar",
		javaOptions in (Test, run) += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000",
		fork in (Test, run) := true
	)
	
	val parserGenerator = TaskKey[Unit]("generate", "Generate parsers")

	val generatorTask = parserGenerator := {
		val outDir = "generated/ca/uwaterloo/gsd/rangeFix/"
		def callJFlex(src:String, tgtPath:String):Int = {
			import java.io._
			val srcPath = new File(src).getAbsolutePath()
			val tgtRoot = new File(outDir).getAbsolutePath()
			val jflex = "java -cp tools/jflex/lib/JFlex.jar JFlex.Main -d \"%s\" \"%s\""
			val result = jflex.format(tgtRoot, srcPath) !
							
			//convert the possible ANSI code to unicode
			if (new File(tgtPath).exists) {
				print("converting to UTF-8...")
				val content = new StringBuffer( new File(tgtPath).length.toInt )
				val reader = new BufferedReader( new FileReader(tgtPath) )
				try {
					var line = reader.readLine()
					while (line != null) {
						content.append(line)
						content.append("\n")
						line = reader.readLine()
					}
				}
				finally {
					reader.close 
				}
				val writer = new OutputStreamWriter( new FileOutputStream(tgtPath), "UTF-8" )
				try {
					writer.write(content.toString())
					println("ok")
				}
				catch {
					case _ => println("failed")
				}
				finally {
					writer.close()
				}
			}
			result
			
		}
		def callCup(src:String, packageName:String, prefix:String):Int = {
			val cup = "java -jar tools/java-cup-11a.jar -destdir \"%s\" -parser %s -symbols %s %s"
			cup.format(new File(outDir).getAbsolutePath(), prefix + "Parser", prefix + "Symbol", src) !
		}
		def callBison(src:String, tgt:String):Int = {
			val bisonRoot = "tools/bison/"
			val bison = bisonRoot + "bison.exe -v --language=java -o \"%s\" \"%s\""
			val bison_pkgdatadir = "tools/bison/share/bison"
			val path = "tools/bison"
			Process(bison.format(new File(tgt).getAbsolutePath, new File(src).getAbsolutePath()), new File("."), "Path"->new File(path).getAbsolutePath, "BISON_PKGDATADIR" -> bison_pkgdatadir) !
		}
		def IsSrcNewer(src:String, tgt:String) = {
			if (!new File(tgt).exists()) true
			else new File(src).lastModified > new File(tgt).lastModified
		}
		def executeIfNewer(src:String, tgt:String, runProcess: => Int ) {
			if (IsSrcNewer(src, tgt)) {
				println("##################################")
				println("Compiling " + src)
				if (runProcess != 0)
					error("Compiling " + src + " failed.")
				println("##################################")
			}
		}
		val eccLexerSrc = "src/main/ecc.flex"
		val eccLexerTgt = outDir + "EccLexer.java"
		val eccParserSrc = "src/main/ecc.y"
		val eccParserTgt = outDir + "EccParser.java"
		executeIfNewer(eccLexerSrc, eccLexerTgt, callJFlex(eccLexerSrc, eccLexerTgt))
		executeIfNewer(eccParserSrc, eccParserTgt, callBison(eccParserSrc, eccParserTgt))
		val eccFullLexerSrc = "src/main/eccFullInternal.flex"
		val eccFullLexerTgt = outDir + "EccFullInternalLexer.java"
		val eccFullValuesLexerSrc = "src/main/eccFullValues.flex"
		val eccFullValuesLexerTgt = outDir + "EccFullValuesLexer.java"
		val eccFullParserSrc = "src/main/eccFull.y"
		val eccFullParserTgt = outDir + "EccFullParser.java"
		executeIfNewer(eccFullLexerSrc, eccFullLexerTgt, callJFlex(eccFullLexerSrc, eccFullLexerTgt))
		executeIfNewer(eccFullValuesLexerSrc, eccFullValuesLexerTgt, callJFlex(eccFullValuesLexerSrc, eccFullValuesLexerTgt))
		executeIfNewer(eccFullParserSrc, eccFullParserTgt, callBison(eccFullParserSrc, eccFullParserTgt))
		val annLexerSrc = "src/main/annotation.flex"
		val annLexerTgt = outDir + "AnnotationLexer.java"
		val annParserSrc = "src/main/annotation.y"
		val annParserTgt = outDir + "AnnotationParser.java"
		executeIfNewer(annLexerSrc, annLexerTgt, callJFlex(annLexerSrc, annLexerTgt))
		executeIfNewer(annParserSrc, annParserTgt, callBison(annParserSrc, annParserTgt))
		Seq(new File(eccLexerTgt), new File(eccParserTgt), new File(eccFullLexerTgt), new File(eccFullParserTgt), new File(eccFullValuesLexerTgt), new File(annLexerTgt), new File(annParserTgt))
	}
	
	
	lazy val root = Project(
		"root", 
		file("."), 
		settings = rootSettings ++ Seq(generatorTask) 
	)

}
