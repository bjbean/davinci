package edp.davinci.rest

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.persistence.base.{BaseDal, BaseEntity, BaseTable}
import edp.davinci.persistence.entities._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils._
import slick.jdbc.MySQLProfile.api._
import edp.davinci.util.JsonProtocol._
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait BaseRoutes {

  def getByIdRoute(route: String): Route

  def getByNameRoute(route: String): Route

  def getByAllRoute(route: String): Route

  def deleteByIdRoute(route: String): Route

  def deleteByBatchRoute(route: String): Route

  def paginateRoute(route: String, column: String): Route

}

class BaseRoutesImpl[T <: BaseTable[A], A <: BaseEntity](baseDal: BaseDal[T, A]) extends BaseRoutes with Directives {


  override def getByIdRoute(route: String): Route = path(route / LongNumber) {
    id =>
      get {
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session => getByIdComplete(route, id, session)
        }
      }
  }

  override def getByNameRoute(route: String): Route = path(route / Segment) {
    name =>
      get {
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session => getByNameComplete(route, name, session)
        }

      }
  }


  override def getByAllRoute(route: String): Route = path(route) {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getByAllComplete(route, session, getByAll(session))
      }
    }
  }


  override def deleteByIdRoute(route: String): Route

  = path(route / LongNumber) {
    id =>
      delete {
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session => deleteByIdComplete(id, session)
        }
      }
  }

  override def deleteByBatchRoute(route: String): Route

  = path(route) {
    delete {
      entity(as[String]) {
        idStr => {
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            val ids = idStr.split(",").map(_.toLong)
            session => deleteByBatchComplete(ids, session)
          }
        }
      }
    }
  }

  override def paginateRoute(route: String, column: String): Route

  = path(route) {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          parameters('page.as[Int], 'size.as[Int] ? 20) { (offset, limit) =>
            val future = baseDal.paginate(_.active === true)(offset, limit).mapTo[Seq[BaseEntity]]
            getByAllComplete(route, session, future)
          }
      }
    }
  }

  def getByIdComplete(route: String, id: Long, session: SessionClass): Route = {
    if (session.admin || access(route, "id"))
      onComplete(baseDal.findById(id)) {
        case Success(baseEntityOpt) => baseEntityOpt match {
          case Some(baseEntity) => complete(OK, ResponseJson[BaseEntity](getHeader(200, session), baseEntity))
          case None => complete(NotFound, getHeader(404, session))
        }
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      } else complete(Forbidden, getHeader(403, session))
  }


  def getByNameComplete(route: String, name: String, session: SessionClass): Route = {
    if (session.admin || access(route, "name"))
      onComplete(baseDal.findByFilter(_.name === name)) {
        case Success(baseSeq) =>
          if (baseSeq.nonEmpty) complete(OK, ResponseJson[Seq[BaseEntity]](getHeader(200, session), baseSeq))
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      } else complete(Forbidden, getHeader(403, session))
  }


  def getByAll(session: SessionClass): Future[Seq[BaseEntity]] = baseDal.findByFilter(_.active === true)


  def getByAllComplete(route: String, session: SessionClass, future: Future[Seq[BaseEntity]]): Route = {
    if (session.admin || access(route, "all")) {
      onComplete(future) {
        case Success(baseSeq) =>
          if (baseSeq.nonEmpty) complete(OK, ResponseJson[Seq[BaseEntity]](getHeader(200, session), baseSeq))
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }


  def postComplete(session: SessionClass, seq: Seq[BaseClass]): Route = {
    if (session.admin) {
      onComplete(insertByPost(session, seq).mapTo[Seq[BaseEntity]]) {
        case Success(baseSeq) => complete(OK, ResponseJson[Seq[BaseEntity]](getHeader(200, session), baseSeq))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.toString, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def postByIdComplete(session: SessionClass, baseId: Long, extendId: Long) = {

  }

  def insertByPost(session: SessionClass, seq: Seq[BaseClass]): Future[Seq[BaseEntity]] = {
    val entitySeq = seq.map {
      generateEntityAsActive(_, session)
    }
    baseDal.insert(entitySeq.asInstanceOf[Seq[A]])
  }


  def putComplete(session: SessionClass, baseClass: Seq[BaseClass]): Route = {
    if (session.admin) {
      onComplete(baseDal.update(baseClass.asInstanceOf[Seq[A]])) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    }
    else complete(Forbidden, getHeader(403, session))
  }


  def deleteByIdComplete(id: Long, session: SessionClass): Route = {
    if (session.admin) {
      onComplete(baseDal.deleteById(id).mapTo[Int]) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }


  def deleteByBatchComplete(ids: Seq[Long], session: SessionClass): Route = {
    if (session.admin) {
      onComplete(baseDal.deleteById(ids).mapTo[Int]) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))

  }


  def generateEntityAsActive(baseClass: BaseClass, session: SessionClass): BaseEntity = {
    baseClass match {
      case bizLogic: BizlogicClass => Bizlogic(0, bizLogic.source_id, bizLogic.name, bizLogic.desc, active = true, currentTime, session.userId, currentTime, session.userId)
      case dashboard: DashboardClass => Dashboard(0, dashboard.name, dashboard.desc, dashboard.publish, active = true, currentTime, session.userId, currentTime, session.userId)
      case group: GroupClass => Group(0, group.name, group.desc, active = true, currentTime, session.userId, currentTime, session.userId)
      case libWidget: LibWidgetClass => LibWidget(0, libWidget.`type`, active = true, currentTime, session.userId, currentTime, session.userId)
      case source: SourceClass => Source(0, source.group_id, source.name, source.desc, source.`type`, source.config, active = true, currentTime, session.userId, currentTime, session.userId)
      case sql: SqlClass => Sql(0, sql.bizlogic_id, sql.name, sql.sql_type, sql.sql_tmpl, sql.sql_order, sql.desc, active = true, currentTime, session.userId, currentTime, session.userId)
      case sqlLog: SqlLogClass => SqlLog(0, sqlLog.sql_id, session.userId, sqlLog.start_time, sqlLog.end_time, active = true, sqlLog.success, sqlLog.error)
      case user: UserClass => User(0, user.email, "123456", user.title, user.name, admin = false, active = true, currentTime, session.userId, currentTime, session.userId)
      case widget: WidgetClass => Widget(0, widget.widgetlib_id, widget.bizlogic_id, widget.name, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish, active = true, currentTime, session.userId, currentTime, session.userId)
    }
  }


  //  def generateEntityAsInactive(baseEntity: BaseEntity, session: SessionClass): BaseEntity = {
  //    baseEntity match {
  //      case bizLogic: Bizlogic => Bizlogic(bizLogic.id, bizLogic.domain_id, bizLogic.source_id, bizLogic.name, bizLogic.desc, active = false, bizLogic.create_time, bizLogic.create_by, currentTime, session.userId)
  //      case dashboard: Dashboard => Dashboard(dashboard.id, dashboard.domain_id, dashboard.name, dashboard.desc, dashboard.publish, active = false, dashboard.create_time, dashboard.create_by, currentTime, session.userId)
  //      case domain: Domain => Domain(domain.id, domain.name, domain.desc, active = false, domain.create_time, domain.create_by, currentTime, session.userId)
  //      case group: Group => Group(group.id, group.domain_id, group.name, group.desc, active = false, group.create_time, group.create_by, currentTime, session.userId)
  //      case libWidget: LibWidget => LibWidget(libWidget.id, libWidget.`type`, active = false, libWidget.create_time, libWidget.create_by, currentTime, session.userId)
  //      case source: Source => Source(source.id, source.domain_id, source.group_id, source.name, source.desc, source.`type`, source.config, active = false, source.create_time, source.create_by, currentTime, session.userId)
  //      case sql: Sql => Sql(sql.id, sql.domain_id, sql.bizlogic_id, sql.name, sql.sql_type, sql.sql_tmpl, sql.sql_order, sql.desc, active = false, sql.create_time, sql.create_by, currentTime, session.userId)
  //      case sqlLog: SqlLog => SqlLog(sqlLog.id, sqlLog.sql_id, sqlLog.user_id, sqlLog.start_time, sqlLog.end_time, active = false, sqlLog.success, sqlLog.error)
  //      case user: User => User(user.id, user.domain_id, user.email, user.password, user.title, user.name, user.admin, active = false, user.create_time, user.create_by, currentTime, session.userId)
  //      case widget: Widget => Widget(widget.id, widget.domain_id, widget.widgetlib_id, widget.bizlogic_id, widget.name, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish, active = false, widget.create_time, widget.create_by, currentTime, session.userId)
  //    }
  //  }

  def access(route: String, `type`: String): Boolean = route match {
    case "groups" | "widgets" | "dashboards" | "bizLogics" => true
    case "users" => `type` match {
      case "id" => true
      case "name" => true
      case "all" => false
    }
    case _ => false
  }


  //  def paginateFilter(filter: String, session: SessionClass, column: String): Future[Seq[BaseEntity]] = {
  //    val (offset, limit) = paginateInfo(filter)
  //    val future = column match {
  //      case "domain_id" => baseDal.paginate(table => table.domain_id === session.domainId && table.active === true)(offset, limit).mapTo[Seq[BaseEntity]]
  //      case "id" => baseDal.paginate(_.active === true)(offset, limit).mapTo[Seq[BaseEntity]]
  //    }
  //    future
  //  }
  //
  //
  //
  //  def paginateInfo(filter: String): (Int, Int) = {
  //    val pattern = new Regex("""\d+""")
  //    val array = pattern.findAllIn(filter).toArray
  //    val page = array(0).toInt
  //    val size = array(1).toInt
  //    ((page - 1) * size + 1, size)
  //  }


}
