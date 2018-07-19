// Copyright (C) 2017-2018 John A. De Goes. All rights reserved.
package scalaz.zio

/**
 * A description of the result of executing an `IO` value. The result is either
 * completed with a value, failed because of an uncaught `E`, or terminated
 * due to interruption or runtime error.
 */
sealed trait ExitResult[E, A] { self =>
  import ExitResult._

  final def succeeded: Boolean = self match {
    case Completed(_)  => true
    case _ => false
  }

  final def map[B](f: A => B): ExitResult[E, B] = self match {
    case Completed(a)  => Completed(f(a))
    case x     => x.asInstanceOf[ExitResult[E, B]]
  }

  final def mapError[E2](f: E => E2): ExitResult[E2, A] = self match {
    case ExitResult.Failed(e, ts) => ExitResult.Failed(f(e), ts)
    case x                    => x.asInstanceOf[ExitResult[E2, A]]
  }

  final def failed: Boolean = !succeeded

  final def fold[Z](completed: A => Z, failed: (E, List[Throwable]) => Z, interrupted: List[Throwable] => Z): Z = self match {
    case Completed(v)  => completed(v)
    case Failed(e, ts)     => failed(e, ts)
    case Terminated(e) => interrupted(e)
  }
}
object ExitResult {
  final case class Completed[E, A](value: A) extends ExitResult[E, A]
  final case class Failed[E, A](error: E, causes: List[Throwable])    extends ExitResult[E, A]

  /**
   * Exceptions are collected in their original order:
   * first element in list = first failure, last element in list = last failure.
   */
  final case class Terminated[E, A](causes: List[Throwable]) extends ExitResult[E, A]
}
