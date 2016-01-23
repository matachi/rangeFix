package ca.uwaterloo.gsd.rangeFix

import org.kiama.rewriting.Rewriter
import org.kiama.rewriting.Rewriter._
import org.kiama.rewriting

object MyRewriter {
  def everywheretdWithGuard(g: => rewriting.Strategy,
                            s: => rewriting.Strategy) : rewriting.Strategy = 
    g <+ (attempt(s) <* (all (everywheretdWithGuard(g, s))))

  def everywherebuWithGuard(g: => rewriting.Strategy,
                            s: => rewriting.Strategy) : rewriting.Strategy = 
    g <+ (all (everywherebuWithGuard(g, s)) <* attempt(s))



}
