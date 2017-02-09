package edp.davinci.persistence.base

import edp.davinci.module.DbModule
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.JdbcProfile
import slick.lifted.CanBeQueryCondition

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BaseDal[T, A] {
  def insert(row: A): Future[A]

  def insert(rows: Seq[A]): Future[Seq[A]]

  def update(row: A): Future[Int]

  def update(rows: Seq[A]): Future[Unit]

  def findById(id: Long): Future[Option[A]]

  def findByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Seq[A]]

  def deleteById(id: Long): Future[Int]

  def deleteById(ids: Seq[Long]): Future[Int]

  def deleteByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Int]

  def createTable(): Future[Unit]

  def paginate[C: CanBeQueryCondition](f: (T) => C)(offset: Int, limit: Int): Future[Seq[A]]
}

class BaseDalImpl[T <: BaseTable[A], A <: BaseEntity](tableQ: TableQuery[T])(implicit val db: JdbcProfile#Backend#Database, implicit val profile: JdbcProfile) extends BaseDal[T, A] with DbModule {

  import profile.api._

  override def insert(row: A): Future[A] = insert(Seq(row)).map(_.head)

  override def insert(rows: Seq[A]): Future[Seq[A]] = {
    val ids = db.run(tableQ returning tableQ.map(_.id) ++= rows.filter(_.isValid))
    ids.flatMap[Seq[A]] {
      seq => findByFilter(_.id inSet seq)
    }
  }

  override def update(row: A): Future[Int] = if (row.isValid) db.run(tableQ.filter(_.id === row.id).update(row)) else Future(0)

  override def update(rows: Seq[A]): Future[Unit] = db.run(DBIO.seq(rows.filter(_.isValid).map(r => tableQ.filter(_.id === r.id).update(r)): _*))

  override def findById(id: Long): Future[Option[A]] = db.run(tableQ.filter(obj => obj.id === id && obj.active === true).result.headOption)

  override def findByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Seq[A]] = db.run(tableQ.withFilter(f).result)

  override def deleteById(id: Long): Future[Int] = deleteById(Seq(id))

  override def deleteById(ids: Seq[Long]): Future[Int] = db.run(tableQ.filter(_.id.inSet(ids)).delete)

  override def deleteByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Int] = db.run(tableQ.withFilter(f).delete)

  override def createTable(): Future[Unit] = db.run(DBIO.seq(tableQ.schema.create))

  override def paginate[C: CanBeQueryCondition](f: (T) => C)(offset: Int, limit: Int): Future[Seq[A]] = db.run(tableQ.withFilter(f).sortBy(_.id.nullsFirst).drop(offset).take(limit).result)
}
