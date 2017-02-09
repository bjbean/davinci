package edp.davinci.persistence.base

trait BaseEntity {
  val id: Long
  val active: Boolean

  def isValid: Boolean = true
}
