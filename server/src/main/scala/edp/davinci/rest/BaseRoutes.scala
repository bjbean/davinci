package edp.davinci.rest

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.persistence.base.{BaseDal, BaseEntity, BaseTable}
import edp.davinci.persistence.entities.JsonProtocol._
import edp.davinci.persistence.entities._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.Utils._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.{Failure, Success}

trait BaseRoutes {

  def getByIdRoute(route: String): Route

  def getByAllRoute(route: String, column: String): Route

  def deleteByIdRoute(route: String): Route

  //  def deleteByAllRoute(route: String): Route

  def paginateRoute(route: String, column: String): Route

}

class BaseRoutesImpl[T <: BaseTable[A], A <: BaseEntity](baseDal: BaseDal[T, A]) extends BaseRoutes with Directives {

  def getById(id: Long): Future[Option[A]] = baseDal.findById(id)

  def getByIdComplete(route: String, id: Long, session: SessionClass): Route = {
    if (session.admin || access(route, "id"))
      onComplete(getById(id)) {
        case Success(baseEntityOpt) => baseEntityOpt match {
          case Some(baseEntity) => complete(OK, ResponseJson[BaseEntity](getHeader(200, session), baseEntity))
          case None => complete(NotFound, getHeader(404, session))
        }
        case Failure(ex) => complete(InternalServerError, getHeader(500, session))
      } else complete(Forbidden, getHeader(403, session))
  }

  override def getByIdRoute(route: String): Route = path(route / LongNumber) {
    id =>
      get {
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session => getByIdComplete(route, id, session)
        }
      }
  }

  def getByAll(session: SessionClass, column: String): Future[Seq[BaseEntity]] = {
    column match {
      case "domain_id" => baseDal.findByFilter(table => table.domain_id === session.domainId && table.active === true)
      case "id" => baseDal.findByFilter(_.active === true)
    }
  }

  def getByAllComplete(route: String, session: SessionClass, future: Future[Seq[BaseEntity]]): Route = {
    if (session.admin || access(route, "all")) {
      onComplete(future) {
        case Success(baseSeq) =>
          if (baseSeq.nonEmpty) complete(OK, ResponseJson[Seq[BaseEntity]](getHeader(200, session), baseSeq))
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  override def getByAllRoute(route: String, column: String) = path(route) {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getByAllComplete(route, session, getByAll(session, column))
      }
    }
  }


  def insertByPost(session: SessionClass, seq: Seq[BaseClass]): Future[Seq[BaseEntity]] = {
    val entitySeq = seq.map {
      generateEntity(_, session)
    }
    baseDal.insert(entitySeq.asInstanceOf[Seq[A]])
  }

  def postComplete(session: SessionClass, seq: Seq[BaseClass]): Route = {
    if (session.admin)
      onComplete(insertByPost(session, seq).mapTo[Seq[BaseEntity]]) {
        case Success(baseSeq) => complete(OK, ResponseJson[Seq[BaseEntity]](getHeader(200, session), baseSeq))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.toString, session))
      } else complete(Forbidden, getHeader(403, session))
  }

  def putComplete(session: SessionClass, seq: Seq[A]): Route = {
    if (session.admin)
      onComplete(baseDal.update(seq).mapTo[Unit]) {
        case Success(result) => complete(OK, ResponseJson[Seq[BaseEntity]](getHeader(200, session), seq))
        case Failure(ex) =>
          println(ex)
          complete(InternalServerError, getHeader(500, ex.toString, session))
      }
    else complete(Forbidden, getHeader(403, session))
  }


  def deleteByIdComplete(id: Long, session: SessionClass): Route = {
    if (session.admin)
      onComplete(getById(id)) {
        case Success(baseEntityOpt) => baseEntityOpt match {
          case Some(baseEntity) =>
            onComplete(baseDal.update(generateEntity(baseEntity, session).asInstanceOf[A]).mapTo[Int]) {
              case Success(result) => complete(OK, getHeader(200, session))
              case Failure(ex) => complete(InternalServerError, getHeader(500, session))
            }
          case None => complete(OK, getHeader(200, session))
        }
        case Failure(ex) => complete(InternalServerError, getHeader(500, session))
      } else complete(Forbidden, getHeader(403, session))
  }

  override def deleteByIdRoute(route: String): Route = path(route / LongNumber) {
    id =>
      delete {
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session => deleteByIdComplete(id, session)
        }
      }
  }


  //  override def deleteByAllRoute(route: String): Route = path(route) {
  //    delete {
  //      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
  //        session =>
  //          responseHeaderWithToken(session) {
  //            if (session.admin)
  //              onComplete(baseDal.findByFilter(table => table.domain_id === session.domainId && table.active === true).mapTo[Seq[BaseEntity]]) {
  //                case Success(seq) => onComplete(baseDal.update(seq.map(base => generateEntity(base, session)).asInstanceOf[Seq[A]]).mapTo[Unit]) {
  //                  case Success(unit) => complete(HttpResponse(OK, entity = "delete success"))
  //                  case Failure(ex) => complete(InternalServerError, "internal server error")
  //                }
  //                case Failure(ex) => complete(InternalServerError, "internal server error")
  //              }
  //            else complete(Forbidden, "user is not admin")
  //          }
  //      }
  //    }
  //  }

  def paginateFilter(filter: String, session: SessionClass, column: String): Future[Seq[BaseEntity]] = {
    val (offset, limit) = paginateInfo(filter)
    val future = column match {
      case "domain_id" => baseDal.paginate(table => table.domain_id === session.domainId && table.active === true)(offset, limit).mapTo[Seq[BaseEntity]]
      case "id" => baseDal.paginate(_.active === true)(offset, limit).mapTo[Seq[BaseEntity]]
    }
    future
  }

  override def paginateRoute(route: String, column: String): Route = path(s"${route}\\?page=\\d+&size=\\d+".r) {
    filter =>
      get {
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session =>
            println(filter)
            getByAllComplete(route, session, paginateFilter(filter, session, column))
        }
      }
  }


  def generateEntity(baseClass: BaseClass, session: SessionClass): BaseEntity = {
    baseClass match {
      case bizLogic: BizlogicClass => Bizlogic(0, session.domainId, bizLogic.source_id, bizLogic.name, bizLogic.desc, true, currentTime, session.userId, currentTime, session.userId)
      case dashboard: DashboardClass => Dashboard(0, session.domainId, dashboard.name, dashboard.desc, dashboard.publish, true, currentTime, session.userId, currentTime, session.userId)
      case domain: DomainClass => Domain(0, domain.name, domain.desc, true, currentTime, session.userId, currentTime, session.userId)
      case group: GroupClass => Group(0, session.domainId, group.name, group.desc, true, currentTime, session.userId, currentTime, session.userId)
      case libWidget: LibWidgetClass => LibWidget(0, libWidget.`type`, true, currentTime, session.userId, currentTime, session.userId)
      case source: SourceClass => Source(0, session.domainId, source.group_id, source.name, source.desc, source.`type`, source.config, true, currentTime, session.userId, currentTime, session.userId)
      case sql: SqlClass => Sql(0, session.domainId, sql.bizlogic_id, sql.name, sql.sql_type, sql.sql_tmpl, sql.sql_order, sql.desc, true, currentTime, session.userId, currentTime, session.userId)
      case sqlLog: SqlLogClass => SqlLog(0, sqlLog.sql_id, session.userId, sqlLog.start_time, sqlLog.end_time, true, sqlLog.success, sqlLog.error)
      case user: UserClass => User(0, session.domainId, user.email, "123456", user.title, user.name, false, true, currentTime, session.userId, currentTime, session.userId)
      case widget: WidgetClass => Widget(0, session.domainId, widget.widgetlib_id, widget.bizlogic_id, widget.name, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish, true, currentTime, session.userId, currentTime, session.userId)
    }
  }

  //
  //  def generateEntity(id: Long, baseClass: BaseClass, session: SessionClass): BaseEntity = {
  //    baseClass match {
  //      case bizLogic: BizlogicClass => Bizlogic(id, session.domainId, bizLogic.source_id, bizLogic.name, bizLogic.desc, true, currentTime, session.userId, currentTime, session.userId)
  //      case dashboard: DashboardClass => Dashboard(id, session.domainId, dashboard.name, dashboard.desc, dashboard.publish, true, currentTime, session.userId, currentTime, session.userId)
  //      case domain: DomainClass => Domain(id, domain.name, domain.desc, true, currentTime, session.userId, currentTime, session.userId)
  //      case group: GroupClass => Group(id, session.domainId, group.name, group.desc, true, currentTime, session.userId, currentTime, session.userId)
  //      case libWidget: LibWidgetClass => LibWidget(id, libWidget.`type`, true, currentTime, session.userId, currentTime, session.userId)
  //      case source: SourceClass => Source(id, session.domainId, source.group_id, source.name, source.desc, source.`type`, source.config, true, currentTime, session.userId, currentTime, session.userId)
  //      case sql: SqlClass => Sql(id, session.domainId, sql.bizlogic_id, sql.name, sql.sql_type, sql.sql_tmpl, sql.sql_order, sql.desc, true, currentTime, session.userId, currentTime, session.userId)
  //      case sqlLog: SqlLogClass => SqlLog(id, sqlLog.sql_id, session.userId, sqlLog.start_time, sqlLog.end_time, true, sqlLog.success, sqlLog.error)
  //      case user: UserClass => User(id, session.domainId, user.email, "123456", user.title, user.name, false, true, currentTime, session.userId, currentTime, session.userId)
  //      case widget: WidgetClass => Widget(id, session.domainId, widget.widgetlib_id, widget.bizlogic_id, widget.name, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish, true, currentTime, session.userId, currentTime, session.userId)
  //    }
  //  }

  def generateEntity(baseEntity: BaseEntity, session: SessionClass): BaseEntity = {
    baseEntity match {
      case bizLogic: Bizlogic => Bizlogic(bizLogic.id, bizLogic.domain_id, bizLogic.source_id, bizLogic.name, bizLogic.desc, false, bizLogic.create_time, bizLogic.create_by, currentTime, session.userId)
      case dashboard: Dashboard => Dashboard(dashboard.id, dashboard.domain_id, dashboard.name, dashboard.desc, dashboard.publish, false, dashboard.create_time, dashboard.create_by, currentTime, session.userId)
      case domain: Domain => Domain(domain.id, domain.name, domain.desc, false, domain.create_time, domain.create_by, currentTime, session.userId)
      case group: Group => Group(group.id, group.domain_id, group.name, group.desc, false, group.create_time, group.create_by, currentTime, session.userId)
      case libWidget: LibWidget => LibWidget(libWidget.id, libWidget.`type`, false, libWidget.create_time, libWidget.create_by, currentTime, session.userId)
      case source: Source => Source(source.id, source.domain_id, source.group_id, source.name, source.desc, source.`type`, source.config, false, source.create_time, source.create_by, currentTime, session.userId)
      case sql: Sql => Sql(sql.id, sql.domain_id, sql.bizlogic_id, sql.name, sql.sql_type, sql.sql_tmpl, sql.sql_order, sql.desc, false, sql.create_time, sql.create_by, currentTime, session.userId)
      case sqlLog: SqlLog => SqlLog(sqlLog.id, sqlLog.sql_id, sqlLog.user_id, sqlLog.start_time, sqlLog.end_time, false, sqlLog.success, sqlLog.error)
      case user: User => User(user.id, user.domain_id, user.email, user.password, user.title, user.name, user.admin, false, user.create_time, user.create_by, currentTime, session.userId)
      case widget: Widget => Widget(widget.id, widget.domain_id, widget.widgetlib_id, widget.bizlogic_id, widget.name, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish, false, widget.create_time, widget.create_by, currentTime, session.userId)
    }
  }

  def paginateInfo(filter: String): (Int, Int) = {
    val pattern = new Regex("""\d+""")
    val array = pattern.findAllIn(filter).toArray
    val page = array(0).toInt
    val size = array(1).toInt
    ((page - 1) * size + 1, size)
  }

  def access(route: String, `type`: String): Boolean = route match {
    case "groups" | "widgets" | "dashboards" | "bizLogics" => true
    case "users" => `type` match {
      case "id" => true
      case "all" => false
    }
    case _ => false
  }

}
