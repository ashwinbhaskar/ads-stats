package model

import java.time.LocalDateTime
import java.util.UUID

case class Click(deliveryId: UUID, clickId: UUID, time: LocalDateTime)