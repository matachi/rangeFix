package ca.uwaterloo.gsd.rangeFix

object CompilationOptions {
	import ConditionalCompilation._
	type ENFORCE_BOOLEAN = FALSE
	type ENUM_CONVERTIBLE = FALSE
	type TRACE_TYPE_PROPAGATION = FALSE
	type CONVERT_REAL_TO_INT = TRUE
        type USE_MINIMAL_CORE = FALSE
	val THREAD_NUMBER = 1
	val Z3_PATH = """tools/Z3/z3.exe""" 
}
