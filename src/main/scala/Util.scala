
package util

import scala.annotation.targetName
// pipe operator converts f(a) to a |> f. 
// useful because a |> f |> g |> h is easier to read than h(g(f(a)))
extension [A](a: A)
  @targetName("pipe")
  inline def |> [B](inline f: A => B): B = f(a)
