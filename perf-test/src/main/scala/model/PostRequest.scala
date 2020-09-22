package model

class PostRequest(f: => Unit) extends Request[Unit] {
  override def execute: Unit = f
}
