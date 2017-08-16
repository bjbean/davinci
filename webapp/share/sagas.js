import appSagas from './containers/App/sagas'
import shareSagas from './containers/Share/sagas'
import widgetSagas from '../app/containers/Widget/sagas'

export default [
  ...appSagas,
  ...shareSagas,
  ...widgetSagas
]
