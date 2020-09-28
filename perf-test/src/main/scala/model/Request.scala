package model

trait Request[T] {
  def execute: T
}
