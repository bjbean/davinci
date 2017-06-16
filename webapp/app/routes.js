// import { getAsyncInjectors } from './utils/asyncInjectors'

import Report from './containers/Report'
import Group from './containers/Group'
import User from './containers/User'
import Source from './containers/Source'
import Bizlogic from './containers/Bizlogic'
import Widget from './containers/Widget'
import Dashboard from './containers/Dashboard'
import Grid from './containers/Dashboard/Grid'
import Login from './containers/Login'

const errorLoading = (err) => {
  console.error('Dynamic page loading failed', err) // eslint-disable-line no-console
}

const loadModule = (cb) => (componentModule) => {
  cb(null, componentModule.default)
}

export default function createRoutes (store) {
  // const { injectReducer, injectSagas } = getAsyncInjectors(store)

  return [
    {
      path: '/login',
      component: Login
    },
    {
      path: '/report',
      name: 'report',
      indexRoute: {
        onEnter: (_, replace) => {
          replace('/report/dashboard')
        }
      },
      component: Report,
      childRoutes: [
        {
          path: '/report/dashboard',
          name: 'dashboard',
          component: Dashboard
        },
        {
          path: '/report/grid/:id',
          name: 'grid',
          component: Grid
        },
        {
          path: '/report/widget',
          name: 'widget',
          component: Widget
        },
        {
          path: '/report/bizlogic',
          name: 'bizlogic',
          component: Bizlogic
        },
        {
          path: '/report/source',
          name: 'source',
          component: Source
        },
        {
          path: '/report/user',
          name: 'user',
          component: User
        },
        {
          path: '/report/group',
          name: 'group',
          component: Group
        }
      ]
    },
    {
      path: '*',
      name: 'notfound',
      getComponent (nextState, cb) {
        import('containers/NotFoundPage')
          .then(loadModule(cb))
          .catch(errorLoading)
      }
    }
  ]
}
