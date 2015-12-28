package ca.uwaterloo.gsd.rangeFix

import org.kiama.rewriting.Rewriter
import org.kiama.rewriting.Rewriter._

object MyRewriter {
  def everywheretdWithGuard(g: => Rewriter.Strategy,
                            s: => Rewriter.Strategy) : Rewriter.Strategy = 
    g <+ (attempt(s) <* (all (everywheretdWithGuard(g, s))))

  def everywherebuWithGuard(g: => Rewriter.Strategy,
                            s: => Rewriter.Strategy) : Rewriter.Strategy = 
    g <+ (all (everywherebuWithGuard(g, s)) <* attempt(s))



}
