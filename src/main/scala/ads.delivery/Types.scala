package ads.delivery

import cats.effect.IO
import ads.delivery.adt.Error
import cats.effect.kernel.MonadCancel

object Types {
  type Result[T] = Either[Error, T]
  type IOResult[T] = IO[Result[T]]
  type FResult[F[_],T] = F[Result[T]]
  type ThrowableMonadCancel[F[_]] = MonadCancel[F, Throwable]
}
