import groupSagas from '../Group/sagas'
import userSagas from '../User/sagas'
import sourceSagas from '../Source/sagas'
import bizlogicSagas from '../Bizlogic/sagas'
import widgetSagas from '../Widget/sagas'
import dashboardSagas from '../Dashboard/sagas'

export default [
  ...groupSagas,
  ...userSagas,
  ...sourceSagas,
  ...bizlogicSagas,
  ...widgetSagas,
  ...dashboardSagas
]
