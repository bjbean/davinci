package edp.davinci.persistence.base

import edp.davinci.module.DbModule
import edp.davinci.rest.BaseInfo
import slick.jdbc.{JdbcBackend, JdbcProfile}
import slick.jdbc.MySQLProfile.api._
import slick.lifted.CanBeQueryCondition

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BaseDal[T<: BaseTable[A], A<: BaseEntity] {
  def getDB: JdbcBackend#DatabaseDef

  def getTableQuery: TableQuery[T]

  def insert(row: A): Future[A]

  def insert(rows: Seq[A]): Future[Seq[A]]

  def update(row: A): Future[Int]

  def update(rows: Seq[A]): Future[Unit]

  def findById(id: Long): Future[Option[A]]

  def findByName(name: String): Future[Option[A]]

  def findAll[C: CanBeQueryCondition](f: (T) => C): Future[Seq[BaseInfo]]

  def findByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Seq[A]]

  def deleteById(id: Long): Future[Int]

  def deleteById(ids: Seq[Long]): Future[Int]

  def deleteByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Int]

  def createTable(): Future[Unit]

  //  def paginate[C: CanBeQueryCondition](f: (T) => C)(offset: Int, limit: Int): Future[Seq[A]]
}

class BaseDalImpl[T <: BaseTable[A], A <: BaseEntity](tableQ: TableQuery[T])(implicit val db: JdbcProfile#Backend#Database, implicit val profile: JdbcProfile) extends BaseDal[T, A] with DbModule {

  import profile.api._

  override def getDB: JdbcBackend#DatabaseDef = db

  override def getTableQuery: TableQuery[T] = tableQ

  override def insert(row: A): Future[A] = insert(Seq(row)).map(_.head)

  override def insert(rows: Seq[A]): Future[Seq[A]] = {
    val ids = db.run(tableQ returning tableQ.map(_.id) ++= rows)
    ids.flatMap[Seq[A]] {
      seq => findByFilter(_.id inSet seq)
    }
  }

  override def update(row: A): Future[Int] = getDB.run(tableQ.filter(_.id === row.id).update(row))

  override def update(rows: Seq[A]): Future[Unit] = getDB.run(DBIO.seq(rows.map(r => {
    tableQ.filter(_.id === r.id).update(r)
  }): _*))

  override def findById(id: Long): Future[Option[A]] = getDB.run(tableQ.filter(obj => obj.id === id).result.headOption)

  override def findByName(name: String): Future[Option[A]] = getDB.run(tableQ.filter(obj => obj.name === name).result.headOption)

  override def findAll[C: CanBeQueryCondition](f: (T) => C): Future[Seq[BaseInfo]] = getDB.run(tableQ.withFilter(f).map(r => (r.id, r.name)).result).mapTo[Seq[BaseInfo]]

  override def findByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Seq[A]] = getDB.run(tableQ.withFilter(f).result)

  override def deleteById(id: Long): Future[Int] = deleteById(Seq(id))

  override def deleteById(ids: Seq[Long]): Future[Int] = getDB.run(tableQ.filter(_.id.inSet(ids)).map(x => x.active).update(false))

  override def deleteByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Int] = getDB.run(tableQ.withFilter(f).map(x => x.active).update(false))

  override def createTable(): Future[Unit] = getDB.run(DBIO.seq(tableQ.schema.create))

  //  override def paginate[C: CanBeQueryCondition](f: (T) => C)(offset: Int, limit: Int): Future[Seq[A]] = db.run(tableQ.withFilter(f).sortBy(_.id.nullsFirst).drop(offset).take(limit).result)
}
