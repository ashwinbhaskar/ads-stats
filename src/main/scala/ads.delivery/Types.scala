package ads.delivery

import cats.effect.IO
import ads.delivery.adt.Error

object Types {
  type Result[T] = Either[Error, T]
  type IOResult[T] = IO[Result[T]]
}
