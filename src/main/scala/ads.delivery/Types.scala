package ads.delivery

import cats.effect.IO
import ads.delivery.adt.Error

object Types {
  type RepoResult[T] = IO[Either[Error, T]]
}
