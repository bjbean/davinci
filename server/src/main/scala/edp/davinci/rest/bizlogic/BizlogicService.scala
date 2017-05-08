package edp.davinci.rest.bizlogic

import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities._
import edp.davinci.rest.SessionClass
import edp.davinci.util.CommonUtils
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future

class BizlogicService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val bDal = modules.bizlogicDal
  private lazy val relGBDal = modules.relGroupBizlogicDal

  def getAllBiz: Future[Seq[QueryBizlogic]] = {
    val bizlogicTQ = bDal.getTableQuery
    bDal.getDB.run(bizlogicTQ.filter(_.active === true).map(r => (r.id, r.source_id, r.name, r.sql_tmpl, r.result_table, r.desc)).result).mapTo[Seq[QueryBizlogic]]
  }

  def updateBiz(bizlogicSeq: Seq[PutBizlogicInfo], session: SessionClass): Future[Unit] = {
    val bizlogicTQ = bDal.getTableQuery
    val query = DBIO.seq(bizlogicSeq.map(r => {
      bizlogicTQ.filter(obj => obj.id === r.id && obj.active === true).map(bizlogic => (bizlogic.name, bizlogic.source_id, bizlogic.sql_tmpl, bizlogic.desc, bizlogic.update_by, bizlogic.update_time))
        .update(r.name, r.source_id, r.sql_tmpl, r.desc, session.userId, CommonUtils.currentTime)
    }): _*)
    bDal.getDB.run(query)
  }

  def deleteByBizId(bizlogicSeq: Seq[PutBizlogicInfo]): Future[Unit] = {
    val relGBTQ = relGBDal.getTableQuery
    val query = DBIO.seq(bizlogicSeq.map(r => {
      relGBTQ.filter(_.bizlogic_id === r.id).delete
    }): _*)
    bDal.getDB.run(query)
  }

  def getGroups(bizId: Long): Future[Seq[PutRelGroupBizlogic]] = {
    val relGBTQ = relGBDal.getTableQuery
    bDal.getDB.run(relGBTQ.filter(_.bizlogic_id === bizId).map(rel => (rel.id, rel.group_id, rel.sql_params)).result).mapTo[Seq[PutRelGroupBizlogic]]
  }


  def getSourceInfo(bizlogicId: Long): Future[Seq[(String, String)]] = {
    val bizlogicTQ = bDal.getTableQuery
    val sourceTQ = modules.sourceDal.getTableQuery
    val query = (bizlogicTQ.filter(obj => obj.active === true && obj.id === bizlogicId) join sourceTQ.filter(_.active === true) on (_.source_id === _.id))
      .map { case (_, s) => (s.connection_url, s.config) }.result
    bDal.getDB.run(query)
  }
}
