package ads.delivery.model

import ads.delivery.adt.Category

case class CategorizedStats(fields: Map[Category, String], stats: Stats)
