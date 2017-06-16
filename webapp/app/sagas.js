import appSagas from './containers/App/sagas'
import reportSagas from './containers/Report/containerSagas'

export default [
  ...appSagas,
  ...reportSagas
]
