package edp.davinci.rest.libwidget

import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities.QueryLibWidget
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

class LibWidgetService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  def getAll: Future[Seq[QueryLibWidget]] = {
    val libDal = modules.libWidgetDal
    libDal.getDB.run(libDal.getTableQuery.filter(_.active === true).map(l => (l.id, l.name, l.params, l.`type`)).result).mapTo[Seq[QueryLibWidget]]
  }

}
