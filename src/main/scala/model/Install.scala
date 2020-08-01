package model

import java.util.UUID
import java.time.LocalDateTime

case class Install(installId: UUID, clickId: UUID, time: LocalDateTime)