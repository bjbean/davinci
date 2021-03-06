/**
 * app.js
 *
 * This is the entry file for the application, only setup and boilerplate
 * code.
 */

// Needed for redux-saga es6 generator support
import 'babel-polyfill'

// Import all the third party stuff
import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import { applyRouterMiddleware, Router, hashHistory } from 'react-router'
import { syncHistoryWithStore } from 'react-router-redux'
import { useScroll } from 'react-router-scroll'
import config from './globalConfig'

// Import root app
import App from 'containers/App'

// Import selector for `syncHistoryWithStore`
import { makeSelectLocationState } from 'containers/App/selectors'

// Import Language Provider
import LanguageProvider from 'containers/LanguageProvider'

// Load the favicon, the manifest.json file and the .htaccess file
/* eslint-disable import/no-webpack-loader-syntax */
import '!file-loader?name=[name].[ext]!./favicon.ico'
import '!file-loader?name=[name].[ext]!./manifest.json'
import 'file-loader?name=[name].[ext]!./.htaccess' // eslint-disable-line import/extensions
/* eslint-enable import/no-webpack-loader-syntax */

import configureStore from './store'

// Import i18n messages
import { translationMessages } from './i18n'

// Import routes
import createRoutes from './routes'

import '../node_modules/antd/dist/antd.less'
import '../node_modules/react-grid-layout/css/styles.css'
import '../node_modules/react-resizable/css/styles.css'
import './assets/fonts/iconfont.css'
import './assets/override/antd.css'
import './assets/override/react-grid.css'
import './assets/less/style.less'

import echarts from 'echarts'
import 'echarts/lib/chart/bar'
import 'echarts/lib/chart/line'
import 'echarts/lib/chart/scatter'
import 'echarts/lib/chart/pie'
import 'echarts/lib/chart/sankey'
import 'echarts/lib/chart/funnel'
import 'echarts/lib/chart/treemap'
import './containers/Widget/temp/wordCloud'
import 'echarts/lib/component/legend'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/toolbox'

echarts.registerTheme('default', config.echarts.theme.default)

// Create redux store with history
// this uses the singleton browserHistory provided by react-router
// Optionally, this could be changed to leverage a created history
// e.g. `const browserHistory = useRouterHistory(createBrowserHistory)()`
const initialState = {}
const store = configureStore(initialState, hashHistory)

// Sync history and store, as the react-router-redux reducer
// is under the non-default key ("routing"), selectLocationState
// must be provided for resolving how to retrieve the "route" in the state
const history = syncHistoryWithStore(hashHistory, store, {
  selectLocationState: makeSelectLocationState()
})

// Set up the router, wrapping all Routes in the App component
const rootRoute = {
  path: '/',
  component: App,
  childRoutes: createRoutes(store),
  indexRoute: {
    onEnter: (_, replace) => {
      replace('/visual')
    }
  }
}

const render = (messages) => {
  ReactDOM.render(
    <Provider store={store}>
      <LanguageProvider messages={messages}>
        <Router
          history={history}
          routes={rootRoute}
          render={
            // Scroll to top when going to a new page, imitating default browser
            // behaviour
            applyRouterMiddleware(useScroll())
          }
        />
      </LanguageProvider>
    </Provider>,
    document.getElementById('app')
  )
}

// Hot reloadable translation json files
if (module.hot) {
  // modules.hot.accept does not accept dynamic dependencies,
  // have to be constants at compile-time
  module.hot.accept('./i18n', () => {
    render(translationMessages)
  })
}

// Chunked polyfill for browsers without Intl support
if (!window.Intl) {
  (new Promise((resolve) => {
    resolve(import('intl'))
  }))
    .then(() => Promise.all([
      import('intl/locale-data/jsonp/en.js'),
      import('intl/locale-data/jsonp/de.js')
    ]))
    .then(() => render(translationMessages))
    .catch((err) => {
      throw err
    })
} else {
  render(translationMessages)
}

// Install ServiceWorker and AppCache in the end since
// it's not most important operation and if main code fails,
// we do not want it installed
if (process.env.NODE_ENV === 'production') {
  require('offline-plugin/runtime').install() // eslint-disable-line global-require

  // disable react developer tools in production
  if (window.__REACT_DEVTOOLS_GLOBAL_HOOK__) {
    window.__REACT_DEVTOOLS_GLOBAL_HOOK__.inject = function () {}
  }
}

// if (process.env.NODE_ENV !== 'production') {
//   const { whyDidYouUpdate } = require('why-did-you-update')
//   whyDidYouUpdate(React)
// }
